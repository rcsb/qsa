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
			sum += v[i] * v[i];
		}
		return (double) Math.sqrt(sum);
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

}
