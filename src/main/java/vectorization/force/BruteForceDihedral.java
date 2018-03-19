package vectorization.force;

import geometry.primitives.MatrixRotation;
import geometry.primitives.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.vecmath.Matrix3d;
import vectorization.RigidBody;

/**
 *
 * @author Antonin Pavelka
 */
public class BruteForceDihedral {

	private static Random random = new Random(1);
	private static final double s3 = Math.sqrt(3) / 2;
	private int n = 100;
	private List<RigidBodyPair> bodies = new ArrayList<>();

	// try to use two closest for optimization?
	public BruteForceDihedral() {
	}

	private void run() {
		for (int i = 0; i < n; i++) {
			RigidBodyPair x = generate();
			add(x);
		}
	}
	
	private void add(RigidBodyPair x) {
		
	}
	
	private RigidBodyPair generate() {
		RigidBody b1 = RigidBody.create(randomTriangle());
		RigidBody b2 = RigidBody.create(randomTriangle());
		b2 = b2.translate(randomShift());
		return new RigidBodyPair(b1,b2);
	}

	private Point randomShift() {
		Point vector = new Point(0, 0, 0);
		while (vector.size() == 0) {
			vector = new Point(shift(), shift(), shift());
		}
		vector = vector.normalize();
		return vector.multiply(5);

	}

	private double shift() {
		return (random.nextDouble() - 0.5) * 2 + 2;
	}

	private Point[] randomTriangle() {
		return rotateRandomly(createTriangle());
	}

	private Point[] rotateRandomly(Point[] points) {
		MatrixRotation matrix = new MatrixRotation(randomRotation());
		Point[] rotated = new Point[points.length];
		for (int i = 0; i < points.length; i++) {
			rotated[i] = matrix.rotate(points[i]);
		}
		return rotated;
	}

	private Point[] createTriangle() {
		Point[] triangle = {
			new Point(s3, 0.5, 0),
			new Point(-s3, 0.5, 0),
			new Point(0, -1, 0)
		};

		return triangle;
	}

	private Matrix3d randomRotation() {

		//index = 5000;
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
		return (random.nextDouble() - 0.5) * Math.PI * 2;
	}

	

	public static void main(String[] args) {
		BruteForceDihedral m = new BruteForceDihedral();
		m.run();
	}
}
