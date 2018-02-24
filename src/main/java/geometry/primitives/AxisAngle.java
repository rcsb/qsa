package geometry.primitives;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;

/**
 *
 * @author Antonin Pavelka
 */
public class AxisAngle {

	private Point axis;
	private double angle;

	public AxisAngle(Matrix3d matrix) {
		double[] xyzAngle = getAxisAngle(matrix);
		axis = new Point(xyzAngle[0], xyzAngle[1], xyzAngle[2]);
		axis = axis.normalize();
		angle = xyzAngle[3];
	}

	private double[] getAxisAngle(Matrix3d matrix) {
		AxisAngle4d axisAngle = new AxisAngle4d();
		axisAngle.set(matrix);
		double[] xyzAngle = new double[4];
		axisAngle.get(xyzAngle);
		return xyzAngle;
	}

	public double getNormalizedAngle() {
		return angle / Math.PI;
	}

	/**
	 * Representation suitable for rotation comparisons - vectors are be similar iff rotations are similar. However, the
	 * topology of the space must be closed (3D version of torus) - similarly to any angles or cyclic values.
	 */
	public Point getVectorRepresentation() {
		//System.err.println("WARNING: morph sphere to cube.");
		return axis.multiply(getNormalizedAngle());

	}

}
