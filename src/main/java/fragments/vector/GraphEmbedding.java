package fragments.vector;

import geometry.Transformer;
import heatmap.Heatmap;
import io.Directories;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import static java.lang.Math.random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.vecmath.Point3d;
import util.Randomness;
import util.Timer;

/**
 * @author Antonin Pavelka
 *
 * Lipschitz embedding, where each set is a singleton. Probably best choice of singletons are cluster representatives.
 *
 */
public class GraphEmbedding {

	private final Directories dirs = Directories.createDefault();
	private final Point3d[][] base;
	private final byte[][] projection; // points in a base that are used for RMSD ~ random projection like LSH
	private final int dim;
	private final Transformer transformer = new Transformer();

	public GraphEmbedding(Point3d[][] baseObjects) throws Exception {
		this.base = baseObjects;
		this.dim = base.length;
		int minA = 12;
		int minB = 10;
		int pointN = 20;
		Randomness rand = new Randomness(12);
		projection = new byte[base.length][];
		for (int i = 0; i < base.length; i++) {
			int n;
			if (rand.next(10) == 0) {
				n = rand.next(pointN - minA) + minA;
			} else {
				n = rand.next(pointN - minB) + minB;
			}
			projection[i] = new byte[n];
			int[] p = rand.subsample(n, pointN);
			//System.out.println(n);
			for (int j = 0; j < n; j++) {
				projection[i][j] = (byte) p[j];
				//System.out.print(b[j] + " ");
			}
			//System.out.println();
		}
	}

	public final void test(Point3d[][] objects, double cutoff) throws Exception {
		List<Double> rds = new ArrayList<>();
		List<Double> vds = new ArrayList<>();
		int n = objects.length;
		Random random = new Random(1);
		System.out.println(n + " test objects loaded");
		double[][] vectors = wordsToVectors(objects);
		System.out.println(vectors.length + " test objects vectorized");
		int maxX = 20;
		int maxY = 20;
		int scale = 50;
		Heatmap hm = new Heatmap(0, 0, maxX, maxY, maxX * scale, maxY * scale, new File("c:/kepler/data/heatmap/colors.png"));
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(dirs.getRealVsVector()))) {
			for (int x = 0; x < n; x++) {
				for (int y = 0; y < x; y++) {
					//if (random.nextInt(10) == 0) {
					double vd = vectorDistanceWithGridError(vectors[x], vectors[y]);
					//double vd = vectorDistance(vectors[x], vectors[y]);
					//if (vd <= maxY) {
					double rd = realDistance(objects[x], objects[y]);
					//if (random.nextInt(1000) == 0) {
					//bw.write(rd + "," + vd + "\n");
					//}
					hm.add(rd, vd);
					rds.add(rd);
					vds.add(vd);
					//}
					//}
				}
			}
		}
		hm.save(new File("c:/kepler/data/heatmap/heatmap.png"));
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
		System.out.println("TP/total = " + (double) tp / rds.size());
		System.out.println("FP/total = " + (double) fp / rds.size());
	}

	private void evaluateRelative(List<Double> rds, List<Double> vds, double cutoff) {
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
		double[] v = new double[dim];
		for (int i = 0; i < dim; i++) {
			//v[i] = realDistanceProjected(word, base[i], projection[i]);
			v[i] = realDistance(word, base[i]);
		}
		return v;
	}

	private double[][] wordsToVectors(Point3d[][] words) {
		double[][] vectors = new double[words.length][dim];
		for (int i = 0; i < words.length; i++) {
			vectors[i] = vectorize(words[i]);
		}
		return vectors;
	}

	private Point3d[] project(Point3d[] a, byte[] projection) {
		Point3d[] b = new Point3d[projection.length];
		for (int i = 0; i < projection.length; i++) {
			b[i] = a[projection[i]];
			//System.out.println("ppp " + projection[i]);
		}
		return b;
	}

	private double realDistanceProjected(Point3d[] a, Point3d[] b, byte[] projection) {
		Point3d[] pa = project(a, projection);
		Point3d[] pb = project(b, projection);
		//System.out.println(pa.length + " " + pb.length);
		//for (int i = 0; i < projection.length;i++){
		//	System.out.println(pa[i] + " " + pb[i]);
		//}
		//System.out.println("---");

		transformer.set(pa, pb);
		double rmsd = transformer.getRmsd();
		//System.out.println("rsmd = " + rmsd);
		return rmsd;
	}

	private double realDistance(Point3d[] a, Point3d[] b) {
		transformer.set(a, b);
		return transformer.getRmsd();
		//return transformer.getMaxDifferences();
	}

	private static double grid = 20.0 / 1000;

	private double vectorDistanceWithGridError(double[] x, double[] y) {
		double diffs[] = new double[x.length];
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < dim; i++) {
			double yi = y[i];
			double a = Math.floor(yi / grid) * grid;
			double b = Math.ceil(yi / grid) * grid;
			double xi = x[i];
			double da = Math.abs(a - xi);
			double db = Math.abs(b - xi);
			double d = da < db ? da : db;
			if (d > max) {
				max = d;
			}
			diffs[i] = Math.abs(xi - yi);
		}
		Arrays.sort(diffs);
		/*System.out.println("d(" + max + "): ");
		for (double d : diffs) {
			System.out.println(d + " ");
		}
		System.out.println();*/
		return max;
	}

	private double vectorDistance(double[] x, double[] y) {
		return Vector.chebyshev(x, y);
	}

}
