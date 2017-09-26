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
	}

	/*
	 * Expresses the p in this coordinate system.
	 *Solving three variable linear equation system
	 * 3x + 2y -  z =  1 ---&gt; Eqn(1)
     * 2x - 2y + 4z = -2 ---&gt; Eqn(2)
	 * -x + y/2-  z =  0 ---&gt; Eqn(3)
	 */
	public Point expresPoint(Point p) {
		//Creating  Arrays Representing Equations
		Point q = p.minus(origin);
		double[][] lhsArray = {
			{x.x, x.y, x.z},
			{y.x, y.y, y.z},
			{z.x, z.y, z.z}
		};
		//{2, -2, 4}, {-1, 0.5, -1}};
		double[] rhsArray = {q.x, q.y, q.z};
		//Creating Matrix Objects with arrays
		Matrix lhs = new Matrix(lhsArray);
		Matrix rhs = new Matrix(rhsArray, 3);
		//Calculate Solved Matrix
		Matrix ans = lhs.solve(rhs);
		//Printing Answers
		//System.out.println("x = " + ans.get(0, 0));
		//System.out.println("y = " + ans.get(1, 0));
		//System.out.println("z = " + ans.get(2, 0));

		Point result = new Point(ans.get(0, 0), ans.get(1, 0), ans.get(2, 0));
		return result;
	}

	public static double[] getPointAsPolar(Point p) {
		double radius = Math.sqrt(p.x * p.x + p.y * p.y + p.z * p.z);
		double inclination = Math.acos(p.z / radius);
		double azimuth = Math.atan2(p.y, p.x);
		double[] result = {radius, inclination, azimuth};
		return result;
	}
}
