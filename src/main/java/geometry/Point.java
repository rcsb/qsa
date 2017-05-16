package geometry;

import java.io.Serializable;
import java.util.Random;

/*
 * Represents a point in 3D space. The point can be treated as a vector and 
 * this class provides vector operations such as addition and dot product.
 */
public class Point implements Coordinates, Serializable {

	public final double x;
	public final double y;
	public final double z;

	private Point() {
		x = 8888;
		y = 8888;
		z = 8888;
	}

	public Point(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;

		assert check();
	}

	public Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		assert check();
	}

	public Point(double[] coordinates) {
		x = coordinates[0];
		y = coordinates[1];
		z = coordinates[2];
		assert check();
	}

	public Point(float[] coordinates) {
		x = coordinates[0];
		y = coordinates[1];
		z = coordinates[2];
		assert check();
	}

	public Point negative() {
		return new Point(-x, -y, -z);
	}

	public boolean check() {
		if (Double.isNaN(x)) {
			throw new RuntimeException();
		}
		if (Double.isNaN(y)) {
			throw new RuntimeException();
		}
		if (Double.isNaN(z)) {
			throw new RuntimeException();
		}
		return true;
	}

	public Point(Point p) {
		x = p.x;
		y = p.y;
		z = p.z;
	}

	public static Point createShattered(Point p, double maxDev, Random random) {
		return new Point(
			shatter(p.x, maxDev, random),
			shatter(p.y, maxDev, random),
			shatter(p.z, maxDev, random));
	}

	/*
     * d - number to randomly change maxDeviation - maximum difference between d
     * and returned value
	 */
	private static double shatter(double d, double maxDev, Random random) {
		assert 0 < maxDev;
		double r = (random.nextDouble() - 0.5) * 2 * maxDev;
		assert r <= maxDev;
		double value = d + r;
		return value;
	}

	/*
     * Returns geometric center of Point collection i. e. center of gravity
     * where each point has unit weight.
	 */
	public static Point center(Iterable<Point> points) {
		Point t = new Point(0, 0, 0);
		int counter = 0;
		for (Point p : points) {
			t = t.plus(p);
			counter++;
		}
		t = t.divide(counter);
		return t;
	}

	public static Point center(Point[] points) {
		Point t = new Point(0, 0, 0);
		for (Point p : points) {
			t = t.plus(p);
		}
		t = t.divide(points.length);
		return t;
	}

	public Point plus(Point p) {
		return new Point(x + p.x, y + p.y, z + p.z);
	}

	public Point minus(Point p) {
		return new Point(x - p.x, y - p.y, z - p.z);
	}

	public double squaredSize() {
		return x * x + y * y + z * z;
	}

	public double size() {
		return Math.sqrt(squaredSize());
	}

	public double distance(Point p) {
		return Math.sqrt(this.minus(p).squaredSize());
	}

	public double squaredDistance(Point p) {
		return this.minus(p).squaredSize();
	}

	public Point multiply(double d) {
		return new Point(d * x, d * y, d * z);
	}

	public Point divide(double d) {
		assert d != 0;
		return new Point(x / d, y / d, z / d);
	}

	/*
     * Dot product. Skalarni soucin.
	 */
	public double dot(Point p) {
		return x * p.x + y * p.y + z * p.z;
	}

	public double angle(Point other) {
		double vDot = this.dot(other) / (this.size() * other.size());
		if (vDot < -1.0) {
			vDot = -1.0;
		}
		if (vDot > 1.0) {
			vDot = 1.0;
		}
		return (double) Math.acos(vDot);
	}

	public Point cross(Point p) {
		Point v = new Point(
			y * p.z - p.y * z,
			z * p.x - p.z * x,
			x * p.y - p.x * y);
		return v;
	}

	@Override
	public String toString() {
		return "[" + x + ", " + y + ", " + z + "]";
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 83 * hash + (int) (Double.doubleToLongBits(this.x)
			^ (Double.doubleToLongBits(this.x) >>> 32));
		hash = 83 * hash + (int) (Double.doubleToLongBits(this.y)
			^ (Double.doubleToLongBits(this.y) >>> 32));
		hash = 83 * hash + (int) (Double.doubleToLongBits(this.z)
			^ (Double.doubleToLongBits(this.z) >>> 32));
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		Point p = (Point) o;
		return p.x == x && p.y == y && p.z == z;
	}

	public boolean close(Point p) {
		return squaredDistance(p) < 0.00001;
	}

	public boolean quiteClose(Point p) {
		return squaredDistance(p) < 0.01;
	}

	public Point normalize() {
		double size = size();
		assert size != 0;
		return divide(size);
	}

	@Override
	public double[] getCoords() {
		double[] cs = {x, y, z};
		return cs;
	}
}
