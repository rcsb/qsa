package fragments.vector;

import geometry.Transformer;
import io.Directories;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.vecmath.Point3d;
import util.Timer;

/**
 * @author Antonin Pavelka
 *
 * Each base is composed of two objects, coordinate of C with corresponing to the base AB is CB - CA
 *
 */
public class EfficientGraphEmbedding {

	private final Directories dirs = Directories.createDefault();
	private Point3d[][] base;
	private final Transformer transformer = new Transformer();
	private final Random random;
	private File baseFile;
	private int baseN;
	private double threshold = 3;

	public EfficientGraphEmbedding(File baseFile, int repreN, int baseN, int seed) throws Exception {
		random = new Random(seed);
		this.baseN = baseN;
		this.baseFile = baseFile;
		Point3d[][] repre = PointVectorDataset.read(baseFile, repreN);
		PointVectorClustering.shuffleArray(repre);
		repreN = Math.min(repreN, repre.length);
		if (baseN > repreN / 2) {
			this.baseN = repreN / 2;
			baseN = repreN / 2;
		}
		System.out.println("Read " + repre.length + " representative objects.");
		System.out.println("Using base " + baseN);
		int pointN = repre[0].length;
		base = new Point3d[baseN][pointN];
		List<Integer> indeces = new ArrayList<>(repreN);
		for (int i = 0; i < repreN; i++) {
			indeces.add(i);
		}

		List<Integer[]> uncovered = new ArrayList<>();
		float[][] matrix = new float[repre.length][repre.length];
		for (int x = 0; x < repre.length; x++) {
			System.out.println("matrix " + x + " / " + repre.length);
			for (int y = 0; y < x; y++) {
				float d = (float) realDistance(repre[x], repre[y]);
				matrix[x][y] = d;
				matrix[y][x] = d;
				if (d > threshold) {
					Integer[] pair = {x, y};
					uncovered.add(pair);
				}
			}
		}

		// build sample of uncovered pairs, which are not covered by previous bases
		// find pair that covers most of them - best next base
		for (int i = 0; i < baseN; i++) {
			// to be able to randomly select uncovered pairs
			Collections.shuffle(uncovered);
			List<Integer> potentialBases = generateSample(1000, repre.length);
			int[] coverCount = new int[potentialBases.size()]; // how many pairs each potential base covers
			for (int bi = 0; bi < potentialBases.size(); bi++) {
				Integer base = potentialBases.get(bi);
				for (int ui = 0; ui < Math.min(1000, uncovered.size()); ui++) {
					Integer[] u = uncovered.get(ui); // uncovered pair, does the potential base pair cover it?

					//double du = matrix[u[0]][u[1]];
					double dif = matrix[base][u[0]] - matrix[base][u[1]];
					if (dif >= threshold) {
						coverCount[bi]++;
					}
				}
			}
			// select potential base pair with greatest cover count
			int max = Integer.MIN_VALUE;
			int baseIndex = -1;
			for (int bi = 0; bi < potentialBases.size(); bi++) {
				if (coverCount[bi] > max) {
					max = coverCount[bi];
					baseIndex = bi;
				}
			}
			base[i] = repre[potentialBases.get(baseIndex)];
			// expand uncovered // TODO avoid testing bases with both covered?
			for (int u = uncovered.size() - 1; u >= 0; u--) {
				double dif = matrix[base[i]][u[0]] - matrix[base[i]][u[1]];
			}
			
		}
	}

	private List<Integer> generateSample(int n, int max) {
		List<Integer> numbers = new ArrayList<>();
		List<Integer> sample = new ArrayList<>();
		for (int i = 0; i < max; i++) {
			numbers.add(i);
		}
		for (int i = 0; i < n; i++) {
			sample.add(numbers.remove(random.nextInt(numbers.size())));
		}
		return sample;
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
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(dirs.getRealVsVector()))) {
			for (int x = 0; x < n; x++) {
				System.out.println(x + " / " + n);
				for (int y = 0; y < x; y++) {
					if (random.nextInt(100) == 0) {
						double vd = vectorDistance(vectors[x], vectors[y]);
						double rd = realDistance(words[x], words[y]);
						bw.write(rd + "," + vd + "\n");
						rds.add(rd);
						vds.add(vd);
					}
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
			double[] d = new double[2];
			for (int j = 0; j < 2; j++) {
				d[j] = realDistance(word, base[i][j]);
			}
			v[i] = d[1] - d[0];
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
