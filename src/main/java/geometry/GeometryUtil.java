package geometry;

import geometry.primitives.Point;
import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 */
public class GeometryUtil {

	public static Point3d[] merge(Point3d[] a, Point3d[] b) {
		Point3d[] c = new Point3d[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	public static Point[] merge(Point[] a, Point[] b) {
		Point[] c = new Point[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}
}
