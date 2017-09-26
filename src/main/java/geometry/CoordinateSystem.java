package geometry;

import Jama.Matrix;

/**
 *
 * @author Antonin Pavelka
 */
public class CoordinateSystem {

	private Point x, y, z;
	private Point origin;

	public CoordinateSystem(Point[] points) {
		Point a = points[0];
		Point b = points[1];
		Point c = points[2];
		origin = a;
		Point u = b.minus(a);
		Point v = c.minus(a);
		z = u.cross(v).normalize();
		x = u.normalize();
		y = x.cross(z);
		if (Math.abs(y.size() - 1) > 0.001) {
			throw new RuntimeException("" + y.size());
		}
		/*Point p = expresPoint(a);
		Point q = expresPoint(b);
		Point r = expresPoint(c);
		System.out.println("***");
		System.out.println(p);
		System.out.println(q);
		System.out.println(r);*/
	}

	public Point expresPoint(Point p) {
		Point q = p.minus(origin);
		double[][] lhsArray = {
			{x.x, y.x, z.x},
			{x.y, y.y, z.y},
			{x.z, y.z, z.z}
		};
		double[] rhsArray = {q.x, q.y, q.z};
		Matrix lhs = new Matrix(lhsArray);
		Matrix rhs = new Matrix(rhsArray, 3);
		Matrix ans = lhs.solve(rhs);
		Point result = new Point(ans.get(0, 0), ans.get(1, 0), ans.get(2, 0));
		//Point check = origin.plus(x.multiply(result.x)).plus(y.multiply(result.y)).plus(z.multiply(result.z));
		//if (check.minus(p).size() > 0.1) {
		//	System.out.println("---check---");
		//	System.out.println(check);
		//	System.out.println(p);
		//} else {
		//	System.out.println("---FINE---");
		//}
		return result;
	}

	public static double[] getPointAsPolar(Point p) {
		double radius = Math.sqrt(p.x * p.x + p.y * p.y + p.z * p.z);
		double inclination = Math.acos(p.z / radius);
		double azimuth = Math.atan2(p.y, p.x);
		double[] result = {radius, inclination, azimuth};
		return result;
	}

	private static void test() {
		Point[] triangle = {new Point(0, 0, 0),
			new Point(10, 0, 0),
			new Point(0, 10, 0)};

		CoordinateSystem cs = new CoordinateSystem(triangle);

		System.out.println(cs.origin);
		System.out.println(cs.x);
		System.out.println(cs.y);
		System.out.println(cs.z);

		Point p = cs.expresPoint(new Point(2, 3, 4));

		System.out.println(p);

	}

	public static void main(String[] args) {
		test();
	}
}
