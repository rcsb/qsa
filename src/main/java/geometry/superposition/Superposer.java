package geometry.superposition;

import embedding.lipschitz.object.PointTuple;
import embedding.lipschitz.object.PointTupleDistanceMeasurement;
import geometry.primitives.Point;
import geometry.primitives.Versor;
import info.laht.dualquat.Quaternion;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

/**
 * Class for superposing sets of points, encapsulates the real algorithm.
 *
 * @author Antonin Pavelka
 *
 */
public class Superposer implements PointTupleDistanceMeasurement {

	private SuperPositionQCP qcp;
	private Point3d[] a;
	private Point3d[] b;

	public Superposer() {
		qcp = new SuperPositionQCP(false);
	}
	
	public Superposer(Point3d[] a, Point3d[] b) {
		qcp = new SuperPositionQCP(false);
		qcp.set(a, b);
	}
	
	public Superposer(boolean centered) {
		qcp = new SuperPositionQCP(centered);
	}
	
	public void set(Point3d[] a, Point3d[] b) {
		this.a = a;
		this.b = b;
		qcp.set(a, b);
		qcp.getRmsd(); // ???
	}

	public void set(Point[] a, Point[] b) {
		set(convert(a), convert(b));
	}

	public Versor getVersor() {
		qcp.calcRotationMatrix();
		Versor versor = qcp.getVersor();
		return versor;
	}

	public Quaternion getQuaternion() {
		qcp.calcRotationMatrix();
		Quaternion quaternion = qcp.getQuaternion();
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
	
	public Matrix4d getTransformationMatrix() {
		return qcp.getTransformationMatrix();
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

	@Override
	public double getDistance(PointTuple a, PointTuple b) {
		set(a.getPoints(), b.getPoints());
		return getRmsd();
	}

}
