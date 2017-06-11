package fragments.vector;

import geometry.Transformer;
import io.Directories;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 *
 * try graph embedding for protein pieces, arbitrary function to compute distance of two vectors graph? distances of
 * words in sequence order
 *
 */
public class GraphEmbedding {

	private final Directories dirs = Directories.createDefault();
	private Point3d[][] base;
	private int dim;
	private final Transformer transformer = new Transformer();

	public GraphEmbedding() throws Exception {
		base = WordDataset.readWords(dirs.getWordRepresentants("2"), 250);
		dim = base.length;
	}

	private final void test() throws Exception {

		List<Double> rds = new ArrayList<>();
		List<Double> vds = new ArrayList<>();

		int n = 10000;
		Point3d[][] words = WordDataset.readWords(dirs.getWordDataset(), n);
		n = words.length;
		Random random = new Random(1);
		double[][] vectors = wordsToVectors(words);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(dirs.getRealVsVector()))) {
			for (int x = 0; x < n; x++) {
				for (int y = 0; y < x; y++) {
					if (random.nextInt(1000) == 0) {
						double rd = realDistance(words[x], words[y]);
						double vd = vectorDistance(vectors[x], vectors[y]);
						rds.add(rd);
						vds.add(vd);
						bw.write(rd + "," + vd + "\n");
					}
				}
			}
		}
		evaluate(rds, vds);
	}

	private void evaluate(List<Double> rds, List<Double> vds) {
		double cutoff = 1.5; // real distance
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
		System.out.println("to process: " + recall);
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

	public double realDistance(Point3d[] a, Point3d[] b) {
		transformer.set(a, b);
		return transformer.getRmsd();
		//return transformer.getSumOfDifferences();
	}

	public double vectorDistance(double[] x, double[] y) {
		//return manhattan(x, y);
		return minkowski(x, y,12);
		//return chebyshev(x, y);
	}

	public double chebyshev(double[] x, double[] y) {
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < x.length; i++) {
			double d = Math.abs(x[i] - y[i]);
			if (d > max) {
				max = d;
			}
		}
		return max;
	}

	public double manhattan(double[] x, double[] y) {
		double f = 0;
		for (int d = 0; d < x.length; d++) {
			f += Math.abs(x[d] - y[d]);
		}
		f /= x.length;
		return f;
	}

	public double minkowski(double[] x, double[] y, double p) {
		double sum = 0;
		for (int i = 0; i < dim; i++) {
			double d = Math.pow(Math.abs(x[i] - y[i]), p);
			sum += d;
		}
		return Math.pow(sum, 1.0 / p);
	}

	public static void main(String[] args) throws Exception {
		GraphEmbedding m = new GraphEmbedding();
		m.test();
	}
}
