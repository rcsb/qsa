package geometry.primitives;

import Jama.Matrix;
import geometry.exceptions.CoordinateSystemException;

/**
 * Orthogonal 3D coordinate system.
 *
 * @author Antonin Pavelka
 */
public class CoordinateSystem {

	private Point xAxis, yAxis, zAxis; // unit vectors, direction of each axis
	private Point origin;
	private double maxError = 0.05;

	/**
	 * Constructs an orthogonal coordinates system with given origin. X-axis is created as a unit vector by normalizing
	 * vectorU. Z-axis is a normalized cross product of vectorU and vectorV, i.e., perpendicular to both. X-axis is then
	 * created as the cross product of Z-axis and X-axis.
	 *
	 * It is assumed that vectorU and vectorV are not codirectional.
	 *
	 * @param origin
	 * @param vectorU
	 * @param vectorV
	 */
	public CoordinateSystem(Point origin, Point vectorU, Point vectorV) throws CoordinateSystemException {
		this.origin = origin;
		zAxis = vectorU.cross(vectorV).normalize();
		checkSize(zAxis);
		xAxis = vectorU.normalize();
		checkSize(xAxis);
		yAxis = zAxis.cross(xAxis);
		checkSize(yAxis);
		checkOrthogonalities();
	}

	private void checkSize(Point axis) throws CoordinateSystemException {
		if (Math.abs(axis.size() - 1) > maxError) {
			throw new CoordinateSystemException("" + axis.size());
		}
	}

	private void checkOrthogonalities() throws CoordinateSystemException {
		checkOrthogonality(xAxis, yAxis);
		checkOrthogonality(xAxis, zAxis);
		checkOrthogonality(yAxis, zAxis);
	}

	private void checkOrthogonality(Point u, Point v) throws CoordinateSystemException {
		double dot = u.dot(v);
		double product = u.size() * v.size();
		double difference = Math.abs(dot / product);
		if (difference > maxError) {
			throw new CoordinateSystemException("" + difference);
		}
	}

	public Point expresPoint(Point p) {
		Point q = p.minus(origin);
		double[][] lhsArray = {
			{xAxis.x, yAxis.x, zAxis.x},
			{xAxis.y, yAxis.y, zAxis.y},
			{xAxis.z, yAxis.z, zAxis.z}
		};
		double[] rhsArray = {q.x, q.y, q.z};
		Matrix lhs = new Matrix(lhsArray);
		Matrix rhs = new Matrix(rhsArray, 3);
		Matrix ans = lhs.solve(rhs);
		Point result = new Point(ans.get(0, 0), ans.get(1, 0), ans.get(2, 0));
		return result;
	}

	
	public Point getXAxis() {
		return xAxis;
	}
	
	public Point getYAxis() {
		return xAxis;
	}
	public Point getZAxis() {
		return xAxis;
	}
}
