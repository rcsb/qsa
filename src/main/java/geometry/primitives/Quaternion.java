package geometry.primitives;

/**
 *
 * @author Antonin Pavelka
 */
public class Quaternion {

	private double a, b, c, d;

	public Quaternion(double a, double b, double c, double d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	public String toString() {
		return a + "," + b + "," + c + "," + d;
	}

	public float[] toFloat() {
		float[] floats = {(float) a, (float) b, (float) c, (float) d};
		return floats;
	}
}
