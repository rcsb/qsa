package geometry.primitives;

import java.util.Random;

/**
 * @author Antonin Pavelka
 *
 * http://www.euclideanspace.com
 */
public class Versor {

	public final double x, y, z;
	public final double w; // scalar, sometimes written first
	private final static Point ZERO = new Point(0, 0, 0);

	public Versor(double x, double y, double z, double w) {
		double d = x * x + y * y + z * z + w * w;
		this.x = x / d;
		this.y = y / d;
		this.z = z / d;
		this.w = w / d;
	}

	public static Versor create(AxisAngle axisAngle) {
		double half = axisAngle.getAngle() / 2;
		double sin = Math.sin(half);
		Point axis = axisAngle.getAxis();
		double x = axis.x * sin;
		double y = axis.y * sin;
		double z = axis.z * sin;
		double w = Math.cos(half);
		return new Versor(x, y, z, w);
	}

	public static Versor create(Random random) {
		Point u = Point.unit(random);
		double angle = (Math.random() * 2 - 1) * Math.PI;
		return Versor.create(new AxisAngle(u, angle));
	}

	public String toString() {
		return x + "," + y + "," + z + "," + w;
	}

	public float[] toFloats() {
		float[] floats = {(float) x, (float) y, (float) z, (float) w};
		return floats;
	}

	public Point rotate(Point point) {
		if (point.close(ZERO)) {
			return point;
		}
		Versor p = new Versor(point.x, point.y, point.z, 0);
		return this.multiply(p).multiply(this.inverse()).getVector();
	}

	public Versor inverse() {
		double d = x * x + y * y + z * z + w * w;
		return new Versor(-x / d, -y / d, -z / d, w / d);
	}

	public Versor negate() {
		return new Versor(-x, -y, -z, -w);
	}

	public Versor multiply(Versor other) {
		double x = this.x * other.w + this.w * other.x + this.y * other.z - this.z * other.y;
		double y = this.w * other.y - this.x * other.z + this.y * other.w + this.z * other.x;
		double z = this.w * other.z + this.x * other.y - this.y * other.x + this.z * other.w;
		double w = this.w * other.w - this.x * other.x - this.y * other.y - this.z * other.z;
		return new Versor(x, y, z, w);
	}

	public Point getVector() {
		return new Point(x, y, z);
	}

	public AxisAngle toAxisAngle() {
		double sqrLength = x * x + y * y + z * z;
		double angle;
		Point axis;
		if (sqrLength == 0.0) {
			assert false;
			angle = 0.0;
			axis = new Point(1, 0, 0);
		} else {
			angle = (2 * Math.acos(w));
			double invLength = (1.0 / Math.sqrt(sqrLength));
			axis = new Point(x * invLength, y * invLength, z * invLength);
		}
		assert angle >= 0 && angle <= 2 * Math.PI : angle;
		return new AxisAngle(axis, angle);
	}

}
