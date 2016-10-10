package geometry;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import breeze.linalg.functions.cosineDistance;
import superposition.SuperPositionQCP;

/**
 * 
 * @author antonin
 * 
 *         Class for superposing sets of points.
 *
 */

public class Transformer {
	private Point3d[] a, b;
	private SuperPositionQCP qcp = new SuperPositionQCP();

	public Transformer(Point3d[] a, Point3d[] b) {
		this.a = a;
		this.b = b;
		qcp.set(a, b);
	}

	public double getRmsd() {
		return qcp.getRmsd();
	}

	public Point3d[] transform() {
		return qcp.getTransformedCoordinates();
	}

	public Matrix4d getMatrix() {
		Matrix4d m = qcp.getTransformationMatrix();
		return m;
	}

	public Matrix3d getRotationMatrix() {
		return qcp.calcRotationMatrix();
	}

	/**
	 * Return the difference between two rotations in radians.
	 * 
	 * More precisely, it returns the angle of the axis-angle representation of
	 * the third rotation z such that composition of x and z gives y.
	 */
	public static double compareRotations(Matrix3d x, Matrix3d y) {
		x.invert();
		x.mul(y);
		AxisAngle4d aa = new AxisAngle4d();
		aa.set(x);
		return aa.angle;
	}

	public static void main(String[] args) {
		double angle1 = -0.2;
		double angle2 = 0.3;
		double[][] ac = { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };
		double[][] bc = { { Math.cos(angle1), Math.sin(angle1), 0 }, { -Math.sin(angle1), Math.cos(angle1), 0 },
				{ 0, 0, 1 } };
		double[][] cc = { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };
		double[][] dc = { { Math.cos(angle2), Math.sin(angle2), 0 }, { -Math.sin(angle2), Math.cos(angle2), 0 },
				{ 0, 0, 1 } };
		Transformer ta = new Transformer(PointConversion.getPoints3d(ac), PointConversion.getPoints3d(bc));
		Transformer tb = new Transformer(PointConversion.getPoints3d(cc), PointConversion.getPoints3d(dc));

		ta.getRmsd();
		Matrix3d ma = ta.getRotationMatrix();
		tb.getRmsd();
		Matrix3d mb = tb.getRotationMatrix();

		System.out.println(compareRotations(ma, mb));
	}
}
