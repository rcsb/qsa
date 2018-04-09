package metric;

import vectorization.dimension.Dimensions;

/**
 *
 * @author Antonin Pavelka
 */
public class LpSpace {

	private Dimensions dimensions;

	public LpSpace(Dimensions dimensions) {
		this.dimensions = dimensions;
	}

	public double euclidean(float[] x, float[] y) {
		assert correctDimensionality(x, y);
		double sum = 0;
		for (int dimension = 0; dimension < x.length; dimension++) {
			double difference = dimensions.computeDifference(dimension, x[dimension], y[dimension]);
			sum += difference * difference;
		}
		return Math.sqrt(sum);
	}

	public double chebyshev(float[] x, float[] y) {
		assert correctDimensionality(x, y);
		double max = 0;
		for (int dimension = 0; dimension < x.length; dimension++) {
			double difference = dimensions.computeDifference(dimension, x[dimension], y[dimension]);
			if (difference > max) {
				max = difference;
			}
		}
		return max;
	}

	private boolean correctDimensionality(float[] x, float[] y) {
		assert x.length == y.length;
		assert dimensions.number() == x.length : dimensions.number() + " " + x.length;
		return true;
	}
}
