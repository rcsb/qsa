package probability.statistics;

import analysis.statistics.Statistics;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonin Pavelka
 */
public class Distribution {

	private List<Double> values = new ArrayList<>();

	public void add(double value) {
		values.add(value);
	}

	public void printHistogram(int brackets) {
		double minimum = Statistics.getMin(values);
		double maximum = Statistics.getMax(values);
		double range = (maximum - minimum) / brackets;
		int[] counts = new int[brackets];
		for (int b = 0; b < brackets; b++) {
			for (double v : values) {
				double min = b * range + minimum;
				double max = (b + 1) * range + minimum;
				if (min <= v && v <= max) {
					counts[b]++;
				}
			}
		}
		System.out.println(minimum + " - " + maximum);
		for (int b = 0; b < brackets; b++) {
			System.out.println(counts[b] + " ");
		}
	}

}
