package fragments.vector;

import geometry.Transformer;
import io.Directories;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.vecmath.Point3d;
import util.Randomness;
import util.Timer;

/**
 * @author Antonin Pavelka
 *
 * Each base is composed of two objects, coordinate of C with corresponing to the base AB is CB - CA
 *
 */
public class EfficientGraphEmbedding {

	private final Directories dirs = Directories.createDefault();
	private final Transformer transformer = new Transformer();
	private final Randomness rand;
	private final double threshold = 3;
	private final float[][] matrix;
	private final Point3d[][] bases;
	private final int baseN;

	public EfficientGraphEmbedding(int baseN, Point3d[][] objects, int seed)
		throws Exception {
		this.baseN = baseN;
		rand = new Randomness(1);
		PointVectorClustering.shuffleArray(objects);

		List<Integer[]> uncovered = new ArrayList<>();
		matrix = new float[objects.length][objects.length];
		for (int x = 0; x < objects.length; x++) {
			System.out.println("matrix " + x + " / " + objects.length);
			for (int y = 0; y < x; y++) {
				float d = (float) realDistance(objects[x], objects[y]);
				matrix[x][y] = d;
				matrix[y][x] = d;
				if (d > threshold) {
					Integer[] pair = {x, y};
					uncovered.add(pair);
				}
			}
		}
		uncovered = rand.subsample(100000, uncovered);

		bases = new Point3d[baseN][objects[0].length];

		// TODO create fixed sample of uncovered
		// sample candidates
		// go through all uncovered pairs
		// no removal of bases, assuming objects.length >> baseN
		for (int i = 0; i < baseN; i++) {
			//List<Integer[]> uncoveredSample = subsample(100, uncovered);
			int[] candidates = rand.subsample(1000, objects.length);
			int[] coverCount = new int[candidates.length]; // how many pairs each potential base covers
			for (int ci = 0; ci < candidates.length; ci++) { // how good is this candidate?
				int candidate = candidates[ci];
				for (int ui = 0; ui < uncovered.size(); ui++) {
					Integer[] u = uncovered.get(ui); // uncovered pair, does the potential base pair cover it?					
					if (isSeparated(u, candidate)) {
						coverCount[ci]++;
					}
				}
			}

			// select potential base pair with greatest cover count
			int max = Integer.MIN_VALUE;
			int bestBase = -1;
			for (int ci = 0; ci < candidates.length; ci++) {
				//System.out.println("cover count " + coverCount[ci]);
				if (coverCount[ci] > max) {
					max = coverCount[ci];
					bestBase = ci;
				}
			}
			//bestBase = rand.next(candidates.length);
			System.out.println("max " + max + " (" + i + ")");
			int baseIndex = candidates[bestBase];
			bases[i] = objects[baseIndex];

			// remove pairs covered by the new base
			for (int u = uncovered.size() - 1; u >= 0; u--) {
				if (isSeparated(uncovered.get(u), baseIndex)) {
					uncovered.remove(u);
				}
			}
			System.out.println("uncovered " + uncovered.size());
		}
	}

	private float matrix(int x, int y) {
		return matrix[x][y];
	}

	private boolean isSeparated(Integer[] pair, int byBase) {
		double dif = Math.abs(matrix(pair[0], byBase) - matrix(pair[1], byBase));
		return (dif >= threshold);
	}

	public final void test(Point3d[][] objects, double cutoff, int seed) throws Exception {
		List<Double> rds = new ArrayList<>();
		List<Double> vds = new ArrayList<>();
		int n = objects.length;
		//System.out.println("words loaded");
		n = objects.length;
		Random random = new Random(seed);
		double[][] vectors = wordsToVectors(objects);
		//System.out.println("words vectorized");
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(dirs.getRealVsVector()))) {
			for (int x = 0; x < n; x++) {
				//System.out.println(x + " / " + n);
				for (int y = 0; y < x; y++) {
					if (random.nextInt(100) == 0) {
						double vd = vectorDistance(vectors[x], vectors[y]);
						double rd = realDistance(objects[x], objects[y]);
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
		int tp = 0;
		int fp = 0;
		for (int i = 0; i < rds.size(); i++) {
			double rd = rds.get(i);
			double vd = vds.get(i);
			if (vd <= cutoff) {
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
	
	private void evaluateMax(List<Double> rds, List<Double> vds, double cutoff) {
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

	private double[] vectorize(Point3d[] object) {
		double[] v = new double[baseN];
		for (int i = 0; i < baseN; i++) {
			v[i] = realDistance(object, bases[i]);
		}
		return v;
	}

	private double[][] wordsToVectors(Point3d[][] words) {
		double[][] vectors = new double[words.length][baseN];
		for (int i = 0; i < words.length; i++) {
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
