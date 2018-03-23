package geometry.random;

import geometry.primitives.MatrixRotation;
import geometry.primitives.Point;
import java.util.Random;
import javax.vecmath.Matrix3d;

public class RandomGeometry {

	private Random random;

	public RandomGeometry(Random random) {
		this.random = random;
	}

	public RandomGeometry() {
		this.random = new Random(1);
	}

	public Point randomVector() {
		Point vector = new Point(0, 0, 0);
		while (vector.size() == 0) {
			vector = new Point(randomShift(), randomShift(), randomShift());
		}
		vector = vector.normalize();
		return vector;
	}

	public Point[] rotateRandomly(Point[] points) {
		MatrixRotation matrix = new MatrixRotation(randomRotation());
		Point[] rotated = new Point[points.length];
		for (int i = 0; i < points.length; i++) {
			rotated[i] = matrix.rotate(points[i]);
		}
		return rotated;
	}

	private Matrix3d randomRotation() {
		Matrix3d x = new Matrix3d();
		x.rotX(randomAngle());
		Matrix3d y = new Matrix3d();
		y.rotY(randomAngle());
		Matrix3d z = new Matrix3d();
		z.rotZ(randomAngle());
		x.mul(y);
		x.mul(z);
		return x;
	}

	private double randomShift() {
		return (random.nextDouble() - 0.5) * 2;
	}

	public double randomAngle() {
		return (random.nextDouble() - 0.5) * Math.PI * 2;
	}
}
