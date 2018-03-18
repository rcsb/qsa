package language;

/**
 *
 * @author Antonin Pavelka
 */
public class MathUtil {

	/**
	 * @param min Low interval bound
	 * @param max High interval bound
	 * @param value Number inside the interval with added integer product of (max - min)
	 * @return The original value of the following operation: original + (max - min) * integer.
	 */
	public static double wrap(double value, double min, double max) {
		double size = max - min;
		double phase = (value - min) / size;
		double shiftCount = Math.floor(phase);
		return value - (shiftCount * size);
	}

	public static double correlation(double[] xs, double[] ys) {

		double sx = 0.0;
		double sy = 0.0;
		double sxx = 0.0;
		double syy = 0.0;
		double sxy = 0.0;

		int n = xs.length;

		for (int i = 0; i < n; ++i) {
			double x = xs[i];
			double y = ys[i];

			sx += x;
			sy += y;
			sxx += x * x;
			syy += y * y;
			sxy += x * y;
		}

		// covariation
		double cov = sxy / n - sx * sy / n / n;
		// standard error of x
		double sigmax = Math.sqrt(sxx / n - sx * sx / n / n);
		// standard error of y
		double sigmay = Math.sqrt(syy / n - sy * sy / n / n);

		// correlation is just a normalized covariation
		return cov / sigmax / sigmay;
	}
}
