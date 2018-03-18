package language;

import java.util.Arrays;

/**
 *
 * @author Antonin Pavelka
 */
public class Util {

	/*public static float[] merge(float[] a, float[] b) {
		float[] c = new float[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}*/
	public static float[] merge(float[]... arrays) {
		int finalLength = 0;
		for (float[] array : arrays) {
			finalLength += array.length;
		}
		float[] destination = null;
		int destinationPosition = 0;
		for (float[] array : arrays) {
			if (destination == null) {
				destination = Arrays.copyOf(array, finalLength);
				destinationPosition = array.length;
			} else {
				System.arraycopy(array, 0, destination, destinationPosition, array.length);
				destinationPosition += array.length;
			}
		}
		return destination;
	}

	public static double min(double... array) {
		switch (array.length) {
			case 0:
				throw new RuntimeException();
			case 1:
				return array[0];
			default:
				double value = array[0];
				for (int i = 1; i < array.length; i++) {
					value = Math.min(value, array[i]);
				}
				return value;
		}
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
