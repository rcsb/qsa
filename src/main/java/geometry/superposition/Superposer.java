package geometry.superposition;

import geometry.primitives.Point;
import geometry.primitives.Quaternion;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

/**
 *
 * @author antonin
 *
 * Class for superposing sets of points, encapsulates the real algorithm.
 *
 */
public class Superposer {

	private SuperPositionQCP qcp = new SuperPositionQCP(false);
	private Point3d[] a;
	private Point3d[] b;

	public void set(Point3d[] a, Point3d[] b) {
		this.a = a;
		this.b = b;
		qcp.set(a, b);
		qcp.getRmsd();
	}

	public void set(Point[] a, Point[] b) {
		set(convert(a), convert(b));
	}

	public Quaternion getQuaternion() {
		Quaternion quaternion = qcp.getQuaternion();
		if (quaternion == null) {
			qcp.calcRotationMatrix();
		}
		quaternion = qcp.getQuaternion();
		return quaternion;
	}

	public double getRmsd() {
		double r = qcp.getRmsd();
		return r;
	}

	public Point3d[] getTransformedY() {
		return qcp.getTransformedCoordinates();
	}

	public Point[] getXPoints() {
		return convert(a);
	}

	public Point[] getTransformedYPoints() {
		return convert(qcp.getTransformedCoordinates());
	}

	public Matrix4d getMatrix() {
		Matrix4d m = qcp.getTransformationMatrix();
		return m;
	}

	public Matrix3d getRotationMatrix() {
		return qcp.calcRotationMatrix();
	}

	private Point3d[] convert(Point[] points) {
		Point3d[] result = new Point3d[points.length];
		for (int i = 0; i < points.length; i++) {
			result[i] = points[i].toPoint3d();
		}
		return result;
	}

	private Point[] convert(Point3d[] points) {
		Point[] result = new Point[points.length];
		for (int i = 0; i < points.length; i++) {
			result[i] = new Point(points[i]);
		}
		return result;
	}

}
