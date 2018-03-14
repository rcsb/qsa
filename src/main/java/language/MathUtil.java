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
}
