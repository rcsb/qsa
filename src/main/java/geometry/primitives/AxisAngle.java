package geometry.primitives;

//import javax.vecmath.AxisAngle4d;
import geometry.angles.Angles;

/**
 * Axis-angle representation of a rotation.
 *
 * @author Antonin Pavelka
 */
public class AxisAngle {

	private Point axis;
	private double angle;

	public AxisAngle(Point axis, double angle) {
		angle = Angles.wrap(angle);
		/*if (angle < 0) {
			angle = 2 * Math.PI - angle;
			axis = axis.negate();
		}*/
		assert 0 <= angle && angle <= 2 * Math.PI;
		this.axis = axis;
		this.angle = angle;
	}

	/*public AxisAngle toNonnegativeZ() {
		if (axis.z < 0) {
			System.out.println("a " + Angles.toDegrees(angle));
			System.out.println("b " + Angles.toDegrees(Angles.wrap(-angle)));
			return new AxisAngle(axis.negate(), -angle);
		} else {
			return new AxisAngle(axis, angle);
		}
	}*/

	public double getAngle() {
		return angle;
	}

	public Point getAxis() {
		return axis;
	}

	public boolean isSimilar(AxisAngle other) {
		boolean points = axis.close(other.axis);
		boolean angles = Math.abs(angle - other.angle) < 0.00001;
		return points && angles;
	}

	//public AxisAngle(Matrix3d matrix) {
	//	matrix.normalize(); // just in case
	//	AxisAngleFactory.toAxisAngle(matrix)
	//double[] xyzAngle = getAxisAngle(matrix);
	//axis = new Point(xyzAngle[0], xyzAngle[1], xyzAngle[2]);
	//axis = axis.normalize();
	//angle = xyzAngle[3];
	//}

	/*private double[] getAxisAngle(Matrix3d matrix) {
		AxisAngle4d axisAngle = new AxisAngle4d();
		axisAngle.set(matrix);
		double[] xyzAngle = new double[4];
		axisAngle.get(xyzAngle);
		return xyzAngle;
	}*/
	public double getNormalizedAngle() {
		return angle / Math.PI;
	}

	public double getAngleInDegrees() {
		return angle * 180 / Math.PI;
	}

	/**
	 * Representation suitable for rotation comparisons - vectors are be similar iff rotations are similar. However, the
	 * topology of the space must be closed (3D version of torus) - similarly to any angles or cyclic values.
	 */
	public Point getVectorRepresentation() {
		double normalizedAngle = getNormalizedAngle();
		assert normalizedAngle <= 1;
		assert normalizedAngle >= 0;

		//System.out.println("axis " + axis + " angle " + angle);
		Point pointInsideSphere = axis.multiply(getNormalizedAngle());

		//System.out.println("---///");
		//System.out.println(pointInsideSphere);
		//System.out.println(pointInsideSphere.size());
		assert pointInsideSphere.size() <= 1;

		return pointInsideSphere;

		/*Point morphed = sphereToCube(pointInsideSphere);
		System.out.println("morphed " + morphed);
		System.out.println(morphed.size());
		return morphed;
		 */
		//return pointInsideSphere;
	}

	// transform difference of vectors into arc length !!!
	// after euclidean
	// berfore morphing
	// this could be used as a replacement of RMSD
	// thwy is transitivity important?
	// check morphing 
	/**
	 * Moves each point in a space inside a unit sphere onto a space inside a unit cube linearly. (zero centered,
	 * diameter and edge is 2).
	 */
	protected Point sphereToCube(Point x) {
		assert x.size() <= 1;
		if (x.size() < 0.0001) { // almost zero, do not transform, avoid zeroes and too big numerical errors
			return x;
		} else {
			double scale = getScale(x);
			Point y = x.multiply(scale);
			return y;
		}
	}

	private double getScale(Point x) {
		Point unit = x.normalize();
		double[] coords = unit.getCoords();
		double dim = 0;
		double max = 0;
		for (int i = 0; i < 3; i++) {
			double dist = Math.abs(coords[i]);
			if (max < dist) {
				max = dist;
				dim = i;
			}
		}
		double scale = 1 / max; // should be one, is max
		return scale;
	}

	public String toString() {
		return axis + ", " + angle;
	}

}
