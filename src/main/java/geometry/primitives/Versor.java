package geometry.primitives;

import java.util.Random;

/**
 *
 * @author Antonin Pavelka
 *
 * http://www.euclideanspace.com
 */
public class Versor {

	private final double x, y, z;
	private final double w; // scalar, sometimes written first

	public Versor(double x, double y, double z, double w) {
		double d = x * x + y * y + z * z + w * w;
		this.x = x / d;
		this.y = y / d;
		this.z = z / d;
		this.w = w / d;
	}

	public static Versor create(Point axis, double angle) {
		double half = angle / 2;
		double x = axis.x * Math.sin(half);
		double y = axis.y * Math.sin(half);
		double z = axis.z * Math.sin(half);
		double w = Math.cos(half);
		return new Versor(x, y, z, w);
	}

	public static Versor create(Random random) {
		Point u = Point.unit(random);
		double angle = (Math.random() * 2 - 1) * Math.PI;
		return Versor.create(u, angle);
	}

	public String toString() {
		return x + "," + y + "," + z + "," + w;
	}

	public float[] toFloats() {
		float[] floats = {(float) x, (float) y, (float) z, (float) w};
		return floats;
	}

	public Point rotate(Point point) {
		Versor p = new Versor(point.x, point.y, point.z, 0);
		return this.multiply(p).multiply(this.inverse()).getVector();
	}
	
	public Versor inverse() {
		double d = x * x + y * y + z * z + w * w;
		return new Versor(-x / d, -y / d, -z / d, w / d);
	}

	public Versor wrap() {
		return new Versor(-x, -y, -z, -w);
	}

	public Versor multiply(Versor other) {
		/*double x = this.w * other.x + this.x * other.w + this.y * other.z - this.z * other.y;
		double y = this.w * other.y - this.x * other.z + this.y * other.w + this.z * other.x;
		double z = this.w * other.z + this.x * other.y - this.y * other.x + this.z * other.w;
		double w = this.w * other.w - this.x * other.x - this.y * other.y - this.z * other.z;
		 */
		double x = this.x * other.w + this.w * other.x + this.y * other.z - this.z * other.y;
		double y = this.w * other.y - this.x * other.z + this.y * other.w + this.z * other.x;
		double z = this.w * other.z + this.x * other.y - this.y * other.x + this.z * other.w;
		double w = this.w * other.w - this.x * other.x - this.y * other.y - this.z * other.z;

		return new Versor(x, y, z, w);
	}

	public Point getVector() {
		return new Point(x, y, z);
	}

}
