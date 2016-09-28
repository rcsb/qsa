package spark.clustering;

import java.util.Random;

public class Multidimensional implements Clusterable<Multidimensional> {

	private static final long serialVersionUID = 1L;
	private static Random random = new Random(1);
	private static int dimensions = 1;
	private double[] coords = new double[dimensions];

	public Multidimensional() {
		for (int i = 0; i < dimensions; i++) {
			coords[i] = random.nextDouble();
		}
	}

	public double distance(Multidimensional e) {
		double sum = 0;
		for (int i = 0; i < dimensions; i++) {
			double d = coords[i] - e.coords[i];
			sum += d * d;
		}
		return Math.sqrt(sum);
	}

	private String nice(double d) {
		return "" + ((double) Math.round(d * 1000));
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("(");
		for (int i = 0; i < dimensions; i++) {
			sb.append(nice(coords[i]));
			if (i < dimensions - 1) {
				sb.append(" ");
			}
		}
		sb.append(")");
		return sb.toString();
	}

}
