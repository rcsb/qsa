package geometry.superposition;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;

import javax.vecmath.Matrix4d;
import util.NumericUtil;

public class SmartTransformation {

	private SuperPositionQCP qcp = new SuperPositionQCP(false);
	private double rmsd;
	private Matrix3d rotationMatrix;
	private double[] eulerAngles;
	private Point3d translation;
	private Matrix4d matrix;

	// TODO copy coordinates whenever needed
	public SmartTransformation(Point3d[] x, Point3d[] y) {
		qcp.set(x, y);
		rmsd = qcp.getRmsd();
	}

	public void rotate(Tuple3d x) {
		rotationMatrix.transform(x);
	}

	public double getRmsd() {
		return rmsd;
	}

	public double[] getEulerAnlgles() {
		return eulerAngles;
	}

	public Point3d getTranslation() {
		return translation;
	}

	public void printAngles() {
		System.out.println(eulerAngles[0] + " " + eulerAngles[1] + " " + eulerAngles[2]);
	}

	public void printTranslation() {
		System.out.println(translation.x + " " + translation.y + " " + translation.z);
	}

	public void transform(Point3d x) {
		if (matrix == null) {
			matrix = qcp.getTransformationMatrix();
		}
		matrix.transform(x);
	}

	/**
	 * If more than RMSD is needed.
	 *
	 * Taking [0, 0, 0] for global origin.
	 */
	public void elaborate() {
		rotationMatrix = qcp.getRotationMatrix();
		eulerAngles = getXYZEuler(rotationMatrix);

		/*
		 * the vector now moves first centroid to the second after the first
		 * structre is rotated, therefore, similar transformations must have
		 * similar translation
		 */
		Point3d tx = new Point3d(qcp.getCentroidX());
		Point3d ty = new Point3d(qcp.getCentroidY());

		System.out.println("aaa");
		System.out.println(tx);
		System.out.println(ty);

		//rotationMatrix.transform(tx);
		rotationMatrix.transform(ty);
		translation = ty;
		translation.sub(tx);
	}

	/**
	 * From package org.biojava.nbio.structure.Calc.
	 *
	 * Convert a rotation Matrix to Euler angles. This conversion uses conventions as described on page:
	 * http://www.euclideanspace.com/maths/geometry/rotations/euler/index.htm Coordinate System: right hand Positive
	 * angle: right hand Order of euler angles: heading first, then attitude, then bank
	 *
	 * @param m the rotation matrix
	 * @return a array of three doubles containing the three euler angles in radians
	 */
	public static final double[] getXYZEuler(Matrix3d m) {
		double heading, attitude, bank;
		// Assuming the angles are in radians.
		if (m.m10 > 0.998) { // singularity at north pole
			heading = Math.atan2(m.m02, m.m22);
			attitude = Math.PI / 2;
			bank = 0;
		} else if (m.m10 < -0.998) { // singularity at south pole
			heading = Math.atan2(m.m02, m.m22);
			attitude = -Math.PI / 2;
			bank = 0;
		} else {
			heading = Math.atan2(-m.m20, m.m00);
			bank = Math.atan2(-m.m12, m.m11);
			attitude = Math.asin(m.m10);
		}
		return new double[]{heading, attitude, bank};
	}

	private static double[][] generateRotation(double a) {
		double[][] x = {{1 * Math.cos(a), 1 * Math.sin(a), 0}, {-1 * Math.sin(a), 1 * Math.cos(a), 0},
		{0, 0, 1}};
		return x;
	}

	private static double[] shift(double[] x, double[] s) {
		double[] y = {x[0] + s[0], x[1] + s[1], x[2] + s[2]};
		return y;
	}

	public static void main(String[] args) {
		double angle1 = Math.PI;
		double angle2 = Math.PI;
		double[] shift1 = {100, 0, 0};
		double[] shift2 = {0, 0, 0};

		double[][] ac = generateRotation(0);
		double[][] bc = generateRotation(angle1);
		double[][] cc = generateRotation(0);
		double[][] dc = generateRotation(angle2);

		SmartTransformation ta = new SmartTransformation(NumericUtil.doubleToPoint(ac), NumericUtil.doubleToPoint(bc));
		ta.elaborate();

		SmartTransformation tb = new SmartTransformation(NumericUtil.doubleToPoint(cc), NumericUtil.doubleToPoint(dc));
		tb.elaborate();

		double a = ta.getRmsd();
		double b = tb.getRmsd();
		System.out.println(a + " rmsd " + b);
		System.out.println("angles");
		ta.printAngles();
		tb.printAngles();
		ta.printTranslation();
		//System.out.println();

		tb.printTranslation();

	}

}
