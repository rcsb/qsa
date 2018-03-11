package geometry.test;

import geometry.primitives.Point;
import java.util.Random;
import javax.vecmath.Matrix3d;

/**
 *
 * @author Antonin Pavelka
 */
public class RandomBodies {

	private Random random = new Random(1);

	public Point[][] createRandomOctahedronPair() {
		Point[][] pair = {createRandomOctahedron(), createRandomOctahedron()};
		return pair;
	}

	private Point[] createRandomOctahedron() {
		Point[] octa = createOctahedron();
		octa = rotateRandomly(octa);
		//octa = translateRandomly(octa);
		return octa;
	}

	private Point[] createOctahedron() {
		Point[] sphere = {
			new Point(1, 0, 0),
			new Point(0, 1, 0),
			new Point(0, 0, 1),
			new Point(-1, 0, 0),
			new Point(0, -1, 0),
			new Point(0, 0, -1)
		};
		return sphere;
	}

	private Point[] rotateRandomly(Point[] points) {
		Matrix3d rotation = randomRotation();
		Point[] rotated = new Point[points.length];
		for (int i = 0; i < points.length; i++) {
			rotated[i] = points[i].transform(rotation);
		}
		return rotated;
	}

	private Point[] translateRandomly(Point[] points) {
		Point shift = randomShift();
		for (int i = 0; i < points.length; i++) {
			points[i] = points[i].plus(shift);
		}
		return points;
	}

	private Point randomShift() {
		Point vector = new Point(0, 0, 0);
		while (vector.size() == 0) {
			vector = new Point(shift(), shift(), shift());
		}
		vector = vector.normalize();
		double size = random.nextDouble() * 3;
		return vector.multiply(size);
	}

	private double shift() {
		return (random.nextDouble() - 0.5) * 50;
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

	private double randomAngle() {
		return random.nextDouble() * Math.PI * 2;
	}
}
