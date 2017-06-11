package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Histogram {

	private double max = Double.NEGATIVE_INFINITY;
	private double min = Double.POSITIVE_INFINITY;
	private int bracketN;
	List<Double> data = new ArrayList<>();

	public Histogram(int bracketN) {
		this.bracketN = bracketN;
	}

	public void add(double d) {
		data.add(d);
	}

	public int[] get() {
		int[] brackets = new int[bracketN];
		max = Double.NEGATIVE_INFINITY;
		min = Double.POSITIVE_INFINITY;
		for (double d : data) {
			if (d < min) {
				min = d;
			}
			if (d > max) {
				max = d;
			}
		}
		double range = max - min;
		for (double d : data) {
			int i = (int) Math.floor((d - min) / range * bracketN);
			if (i >= brackets.length) {
				i = brackets.length - 1;
			}
			brackets[i]++;
		}
		return brackets;
	}

	public void print() {
		int[] brackets = get();
		System.out.println("--- histogram ---");
		for (int i =0;i< brackets.length; i++) {
			double a = min + (max - min) / bracketN * i;
			double b = min + (max - min) / bracketN * (i + 1);
			System.out.println(a + " - " + b + ": " + brackets[i]);
		}
	}

	public static void main(String[] args) {
		Histogram h = new Histogram(2);
		h.add(1);
		h.add(1.9999);
		h.add(3);
		h.print();
	}

}
