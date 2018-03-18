package geometry.primitives;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 */
public class MatrixRotation {

	private Matrix3d matrix;

	public MatrixRotation(Matrix3d matrix) {
		this.matrix = matrix;
	}

	public Point rotate(Point x) {
		Point3d p3d = x.toPoint3d();
		matrix.transform(p3d);
		return new Point(p3d);
	}
}
