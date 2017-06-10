package fragments.vector;

import geometry.Transformer;
import io.Directories;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.vecmath.Point3d;
import pdb.PdbLine;

public class Points {

	private final int dim = 6;
	//private final int objectDim = 100;
	private double[][] vectors;
	private Point3d[][] words;
	private int size;
	private Random random = new Random(2);
	private final Directories dirs = Directories.createDefault();
	private final Transformer transformer = new Transformer();
	private double totalError = 0;
	private long totalCounter = 0;

	public Points() throws Exception {
		size = 0;
		words = WordDataset.readWords(dirs.getWordRepresentants());
		vectors = new double[words.length][dim];
		for (Point3d[] w : words) {
			add(w);
		}
		System.out.println("VECTORS");
		double m = 10;
		double[][] errors = new double[vectors.length][vectors.length];
		for (int x = 0; x < errors.length; x++) {
			for (int y = 0; y < x; y++) {
				errors[x][y] = Math.abs(rmsd(words[x], words[y]) - euclidean(vectors[x], vectors[y]));
			}
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter("c:/kepler/rozbal/vectors.pdb"))) {
			int serial = 1;
			for (int i = 0; i < vectors.length; i++) {
				double[] v = vectors[i];
				Point3d[] word = words[i];
				boolean first = true;
				for (int j = 0; j < word.length; j++) {
					Point3d a = word[j];
					PdbLine pl = new PdbLine(serial, "C", "C", "ALA", "" + serial, 'A',
						v[0] * m + a.x, v[1] * m + a.y, v[2] * m + a.z);
					double worst = 0;
					for (double e : errors[i]) {
						if (worst < e) {
							worst = e;
						}
					}
					pl.setTemperatureFactor(worst);
					bw.write(pl.toString() + "\n");
					if (!first) {
						bw.write(PdbLine.getConnectString(serial - 1, serial) + "\n");
					} else {
						first = false;
					}
					serial++;
				}
			}
		}

	}

	private double[] center(double[][] points) {
		int n = points.length;
		double[] c = new double[points[0].length];
		for (int x = 0; x < n; x++) {
			for (int y = 0; y < c.length; y++) {
				c[y] += points[x][y];
			}
		}
		for (int i = 0; i < c.length; i++) {
			c[i] /= n;
		}
		return c;
	}

	private void init(double[] x, double[] distantSample, double maxRmsd) {
		for (int i = 0; i < dim; i++) {
			x[i] = distantSample[i] + (random.nextDouble() - 0.5) * maxRmsd * 1.5;
		}
	}

	private void first(double[] x) {
		for (int i = 0; i < dim; i++) {
			x[i] = 0;
		}
	}

	public void plus(double[] x, final double[] y) {
		for (int i = 0; i < x.length; i++) {
			x[i] += y[i];
		}
	}

	public void minus(double[] x, final double[] y) {
		for (int i = 0; i < x.length; i++) {
			x[i] -= y[i];
		}
	}

	public double[] vector(final double[] x, final double[] y) {
		double[] v = new double[dim];
		for (int i = 0; i < x.length; i++) {
			v[i] = y[i] - x[i];
		}
		return v;
	}

	public void divide(double[] x, double d) {
		for (int i = 0; i < x.length; i++) {
			x[i] /= d;
		}
	}

	public void multiply(double[] x, double d) {
		for (int i = 0; i < x.length; i++) {
			x[i] *= d;
		}
	}

	public double size(double[] v) {
		double sum = 0;
		for (int i = 0; i < v.length; i++) {
			sum += v[i] * v[i];

		}
		return (double) Math.sqrt(sum);
	}

	public void finite(double[] fs) {
		for (double f : fs) {
			assert Double.isFinite(f);
		}
	}

	private String s(double[] fs) {
		StringBuilder sb = new StringBuilder("[");
		for (double f : fs) {
			sb.append(f).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("]");
		return sb.toString();
	}

	public String nice(double d) {
		return "" + (double) Math.round(d * 100) / 100;
	}

	public double rmsd(Point3d[] a, Point3d[] b) {
		transformer.set(a, b);
		return transformer.getRmsd();
	}

	public double euclidean(double[] x, double[] y) {
		double f = 0;
		for (int d = 0; d < x.length; d++) {
			double dif = x[d] - y[d];
			f += dif * dif;
		}
		f = (double) Math.sqrt(f);
		return f;
	}

	// try to add point, might not be added or might replace
	// procedure - pick one, pick furthest, pick furthest to both ...
	
	
	// GLOBAL opt. - pick the wost connection and move both, to improve globally
	// in case of ambiguity during initialization, remember both?
	public void add(Point3d[] word) {
		double[] vector = new double[dim];
		//int vectorI = size();

		int iteration = 0;
		double min = Double.MAX_VALUE;
		double[] winner = vector;

		// make sure each part of space is represented when measuring, kindof acceleration and accuracy
		/// MAX error matters most?
		/// refuse to do big mistakes? put them in separate space?
		/// !!! several trees, might help, is it combination of max and eucl? no
		/// trees - st. like clusters, with internal fine division
		// ~~~~~~~~~~~~~~~~~~~ compute rmsd just once per adddition, optimize vs fixed points
		// descent for each random
		int sampleN = Math.min(size, 10);
		//Point3d[][] sample = new Point3d[sampleN][];
		double[][] sampleVectors = new double[sampleN][];
		Set<Integer> sampled = new HashSet<>();
		double[] rmsds = new double[sampleN];
		double[] distantSample = null;
		double maxRmsd = 0;
		for (int i = 0; i < sampleN; i++) {
			int r;
			while (sampled.contains(r = random.nextInt(words.length))) {
				sampled.add(r);
			}
			sampleVectors[i] = vectors[r];
			rmsds[i] = rmsd(word, words[r]);
			if (maxRmsd < rmsds[i]) {
				maxRmsd = rmsds[i];
				distantSample = vectors[r];
			}
		}
		while (iteration < 1000 * 1000 * 100) {
			if (size == 0) {
				first(vector);
			} else {
				init(vector, distantSample, maxRmsd);
			}
			double worst = 0;
			for (int i = 0; i < sampleN; i++) {
				double e = Math.abs(euclidean(vector, sampleVectors[i]) - rmsds[i]);
				if (e > worst) {
					worst = e;
				}
			}
			iteration++;
			if (worst < min) {
				min = worst;
				winner = new double[vector.length];
				System.arraycopy(vector, 0, winner, 0, vector.length);
			}
		}
		if (size > 0) {
			totalError += min;
			totalCounter++;
		}
		vectors[size] = winner;
		words[size] = word;
		size++;
		System.out.println("avg " + nice(totalError / totalCounter) + " worst "
			+ nice(min));

	}

}
