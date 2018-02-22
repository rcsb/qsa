package geometry;

import geometry.primitives.Point;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;

import global.Parameters;

public class RotationPair {
	Matrix3d x;
	Matrix3d y;

	public RotationPair(Matrix3d x, Matrix3d y) {
		this.x = new Matrix3d(x); // just in case, copied once more everywhere
		this.y = new Matrix3d(y);
	}

	/**
	 * Return the difference between two rotations in radians.
	 * 
	 * More precisely, it returns the angle of the axis-angle representation of
	 * the third rotation z such that composition of x and z gives y.
	 */
	public double compareRotations() {
		Matrix3d m = new Matrix3d(x);
		Matrix3d n = new Matrix3d(y); // just in case, probably not modified
		m.invert();
		m.mul(n);
		AxisAngle4d aa = new AxisAngle4d();
		aa.set(m);
		return aa.angle;
	}

	public Matrix3d average() {
		AxisAngle4d aax = new AxisAngle4d();
		aax.set(x);
		AxisAngle4d aay = new AxisAngle4d();
		aay.set(y);
		Point px = new Point(aax.x, aax.y, aax.z).normalize(); // just in case
		Point py = new Point(aay.x, aay.y, aay.z).normalize();
		Point p = px.plus(py).divide(2);
		double angle = (aax.angle + aay.angle) / 2;
		AxisAngle4d aa = new AxisAngle4d(p.x, p.y, p.z, angle);
		Matrix3d m = new Matrix3d();
		m.set(aa);
		return m;
	}

}
