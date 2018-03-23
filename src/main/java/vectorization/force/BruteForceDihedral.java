package vectorization.force;

import analysis.statistics.Csv;
import analysis.statistics.Distribution2d;
import java.io.File;
import java.util.Random;
import vectorization.dimension.DimensionCyclic;

/**
 *
 * @author Antonin Pavelka
 */
public class BruteForceDihedral {

	// TODO
	/* In the end/start, go from worst cases and optimize their error in ALL coordinates
	 *
	 */
	private int accuracy = 100;
	private int objects = 100;
	private double factor = 2;
	private Bodies bodies = new Bodies(objects, factor);
	private double min, max;

	// try to use two closest for initial optimization?
	public BruteForceDihedral() {
	}

	private void run() {
		DimensionCyclic dc = (DimensionCyclic) bodies.getDimensions().getLastDimension();
		min = dc.getMin();
		max = dc.getMax();
		int[][] sampleIndexes = bodies.sampleIndexPairs(20000);
		Distribution2d plotBefore = bodies.getPlot(sampleIndexes);

		print("before");
		/*System.out.println(bodies.get(51).vectorDistance(bodies.get(52)));
		System.out.println(bodies.get(50).vectorDistance(bodies.get(51)));
		printVector(bodies.get(0).getVector());
		printVector(bodies.get(1).getVector());
		 */
		for (int i = 0; i < bodies.size(); i++) {
			optimize(bodies.get(i), i);
			if (i % 10 == 1) {
				/*for (int k = 0; k < bodies.size(); k++) {
					optimize(bodies.get(k), bodies.size());
				}*/
			}
		}
		print("after");

		for (int k = 0; k < 2; k++) {
			for (int i = 0; i < bodies.size(); i++) {
				optimize(bodies.get(i), bodies.size());
			}
			print("after");
		}
		/*		System.out.println(bodies.get(51).vectorDistance(bodies.get(52)));
		System.out.println(bodies.get(50).vectorDistance(bodies.get(51)));
		printVector(bodies.get(0).getVector());
		printVector(bodies.get(1).getVector());*/
		Distribution2d plotAfter = bodies.getPlot(sampleIndexes);
		savePlots(plotBefore, plotAfter);
	}

	private void printVector(float[] v) {
		for (int i = 0; i < v.length; i++) {
			System.out.print(v[i] + " ");
		}
		System.out.println();
	}

	private void print(String s) {
		System.out.println("");
		System.out.println(s);
		System.out.println(bodies.getWorstAverage());
		System.out.println(bodies.getWorst());
		System.out.println("--");
	}

	private void savePlots(Distribution2d before, Distribution2d after) {
		Random random = new Random();
		Csv csv = new Csv(new File("e:/data/qsa/plots/plot_" + random.nextInt() + ".csv"));
		csv.save2d(before, after);
	}

	private void optimize(RigidBodyPair object, int numberOfOptimized) {
		double bestError = Double.MAX_VALUE;
		Float bestDihedral = null;
		for (int i = 0; i < accuracy; i++) {
			float dihedral = (float) (min + i * (max - min) / accuracy);
			object.setDihedral(dihedral);
			double error = bodies.computeAbsoluteAverageError(object, numberOfOptimized);
			if (error < bestError) {
				bestError = error;
				bestDihedral = dihedral;
			}
		}
		if (bestDihedral != null) { // not first one
			object.setDihedral(bestDihedral);
		}
	}

	public static void main(String[] args) {
		BruteForceDihedral m = new BruteForceDihedral();
		m.run();
	}
}
