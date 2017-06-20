package fragments.vector;

import geometry.Transformer;
import io.Directories;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.vecmath.Point3d;
import util.Timer;

/**
 * @author Antonin Pavelka
 *
 * Lipschitz embedding, where each set is a singleton. Probably best choice of singletons are cluster representatives.
 *
 */
public class GraphSetEmbedding {

	private final Directories dirs = Directories.createDefault();
	private Point3d[][][] base; // [base id][set id][set content]
	private final Transformer transformer = new Transformer();
	private final Random random;
	private File baseFile;
	private int baseN;
	private int setN;
	private int repreN;

	public GraphSetEmbedding(File baseFile, int repreN, int baseN, int setN, int seed) throws Exception {
		random = new Random(seed);
		this.baseN = baseN;
		this.setN = setN;
		this.repreN = repreN;
		this.baseFile = baseFile;
		Point3d[][] repre = PointVectorDataset.read(baseFile, repreN);
		PointVectorClustering.shuffleArray(repre);
		repreN = Math.min(repreN, repre.length);
		this.repreN = repreN;
		if (baseN > repreN) {
			this.baseN = repreN;
			baseN = repreN;
		}
		System.out.println("Using base " + baseN);
		int points = repre[0].length;
		base = new Point3d[baseN][setN][points];
		List<Integer> indeces = new ArrayList<>(repreN);
		for (int i = 0; i < repreN; i++) {
			indeces.add(i);
		}
		for (int i = 0; i < baseN; i++) {
			for (int j = 0; j < setN; j++) {
				int r = indeces.remove(random.nextInt(indeces.size()));
				base[i][j] = repre[r];
			}
		}
	}

	public final void test(File testFile, int max, double cutoff, int seed) throws Exception {

		List<Double> rds = new ArrayList<>();
		List<Double> vds = new ArrayList<>();

		int n = max;
		Point3d[][] words = PointVectorDataset.read(testFile, n);
		System.out.println("words loaded");
		n = words.length;
		Random random = new Random(seed);
		double[][] vectors = wordsToVectors(words);
		System.out.println("words vectorized");

		Map<Integer, Integer> density = new HashMap<>();

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(dirs.getRealVsVector()))) {
			for (int x = 0; x < n; x++) {
				System.out.println(x + " / " + n);
				for (int y = 0; y < x; y++) {
					//if (random.nextInt(300) == 0) {
					if (random.nextInt(10) == 0) {
						double vd = vectorDistance(vectors[x], vectors[y]);
						double rd = realDistance(words[x], words[y]);
						bw.write(rd + "," + vd + "\n");
						rds.add(rd);
						vds.add(vd);

					}
					/*if (random.nextInt(1000) == 0
						|| (vd < 4 && random.nextInt(100) == 0)
						|| (vd < 3 && random.nextInt(10) == 0)
						|| (vd < 2)) {
						double rd = realDistance(words[x], words[y]);
						bw.write(rd + "," + vd + "\n");
					}*/
				}
			}
		}
		evaluate(rds, vds, cutoff);
	}

	public final void measureSpeed() throws Exception {
		int n = 1000;
		Point3d[][] words = PointVectorDataset.read(dirs.getWordDatasetShuffled(), n);
		System.out.println("words loaded");
		n = words.length;
		Random random = new Random(1);
		double[][] vectors = wordsToVectors(words);
		System.out.println("words vectorized");

		Timer.start();
		for (int x = 0; x < n; x++) {
			for (int y = 0; y < x; y++) {
				vectorDistance(vectors[x], vectors[y]);
			}
		}
		Timer.stop();
		System.out.println("vectors time: " + Timer.get());

		Timer.start();
		for (int x = 0; x < n; x++) {
			for (int y = 0; y < x; y++) {
				realDistance(words[x], words[y]);

			}
		}
		Timer.stop();
		System.out.println("qcp time: " + Timer.get());
	}

	private void evaluate(List<Double> rds, List<Double> vds, double cutoff) {
		double maxVd = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < rds.size(); i++) {
			double rd = rds.get(i);
			double vd = vds.get(i);
			if (rd <= cutoff && vd > maxVd) {
				maxVd = vd;
			}
		}
		int tp = 0;
		int fp = 0;
		for (int i = 0; i < rds.size(); i++) {
			double rd = rds.get(i);
			double vd = vds.get(i);
			if (vd <= maxVd) {
				if (rd <= cutoff) {
					tp++;
				} else {
					fp++;
				}
			}
		}
		double recall = (double) (tp + fp) / rds.size();
		System.out.println("to process: " + recall + " (" + tp + " + " + fp + ")");
		double garbage = (double) fp / (tp + fp);
		System.out.println("garbage = " + garbage);
	}

	private double[] vectorize(Point3d[] word) {
		double[] v = new double[baseN];
		for (int i = 0; i < baseN; i++) {
			double min = Double.POSITIVE_INFINITY;
			for (int j = 0; j < setN; j++) {
				double d = realDistance(word, base[i][j]);
				if (d < min) {
					min = d;
				}
				v[i] = min;
			}
		}
		return v;
	}

	private double[][] wordsToVectors(Point3d[][] words) {
		double[][] vectors = new double[words.length][baseN];
		for (int i = 0; i < words.length; i++) {
			System.out.println("vectorizing " + i + " / " + words.length);
			vectors[i] = vectorize(words[i]);
		}
		return vectors;
	}

	private double realDistance(Point3d[] a, Point3d[] b) {
		transformer.set(a, b);
		return transformer.getRmsd();
		//return transformer.getSumOfDifferences();
	}

	private double vectorDistance(double[] x, double[] y) {
		return chebyshev(x, y);
		//return minkowski(x, y, 0.2);
	}

	private double chebyshev(double[] x, double[] y) {
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < x.length; i++) {
			double d = Math.abs(x[i] - y[i]);
			if (d > max) {
				max = d;
			}
		}
		return max;
	}

	private double manhattan(double[] x, double[] y) {
		double f = 0;
		for (int d = 0; d < x.length; d++) {
			f += Math.abs(x[d] - y[d]);
		}
		f /= x.length;
		return f;
	}

	private double minkowski(double[] x, double[] y, double p) {
		double sum = 0;
		for (int i = 0; i < baseN; i++) {
			double d = Math.pow(Math.abs(x[i] - y[i]), p);
			sum += d;
		}
		return Math.pow(sum, 1.0 / p);
	}

}
