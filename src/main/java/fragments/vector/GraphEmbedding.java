package fragments.vector;

import geometry.Transformer;
import heatmap.Heatmap;
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
public class GraphEmbedding {

	private final Directories dirs = Directories.createDefault();
	private Point3d[][] base;
	private byte[][] active; // points in a base that are used for RMSD ~ random projection like LSH
	private int dim;
	private final Transformer transformer = new Transformer();

	public GraphEmbedding(Point3d[][] baseObjects) throws Exception {
		this.base = baseObjects;
		this.dim = base.length;
	}

	public final void test(Point3d[][] objects, double cutoff) throws Exception {

		List<Double> rds = new ArrayList<>();
		List<Double> vds = new ArrayList<>();

		int n = objects.length;
		System.out.println(n + " test objects loaded");
		Random random = new Random(2);
		double[][] vectors = wordsToVectors(objects);
		System.out.println(vectors.length + " test objects vectorized");

		Map<Integer, Integer> density = new HashMap<>();
		
		Heatmap hm = new Heatmap(0, 0, 20, 20, 1000, 1000, new File("c:/kepler/data/heatmap/colors.png"));
		

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(dirs.getRealVsVector()))) {
			for (int x = 0; x < n; x++) {
				//System.out.println(x + " / " + n);
				for (int y = 0; y < x; y++) {
					//if (random.nextInt(10) == 0) {
						double vd = vectorDistance(vectors[x], vectors[y]);
						double rd = realDistance(objects[x], objects[y]);
						//if (vd < cutoff + 1 || rd < cutoff + 1) {
						bw.write(rd + "," + vd + "\n");
						hm.add(rd, vd);
						//}
						rds.add(rd);
						vds.add(vd);

					//}
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

	private double realDistance(Point3d[] a, Point3d[] b) {
		transformer.set(a, b);

		return transformer.getRmsd();
		//return transformer.getMaxDifferences();
	}

	private double vectorDistance(double[] x, double[] y) {
		return Vector.chebyshev(x, y);
		//return minkowski(x, y, 15);
	}


}
