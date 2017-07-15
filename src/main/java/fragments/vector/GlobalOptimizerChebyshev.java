package fragments.vector;

import geometry.Transformer;
import io.Directories;
import javax.vecmath.Point3d;
import util.Randomness;

/**
 *
 * @author Antonin Pavelka
 */
public class GlobalOptimizerChebyshev {

	private final Directories dirs = Directories.createDefault();
	private final Transformer transformer = new Transformer();
	private final Randomness rand;
	private final float[][] matrix;
	private final int dim = 100;
	private final double[][] vectors;

	public GlobalOptimizerChebyshev() throws Exception {
		Point3d[][] objects = PointVectorDataset.read(dirs.getBiwordRepresentants(5.0), 4000);
		System.out.println("Objects: " + objects.length);
		/*for (Point3d[] p : objects) {
			System.out.println("---");
			for (Point3d x : p) {
				System.out.println(x);
			}
		}*/
		rand = new Randomness(1);
		PointVectorClustering.shuffleArray(objects);
		matrix = new float[objects.length][objects.length];
		for (int x = 0; x < objects.length; x++) {
			for (int y = 0; y < x; y++) {
				float d = (float) realDistance(objects[x], objects[y]);
				matrix[x][y] = d;
				matrix[y][x] = d;
			}
		}

		vectors = new double[objects.length][dim];
		for (int i = 0; i < vectors.length; i++) {
			for (int d = 0; d < dim; d++) {
				vectors[i][d] = rand.nextDouble(10);
			}
		}

		double tension = Double.POSITIVE_INFINITY;
		while (tension > 0.001) {
			tension = move();
		}
	}

	private double move() {
		double avg = 0;
		double tension = 0;
		int tensionCounter = 0;
		double maxTension = Double.NEGATIVE_INFINITY;
		for (int xi = 0; xi < matrix.length; xi++) {
			double[] x = vectors[xi];
			double[] force = new double[dim];
			int counter = 0;
			for (int yi = 0; yi < matrix[xi].length; yi++) {
				if (xi == yi) {
					continue;
				}
				double[] y = vectors[yi];
				double maxError = Double.NEGATIVE_INFINITY;
				int maxZ = -1;
				for (int z = 0; z < dim; z++) {
					double d = Math.abs(Math.abs(y[z] - x[z]) - matrix[xi][yi]);
					if (d > maxError) {
						maxError = d;
						maxZ = z;
					}
				}
				// TODO try different directions and always test if it helps?
				// 
				
				double[] f = new double[dim];
				double direction = Math.abs(x[maxZ] - y[maxZ]) / (x[maxZ] - y[maxZ]) * -1; 
				double change = (matrix[xi][yi] - Math.abs(y[maxZ] - x[maxZ])) * direction;
				f[maxZ] = change;
				
				// bug or principal?
				// try random changes?
				
				//System.out.println(matrix[xi][yi] + " " + change);
				double t = Math.abs(change);
				tension += t;
				tensionCounter++;
				if (maxTension < t) {
					maxTension = t;
				}
				Vector.add(force, f);
				counter++;
			}
			force = Vector.divide(force, counter);
			avg += Vector.size(force);
			Vector.add(x, force);
		}
		double movement = avg / vectors.length;
		double relativeTension = tension / tensionCounter;
		System.out.println("m " + movement + " mt " + maxTension + " t " + relativeTension);
		return movement;
	}

	private double realDistance(Point3d[] a, Point3d[] b) {
		transformer.set(a, b);
		return transformer.getRmsd();
		//return transformer.getSumOfDifferences();
	}

	private double vectorDistance(double[] x, double[] y) {
		//return Vector.euclidean(x, y);
		return Vector.chebyshev(x, y);
		//return Vector.manhattan(x, y);
		//return Vector.minkowski(x, y, 3);
	}

	public static void main(String[] args) throws Exception {
		GlobalOptimizerChebyshev m = new GlobalOptimizerChebyshev();

	}

}
