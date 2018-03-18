package statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import language.MathUtil;

/**
 *
 * @author Antonin Pavelka
 */
public class Distribution2d {

	private List<Pair> data = new ArrayList<>();
	private boolean sorted;

	private void add(double x, double y) {
		sorted = false;
	}

	private void sort() {
		if (!sorted) {
			Collections.sort(data);
			sorted = true;
		}
	}

	public double correlation() {
		return MathUtil.correlation(getXs(), getYs());
	}

	private double[] getXs() {
		double[] xs = new double[data.size()];
		for (int i = 0; i < data.size(); i++) {
			xs[i] = data.get(i).x;
		}
		return xs;
	}

	private double[] getYs() {
		double[] xs = new double[data.size()];
		for (int i = 0; i < data.size(); i++) {
			xs[i] = data.get(i).y;
		}
		return xs;
	}

	private List<Pair> percentile(double percentile) {
		sort();
		List<Pair> subset = new ArrayList<>();
		int size = (int) Math.ceil(data.size() * percentile);
		for (int i = 0; i < size; i++) {
			subset.add(data.get(i));
		}
		return subset;
	}
}

class Pair implements Comparable<Pair> {

	public final double x, y;

	public Pair(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int compareTo(Pair other) {
		return Double.compare(x, other.x);
	}
}
