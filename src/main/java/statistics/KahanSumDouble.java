package statistics;

/**
 * https://rosettacode.org/wiki/Kahan_summation
 */
public class KahanSumDouble {

	private double sum = 0.0;
	private double c = 0.0;

	public void add(double d) {
		double y = d - c;
		double t = sum + y;
		c = (t - sum) - y;
		sum = t;
	}

	public double value() {
		return sum;
	}

}
