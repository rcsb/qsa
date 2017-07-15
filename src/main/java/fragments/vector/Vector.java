package fragments.vector;

/**
 *
 * @author Antonin Pavelka
 */
public class Vector {

	public static void add(double[] x, double[] y) {
		for (int i = 0; i < x.length; i++) {
			x[i] += y[i];
		}
	}

	public static double[] plus(double[] x, double[] y) {
		double[] z = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			z[i] = x[i] + y[i];
		}
		return z;
	}

	public static double[] invert(double[] x) {
		double[] z = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			z[i] = -x[i];
		}
		return z;
	}

	public static double[] minus(double[] x, double[] y) {
		double[] z = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			z[i] = x[i] - y[i];
		}
		return z;
	}

	public static double[] divide(double[] x, double d) {
		if (d == 0) {
			throw new RuntimeException();
		}
		double[] z = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			z[i] = x[i] / d;
		}
		return z;
	}

	public static double[] multiply(double[] x, double d) {
		double[] z = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			z[i] = x[i] * d;
		}
		return z;
	}

	public static double size(double[] v) {
		double sum = 0;
		for (int i = 0; i < v.length; i++) {
			double d = v[i];
			sum += d * d;
		}
		return Math.sqrt(sum);
	}

	public static double[] unit(double[] x) {
		double[] z = new double[x.length];
		double size = size(x);
		for (int i = 0; i < x.length; i++) {
			z[i] = x[i] / size;
		}
		return z;
	}

	public static void print(double[] v) {
		System.out.print("[");
		for (double d : v) {
			System.out.print(d + ",");
		}
		System.out.println("]");
	}

	public static double chebyshev(double[] x, double[] y) {
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < x.length; i++) {
			double d = Math.abs(x[i] - y[i]);
			if (d > max) {
				max = d;
			}
		}
		return max;
	}

	public static double manhattan(double[] x, double[] y) {
		double f = 0;
		for (int d = 0; d < x.length; d++) {
			f += Math.abs(x[d] - y[d]);
		}
		f /= x.length;
		return f;
	}

	public static double minkowski(double[] x, double[] y, double p) {
		double sum = 0;
		for (int i = 0; i < x.length; i++) {
			double d = Math.pow(Math.abs(x[i] - y[i]), p);
			sum += d;
		}
		return Math.pow(sum, 1.0 / p);
	}

	public static double euclidean(double[] x, double[] y) {
		double sum = 0;
		for (int i = 0; i < x.length; i++) {
			double d = x[i] - y[i];
			sum += d * d;
		}
		return Math.sqrt(sum);
	}

}
