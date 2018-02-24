package geometry.superposition;

import geometry.primitives.Point;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

/**
 *
 * @author antonin
 *
 * Class for superposing sets of points.
 *
 */
public class Transformer {

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

	public double getRmsd() {
		double r = qcp.getRmsd();
		return r;
	}

	public double getSumOfDifferences() {
		Matrix4d m = getMatrix();
		for (Point3d p : b) {
			m.transform(p);
		}
		Point3d[] c = getTransformedY();
		double sum = 0;
		for (int i = 0; i < c.length; i++) {
			double d = a[i].distance(b[i]);
			sum += d;
			//sum += 1 / (1 + d * d);
		}
		sum = c.length;
		return sum;
	}

	public double getMaxDifferences() {
		Matrix4d m = getMatrix();
		for (Point3d p : b) {
			m.transform(p);
		}
		Point3d[] c = getTransformedY();
		double max = 0;
		for (int i = 0; i < c.length; i++) {
			double d = a[i].distance(b[i]);
			if (max < d) {
				max = d;
			}
		}
		return max;
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
			result[i] = new Point3d(points[i].getCoords());
		}
		return result;
	}

	private Point[] convert(Point3d[] points) {
		Point[] result = new Point[points.length];
		for (int i = 0; i < points.length; i++) {
			Point3d p = points[i];
			result[i] = new Point(p.x, p.y, p.z);
		}
		return result;
	}

}
