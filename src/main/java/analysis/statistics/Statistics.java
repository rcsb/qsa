package analysis.statistics;

import java.util.List;
import java.util.function.Function;

/**
 *
 * @author Antonin Pavelka
 */
public class Statistics {

	public static double difference(List<Double> as, List<Double> bs) {
		if (as.size() != bs.size()) {
			throw new IllegalArgumentException();
		}
		double sum = 0;
		for (int i = 0; i < as.size(); i++) {
			sum += Math.abs(as.get(i) - bs.get(i));
		}
		return sum / as.size();
	}

	public static <T> double average(List<T> array, Function<T, Double> function) {
		double sum = 0;
		for (T t : array) {
			sum += function.apply(t);
		}
		return sum / array.size();
	}
}
