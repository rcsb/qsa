package vectorization;

import geometry.primitives.Point;

/**
 *
 * @author Antonin Pavelka
 */
public class RigidBody {

	private Point center;
	private Point[] auxiliary;

	public RigidBody(Point center, Point... auxiliaryPoints) {
		this.center = center;
		this.auxiliary = auxiliaryPoints;
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

}
