package embedding.lipschitz.object;

import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 */
public class PointTuple {

	private Point3d[] points;

	public PointTuple() {

	}

	public PointTuple(Point3d[] source) {
		points = new Point3d[source.length];
		for (int i = 0; i < source.length; i++) {
			points[i] = new Point3d(source[i]);
		}
	}

	public Point3d[] getPoints() {
		return points;
	}
}
