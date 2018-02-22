package vectorization;

import geometry.primitives.Point;

/**
 *
 * @author Antonin Pavelka
 */
public class RigidBody {

	private Point center;
	private Point[] cloud;

	public RigidBody(Point center, Point... cloud) {
		this.center = center;
		this.cloud = cloud;
	}

	public Point getCenter() {
		return center;
	}

	public Point[] getCloud() {
		return cloud;
	}

}
