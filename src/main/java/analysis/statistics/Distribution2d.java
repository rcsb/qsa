package analysis.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class Distribution2d {

	private final List<Double> xs = new ArrayList<>();
	private final List<Double> ys = new ArrayList<>();

	public void add(double x, double y) {
		xs.add(x);
		ys.add(y);
	}

	public int size() {
		return xs.size();
	}

	public double getX(int index) {
		return xs.get(index);
	}

	public double getY(int index) {
		return ys.get(index);
	}

	// biggest x for all good enough solutions (y - delta)
	// return delta
	public void approximation(double yFraction) {
		double yThreshold = Collections.max(ys) * yFraction;
		double xMax = Collections.max(xs);
		Integer bestAccepted = null;
		for (int i = 0; i < xs.size(); i++) {
			double y = ys.get(i);
			double x = xs.get(i);
			if (y >= yThreshold) {
				if (bestAccepted == null || xs.get(i) > xs.get(bestAccepted)) {
					bestAccepted = i;
				}
			}
		}
		int selected = 0; // how many will be selected above x[bestAccepted]
		for (double x : xs) {
			if (x >= xs.get(bestAccepted)) {
				selected++;
			}
		}
		System.out.println("relative delta: " + (xMax - xs.get(bestAccepted) / xMax));
		System.out.println("selected percent: " + ((double) selected / xs.size()));
	}

	public double getPercentage(BiFunction<Double, Double, Boolean> selection) {
		int count = 0;
		for (int i = 0; i < size(); i++) {
			if (selection.apply(getX(i), getY(i))) {
				count++;
			}
		}
		return ((double) count) / size();
	}

}
