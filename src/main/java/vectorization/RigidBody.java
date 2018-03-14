package vectorization;

import geometry.exceptions.CoordinateSystemException;
import geometry.primitives.CoordinateSystem;
import geometry.primitives.Point;
import geometry.primitives.Versor;
import java.util.function.Function;

/**
 *
 * @author Antonin Pavelka
 */
public class RigidBody {

	private final Point center;
	private final Point[] auxiliary;

	private RigidBody(Point center, Point[] auxiliaryPoints) {
		this.center = center;
		this.auxiliary = auxiliaryPoints;
		center.check();
	}

	public static RigidBody createWithCenter(Point center, Point... auxiliaryPoints) {
		return new RigidBody(center, auxiliaryPoints);
	}

	public static RigidBody create(Point... points) {
		return new RigidBody(Point.average(points), points);
	}

	private RigidBody transform(Function<Point, Point> function) {
		Point[] newAuxiliary = new Point[auxiliary.length];
		for (int i = 0; i < auxiliary.length; i++) {
			newAuxiliary[i] = function.apply(auxiliary[i]);
		}
		Point newCenter = function.apply(center);
		return new RigidBody(newCenter, newAuxiliary);
	}

	public RigidBody center() {
		return transform(x -> x.minus(center));
	}

	public RigidBody express(CoordinateSystem system) {
		return transform(x -> system.expresPoint(x));
	}

	public RigidBody rotate(Versor v) {
		centerIsInOrigin();
		return transform(x -> v.rotate(x));
	}

	public RigidBody average(RigidBody other) {
		Point averagedCenter = center.average(other.center);
		Point[] averagedAuxiliary = new Point[auxiliary.length];
		for (int i = 0; i < auxiliary.length; i++) {
			averagedAuxiliary[i] = auxiliary[i].average(other.auxiliary[i]);
		}
		return new RigidBody(averagedCenter, averagedAuxiliary);
	}

	private void centerIsInOrigin() {
		if (!center.close(new Point(0, 0, 0))) {
			throw new RuntimeException();
		}
	}

	public Point getCenter() {
		return center;
	}

	public Point[] getAuxiliaryPoints() {
		return auxiliary;
	}

	public Point[] getAllPoints() {
		Point[] cloud = new Point[auxiliary.length + 1];
		cloud[0] = getCenter();
		for (int i = 0; i < auxiliary.length; i++) {
			cloud[i + 1] = auxiliary[i];
		}
		return cloud;
	}

	public CoordinateSystem getCoordinateSystem() throws CoordinateSystemException {
		if (auxiliary.length != 2) {
			throw new IllegalStateException();
		}
		Point vectorU = Point.vector(center, auxiliary[0]);
		Point vectorV = Point.vector(center, auxiliary[1]);
		return new CoordinateSystem(center, vectorU, vectorV);
	}

}
