package geometry.metric;

/**
 *
 * @author Antonin Pavelka
 */
public class Metric {

	public static double euclidean(float[] x, float[] y) {
		assert x.length == y.length;
		double sum = 0;
		for (int i = 0; i < x.length; i++) {
			float diff = x[i] - y[i];
			sum += diff * diff;
		}
		return Math.sqrt(sum);
	}

	public static double chebyshev(float[] x, float[] y) {
		assert x.length == y.length;
		float max = 0;
		for (int i = 0; i < x.length; i++) {
			float diff = Math.abs(x[i] - y[i]);
			if (diff > max) {
				max = diff;
			}
		}
		return max;
	}
}
