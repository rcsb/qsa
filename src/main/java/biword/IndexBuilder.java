package biword;

import fragments.Biword;
import fragments.Biwords;
import fragments.index.IndexFile;
import geometry.Point;
import geometry.PointConversion;
import grid.sparse.Buffer;
import grid.sparse.MultidimensionalArray;
import io.Directories;
import io.LineFile;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import javax.vecmath.Point3d;
import pdb.PdbLine;
import superposition.SuperPositionQCP;
import util.Timer;

public class IndexBuilder {

	private static Directories dirs = Directories.createDefault();
	// ID of biword can be int
	// last dimension is bucket
	//int[][][][][][][] array = new int[10][10][10][10][10][10][10]; // can be made partially sparse by top down initalization
	static float[] globalMin = new float[10];
	static float[] globalMax = new float[10];
	static int bracketN = 20;

	/// how empty it will be...
	// define coordinate system
	// first word - 1. axixs
	// from center perpendicular towards other word: 2. axis, duplicate if uncertain
	// 2. word determines orientation of 3. axis, duplicate if uncertain
	// 
	// distance to second word center
	// angle to second word center (duplicate)
	// angle of words (duplicate)
	// torsion angle (duplicate)
	// CA-CB angle of middle residue
	// mostly alpha, mostly beta, else (duplicate uncertain but rarely)
	// length of words
	// see distributions in weka
	// or just place it into space and learn? especially chebyshev would be nice
	// focus on 5 residues (CA-CB for interfaces maybe)
	// 2x phi, psi = 4
	// 6 for orientation, distance, angles, if last two uncertain, leave them in one bucket
	// ! focus only on local contacts - at most distance of two beta sheets, but rather:
	// (maximum distance of residues touching), require they actually touch by atoms (all, just in case)
	public static void main(String[] args) {
		//init();
		analyze();
	}

	private static void init() {
		BiwordsProvider bp = new BiwordsProvider();
		Biwords bs;
		int proteins = 0;
		int biwords = 0;
		LineFile lf = new LineFile(new File("c:/kepler/rozbal/biwords.csv"));
		LineFile vlf = new LineFile(new File("c:/kepler/rozbal/biword_vectors.csv"));
		IndexFile index = new IndexFile();

		try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(
			dirs.getBiwordIndex())))) {

			while ((bs = bp.next()) != null) {
				int count = 0;
				//System.out.println("pdb=" + bp.getLastPdbCode());
				//save(bs.getBiwords(), new File("c:/kepler/rozbal/" + bp.getLastPdbCode() + ".pdb"));
				for (Biword bw : bs.getBiwords()) {
					count++;

					index.add(bw, dos);
				}
				//lf.writeLine(bp.getLastPdbCode() + "," + bp.getLastSize() + "," + count);
				biwords += count;
				proteins++;
				System.out.println("bw_tot " + biwords + " | " + "now " + count + " | " + proteins);
				if (proteins > 10000) {
					break;
				}
				//System.out.println("	EXIT");
				//return;
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static double angleDif(double a, double b) {
		double c = Math.abs(a - b);
		if (c > 180) {
			c = 360 - c;
		}
		return c;
	}

	private static void analyze() {
		SuperPositionQCP qcp = new SuperPositionQCP();
		IndexFile nif = new IndexFile();
		System.out.println("reading...");
		nif.readAll(1 * 100 * 1000);
		System.out.println(nif.size() + " read");
		Random random = new Random(1);
		float a = 90;
		float b = 50;
		//float  ar = a / 180 * Math.PI;
		float shift = 4;

		for (int i = 0; i < nif.size(); i++) {
			float[] v = nif.getVector(i);
			for (int d = 0; d < v.length; d++) {
				if (v[d] < globalMin[d]) {
					globalMin[d] = v[d];
				}
				if (v[d] > globalMax[d]) {
					globalMax[d] = v[d];
				}
			}
		}
		System.out.println("BOUNDARIES");
		for (int d = 0; d < globalMin.length; d++) {
			System.out.println(globalMin[d] + " - " + globalMax[d] + " | ");
		}
		System.out.println("----");

		float[] box = {a, a, a, a, shift, shift, shift, shift, shift, shift};
		//double[] box = {a, a, a, a, shift, b, b, shift, b, b};
		int fn = 0;
		int fp = 0;
		int tn = 0;
		int tp = 0;

		Buffer<Integer> out = new Buffer<>(nif.size());
		System.out.println("inserting...");
		Timer.start();
		MultidimensionalArray grid;
		grid = new MultidimensionalArray(nif.size(), 10, bracketN);
		for (int i = 0; i < 4; i++) {
			grid.setCycle(i);
		}

		for (int i = 0; i < nif.size(); i++) {
			grid.insert(discretize(nif.getVector(i)), i);
		}
		Timer.stop();
		System.out.println("...finished " + Timer.get());

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(dirs.getBiwordPair()))) {
			// visualize failed cases
			// RMSD might be meaningful only for bigger blocks?
			double rmsdThreshold = 1;
			for (int qi = 0; qi < 100; qi++) {
				int qr = random.nextInt(nif.size());
				Point3d[] x = nif.getPoints(qr);
				float[] xv = nif.getVector(qr);

				Timer.start();
				// query
				int dim = xv.length;
				float[] min = new float[dim];
				float[] max = new float[dim];
				for (int i = 0; i < dim; i++) {
					min[i] = xv[i] - box[i];
					max[i] = xv[i] + box[i];
				}
				out.clear();
				grid.getRange(discretize(min), discretize(max), out);
				Timer.stop();
				System.out.println("grid   " + out.size() + " in " + Timer.get());

				Timer.start();

				int smallBox = 0;
				int circle = 0;

				for (int i = 0; i < nif.size(); i++) {
					Point3d[] y = nif.getPoints(i);
					float[] yv = nif.getVector(i);
					boolean in = true;
					double[] difs = new double[xv.length];
					for (int d = 0; d < box.length; d++) {
						double dif;
						if (d < 4) { 
							dif = angleDif(xv[d], yv[d]);
						} else {
							dif = Math.abs(xv[d] - yv[d]);
						}
						difs[d] = dif;
						if (dif > box[d]) {
							in = false;
						}
					}
					qcp.set(x, y);
					double rmsd = qcp.getRmsd();
					if (rmsd <= rmsdThreshold) {
						circle++;
					}
					if (in) {
						smallBox++;
						if (rmsd <= rmsdThreshold) {
							tp++;
							//save(x, y, bw);
						} else {
							fp++;
						}
					} else { // out of box
						if (rmsd <= rmsdThreshold) {
							fn++;
							if (rmsd < rmsdThreshold / 20) {
								System.out.println((in ? "+" : "O") + " rmsd " + rmsd);
								for (int d = 0; d < box.length; d++) {
									double dif = difs[d];
									System.out.println(n(xv[d]) + " - " + n(yv[d]) + " -> " + n(dif) + " " + (dif > box[d] ? "*" : ""));
								}
								System.out.println();
								save(x, y, bw);
							}
						} else {
							//if (random.nextInt(20000) == 0) {
							//	save(x, y, bw);
							//}
							tn++;
						}
					}
				}
				Timer.stop();
				System.out.println("box    " + smallBox + " in " + Timer.get());
				System.out.println("circle " + circle);
				//if (smallBox > out.size()) {
				//	throw new RuntimeException();
				//}
				System.out.println();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println("TP = " + tp);
		System.out.println("TN = " + tn);
		System.out.println("FP = " + fp);
		System.out.println("FN = " + fn);
	}

	private static int[] discretize(float[] x) {
		int[] indexes = new int[x.length];
		for (int i = 0; i < x.length; i++) {
			float v = x[i];
			/*if (v < globalMin[i]) {
				v = globalMin[i];
			}
			if (v > globalMax[i]) {
				v = globalMax[i];
			}*/
			indexes[i] = (int) Math.floor((v - globalMin[i]) / (globalMax[i] - globalMin[i]) * bracketN);

			/*if (indexes[i] >= bracketN) {
				indexes[i] = bracketN - 1;
				//throw new RuntimeException("" + x[i] + " " + globalMax[i]);
			}*/
		}
		return indexes;
	}

	private static String n(double d) {
		return "" + ((double) Math.round(d * 100)) / 100;
	}
	public static int model = 1;
	public static int serial = 1;

	public static void save(Point3d[] xIn, Point3d[] yIn, BufferedWriter bw) throws IOException {
		Point3d[] x = new Point3d[xIn.length];
		Point3d[] y = new Point3d[yIn.length];
		if (x.length != y.length) {
			throw new RuntimeException();
		}
		for (int i = 0; i < x.length; i++) {
			x[i] = new Point3d(xIn[i]);
			y[i] = new Point3d(yIn[i]);
		}
		SuperPositionQCP qcp = new SuperPositionQCP();
		qcp.set(x, y);
		Point3d[] z = qcp.getTransformedCoordinates();
		//Point3d[][] points3d = {x, z};

		Point[][] points = {PointConversion.getPoints(x), PointConversion.getPoints(z)};

		Point center = Point.center(points[0]).plus(Point.center(points[1])).divide(2);

		//int serial = 1;
		bw.write(PdbLine.getModelString(model++));
		bw.newLine();
		for (int k = 0; k < 2; k++) {
			for (int i = 0; i < points[k].length; i++) {
				Point p = points[k][i].minus(center);
				PdbLine pl = new PdbLine(serial, "CA", "C", "GLY",
					Integer.toString(serial), 'A', p.x, p.y, p.z);
				bw.write(pl.toString());
				bw.newLine();
				if (i > 0 && i != points[k].length / 2) {
					bw.write(PdbLine.getConnectString(serial - 1, serial));
					bw.newLine();
				}
				serial++;
				//if (serial > 99999) 
			}
		}
		bw.write(PdbLine.getEndmdlString());
		bw.newLine();
	}

	/*public static void save(Biword[] biwords, File f) {
		try {
			int serial = 1;
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
				for (int i = 0; i < biwords.length; i++) {
					for (int k = 0; k < 2; k++) {
						WordImpl w = biwords[i].getWords()[k];
						Point p = w.getCentralResidue().getPosition();
						PdbLine pl = new PdbLine(serial + k, "CA", "C", "GLY",
							Integer.toString(serial + k), 'A', p.x, p.y, p.z);
						bw.write(pl.toString());
						bw.newLine();
					}
					bw.write(PdbLine.getConnectString(serial, serial + 1));
					bw.newLine();
					serial += 2;
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}*/
 /*public void add(int[] vector) {

		for (int d = 0; d < vector.length; d++) {
			Object o = array[0][0];

			//if (array[0] == null)
		}
	}

	public void query(int[] vector) {

	}*/
}
