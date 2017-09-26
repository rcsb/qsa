package biword;

import fragments.Biword;
import fragments.Biwords;
import fragments.WordImpl;
import fragments.index.IndexFile;
import geometry.Point;
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

public class FullArray {

	private static Directories dirs = Directories.createDefault();
	// ID of biword can be int
	// last dimension is bucket
	int[][][][][][][] array = new int[10][10][10][10][10][10][10]; // can be made partially sparse by top down initalization

	/// how empty it will be...
	// define coordinate system
	// first word - 1. axis
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
				System.out.println("pdb=" + bp.getLastPdbCode());
				if (false) {
					save(bs.getBiwords(), new File("c:/kepler/rozbal/" + bp.getLastPdbCode() + ".pdb"));
				}
				for (Biword bw : bs.getBiwords()) {
					count++;

					index.add(bw, dos);
				}
				//lf.writeLine(bp.getLastPdbCode() + "," + bp.getLastSize() + "," + count);
				biwords += count;
				proteins++;
				System.out.println("bw_tot " + biwords + " | " + "now " + count + " | " + proteins);
				if (proteins > 1000) {
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
		nif.readAll(50000);
		Random random = new Random(1);
		double a = 90;
		double ar = a / 180 * Math.PI;
		double shift = 4;
		//double[] box = {a, a, a, a, ar, ar, ar, shift, shift, shift};
		double[] box = {a, a, a, a, shift, shift, shift, shift, shift, shift};
		System.out.println(ar);
		int fn = 0;
		int fp = 0;
		int tn = 0;
		int tp = 0;

		// visualize failed cases
		// RMSD might be meaningful only for bigger blocks?
		double rmsdThreshold = 1;
		for (int qi = 0; qi < 100; qi++) {
			int qr = random.nextInt(nif.size());
			Point3d[] x = nif.getPoints(qr);
			float[] xv = nif.getVector(qr);
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
					if (dif > box[d] /*&& d <= 7*/) { // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
						in = false;
					}
				}
				qcp.set(x, y);
				double rmsd = qcp.getRmsd();
				if (in) {
					if (rmsd <= rmsdThreshold) {
						tp++;

					} else {
						fp++;
					}
				} else { // out of box
					if (rmsd <= rmsdThreshold) {
						fn++;
						if (rmsd < rmsdThreshold / 5) {
							System.out.println((in ? "+" : "O") + " rmsd " + rmsd);
							for (int d = 0; d < box.length; d++) {
								double dif = difs[d];
								System.out.println(n(xv[d]) + " - " + n(yv[d]) + " -> " + n(dif) + " " + (dif > box[d] ? "*" : ""));
							}
							System.out.println();
							save(x, y, dirs.getBiwordPair());
							//System.exit(6);
						}
					} else {
						tn++;
					}
				}
			}
		}
		System.out.println("TP = " + tp);
		System.out.println("TN = " + tn);
		System.out.println("FP = " + fp);
		System.out.println("FN = " + fn);
	}

	private static String n(double d) {
		return "" + ((double) Math.round(d * 100)) / 100;
	}
	public static int model = 1;
	public static int serial = 1;

	public static void save(Point3d[] xIn, Point3d[] yIn, File f) {
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
		Point3d[][] points = {x, z};
		System.out.println(x.length);
		System.out.println(z.length);
		try {
			//int serial = 1;
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, true))) {
				bw.write(PdbLine.getModelString(model++));
				bw.newLine();
				for (int k = 0; k < 2; k++) {
					for (int i = 0; i < points[k].length; i++) {
						Point3d p = points[k][i];
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
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void save(Biword[] biwords, File f) {
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
	}

	public void add(int[] vector) {

		for (int d = 0; d < vector.length; d++) {
			Object o = array[0][0];

			//if (array[0] == null)
		}
	}

	public void query(int[] vector) {

	}

}
