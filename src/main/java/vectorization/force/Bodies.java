package vectorization.force;

import algorithm.primitives.TriangularMatrix;
import analysis.statistics.Distribution2d;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import vectorization.dimension.Dimensions;

public class Bodies implements Iterable<RigidBodyPair> {

	private RigidBodyPair[] bodies;
	private RandomTriangles generator = new RandomTriangles();
	private TriangularMatrix rmsd;
	private double rmsdFactor;
	private Random random = new Random(1);

	public Bodies(int number, double rmsdFactor) {
		this.rmsdFactor = rmsdFactor;
		bodies = new RigidBodyPair[number];
		for (int i = 0; i < number; i++) {
			bodies[i] = generator.generate();
		}
		rmsd = new TriangularMatrix(bodies.length);
		for (int x = 0; x < rmsd.size(); x++ ){
			for (int y = 0; y < x; y++ ) {
				rmsd.set(x, y, (float)bodies[x].rmsdDistance(bodies[y]));
			}
		}
	}

	public int[][] sampleIndexPairs(int n) {
		int[][] sample = new int[2][n];
		for (int i = 0; i < n; i++) {
			int x = 0, y = 0;
			while (x >= y) {
				x = random.nextInt(size());
				y = random.nextInt(size());
			}
			sample[0][i] = x;
			sample[1][i] = y;
		}
		return sample;
	}

	public Distribution2d getPlot(int[][] sampleIndexes) {
		int n = sampleIndexes[0].length;
		Distribution2d plot = new Distribution2d();
		for (int i = 0; i < n; i++) {
			int x = sampleIndexes[0][i];
			int y = sampleIndexes[1][i];
			plot.add(rmsd(x, y), bodies[x].vectorDistance(bodies[y]) / rmsdFactor); // !!!
		}
		return plot;
	}

	public float rmsd(int x, int y) {
		if (x == y) {
			throw new RuntimeException();
		}
		if (x > y) {
			int temp = x;
			x = y;
			y = temp;
		}
		return rmsd.get(x, y);
	}

	public double computeAverageRmsd(RigidBodyPair x, int max) {
		double sum = 0;
		for (int i = 0; i < max; i++) {
			sum += x.rmsdDistance(bodies[i]) * rmsdFactor;
		}
		return sum / max;
	}

	public double computeAverageVectorDistance(RigidBodyPair x, int max) {
		double sum = 0;
		for (int i = 0; i < max; i++) {
			sum += x.vectorDistance(bodies[i]);
		}
		return sum / max;
	}

	public double computeAbsoluteAverageError(RigidBodyPair x, int max) {
		return Math.abs(computeAverageRmsd(x, max) - computeAverageVectorDistance(x, max));
	}

	@Override
	public Iterator<RigidBodyPair> iterator() {
		return Arrays.asList(bodies).iterator();
	}

	public Dimensions getDimensions() {
		return RigidBodyPair.getDimensions();
	}

	public int size() {
		return bodies.length;
	}

	public RigidBodyPair get(int index) {
		return bodies[index];
	}

	public double getWorstAverage() {
		double worst = 0;
		for (RigidBodyPair x : bodies) {
			double d = computeAbsoluteAverageError(x, size());
			if (d > worst) {
				worst = d;
			}
		}
		return worst;
	}

	public double getWorst() {
		double worst = 0;
		for (RigidBodyPair x : bodies) {
			for (RigidBodyPair y : bodies) {
				double e = Math.abs(x.rmsdDistance(y) - x.vectorDistance(y));
				if (e > worst) {
					worst = e;
				}
			}
		}
		return worst;
	}
}
