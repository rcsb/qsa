package language;

/**
 *
 * @author Antonin Pavelka
 */
public class Util {

	public static float[] merge(float[] a, float[] b) {
		float[] c = new float[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	public static void print(float[] a) {
		for (float f : a) {
			System.out.print(f + " ");
		}
		System.out.println();
	}

	public static boolean similar(double x, double y) {
		return Math.abs(x - y) < 0.000001;
	}

	public static void check(boolean b, String s) {
		if (!b) {
			throw new RuntimeException(s);
		}
	}

	public static double random() {
		return (Math.random() - 0.5) * 1000;
	}

	public static double randomNonnegative() {
		return Math.random() * 1000;
	}
}
