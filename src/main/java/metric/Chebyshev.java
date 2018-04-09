package metric;

/**
 *
 * @author Antonin Pavelka
 */
public class Chebyshev {

	public static double distance(float[] x, float[] y) {
		double max = 0;
		for (int dimension = 0; dimension < x.length; dimension++) {
			double difference = Math.abs(x[dimension] - y[dimension]);
			if (difference > max) {
				max = difference;
			}
		}
		return max;
	}

}
