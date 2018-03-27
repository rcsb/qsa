package geometry.test;

import geometry.primitives.AxisAngle;
import geometry.primitives.AxisAngleFactory;
import geometry.primitives.MatrixRotation;
import geometry.primitives.Point;
import java.util.Random;
import javax.vecmath.Matrix3d;
import language.Pair;
import embedding.RigidBody;

/**
 *
 * @author Antonin Pavelka
 */
public class RandomBodies {

	private Random random = new Random(1);
	// TODO rotation reliable? Test? Internal distances?
	// better random shifts, RigidBody?
	public boolean singularity;

	// TODO more and rotate as a whole, just in case
	// replaced by RandomTriangles
	@Deprecated
	public Point[][] createRandomOctahedronPair() {
		Point[] a = createRandomOctahedron();
		Point[] b = createRandomOctahedronShifted();
		Point[][] pair = {a, b};
		return pair;
	}

	public void initSeed(long seed) {
		random.setSeed(seed);
	}

	private Point[] createRandomOctahedron() {
		Point[] octa = createOctahedron();
		octa = rotateRandomly(octa);
		//octa = translateRandomly(octa);
		return octa;
	}

	private Point[] createRandomOctahedronShifted() {
		Point[] octa = createOctahedron();
		octa = rotateRandomly(octa);
		octa = translateRandomly(octa); 
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

	public Pair<RigidBody> createDummiesZ(Point origin, double xyAngle) {
		Point[] auxiliary = {new Point(1, -0.1, 0), new Point(1, 0.1, 0)};
		RigidBody body = new RigidBody(new Point(0, 0, 0), auxiliary);

		RigidBody a = body;
		RigidBody b = rotateZ(body, xyAngle).translate(new Point(1, 0, 0));
		return new Pair(a, b);
	}

	public Pair<RigidBody> createDummiesX(Point origin, double xAngle) {
		Point[] auxiliary = {new Point(0.1, 1, 0), new Point(-0.1, 1, 0)};
		RigidBody body = new RigidBody(new Point(0, 0, 0), auxiliary);

		MatrixRotation rotation = new MatrixRotation(randomRotation());

		double yAngle = randomAngle();
		double zAngle = 0;//randomAngle();
		MatrixRotation rotationYZ = new MatrixRotation(randomRotationYZ(yAngle, zAngle));

		RigidBody a = body.rotate(rotation);
		RigidBody b = rotateX(body, xAngle).rotate(rotationYZ).translate(new Point(1, 0, 0)).rotate(rotation);

		//RigidBody a = body;
		//RigidBody b = rotateX(body, xAngle).translate(new Point(1, 0, 0));
		return new Pair(a, b);
	}

	private RigidBody rotateZ(RigidBody body, double angle) {
		Matrix3d z = new Matrix3d();
		z.rotZ(angle);
		MatrixRotation rotation = new MatrixRotation(z);
		return body.rotate(rotation);
	}

	private RigidBody rotateX(RigidBody body, double angle) {
		Matrix3d x = new Matrix3d();
		x.rotX(angle);
		MatrixRotation rotation = new MatrixRotation(x);
		return body.rotate(rotation);
	}

	private RigidBody rotateRandomly(RigidBody body) {
		Matrix3d rotation = randomRotation();
		return body.rotate(new MatrixRotation(rotation));
	}
	public static AxisAngle lastAxisAngle;

	private Point[] rotateRandomly(Point[] points) {
		Matrix3d rotation = randomRotation();

		lastAxisAngle = AxisAngleFactory.toAxisAngle(rotation);
		//System.out.println(lastAxisAngle.getAngle() + "!");

		//System.out.println("angle: " + aa.getAngle() + " axis: " + aa.getAxis() );
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

	private Point[] translate(Point[] points) {
		for (int i = 0; i < points.length; i++) {
			points[i] = points[i].plus(new Point(5, 20, 3));
		}
		return points;
	}

	private Point randomShift() {
		Point vector = new Point(0, 0, 0);
		while (vector.size() == 0) {
			vector = new Point(shift(), shift(), shift());
		}
		//vector = new Point(1, 2, 3);// ~!!!!!!!!!!!!!!!!!!!!!!
		//vector = new Point(0, 0, 1);// ~!!!!!!!!!!!!!!!!!!!!!!
		vector = vector.normalize();
		double size = random.nextDouble() * 5 + 3;
		size = 5.5;                                          // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		return vector.multiply(size);
	}

	private double shift() {
		return (random.nextDouble() - 0.5) * 2 + 2;
	}

	/*private Matrix3d randomRotation() {
		Matrix3d x = new Matrix3d();
		x.rotX(randomAngle());
		Matrix3d y = new Matrix3d();
		y.rotY(randomAngle());
		Matrix3d z = new Matrix3d();
		z.rotZ(randomAngle());
		x.mul(y);
		x.mul(z);
		index++;
		return x;
	}*/
	private Matrix3d randomRotation() {
		Matrix3d x = new Matrix3d();
		boolean safe = false;
		while (!safe) {
			x.rotX(next());
			Matrix3d y = new Matrix3d();
			y.rotY(next());
			Matrix3d z = new Matrix3d();
			z.rotZ(next());
			x.mul(y);
			x.mul(z);
			/*singularity  = true;
			if (singularity) {
				safe = 0.000002 > Math.abs(AxisAngleFactory.toAxisAngle(x).getAngle() - Math.PI);
				System.out.println("safe");
			} else {
				safe = 0.000002 < Math.abs(AxisAngleFactory.toAxisAngle(x).getAngle() - Math.PI);
			}*/
			safe = true;
		}
		index += 1.0 / 10000;
		rrr++;
		return x;
	}

	private Matrix3d randomRotationYZ(double yAngle, double zAngle) {
		Matrix3d m = new Matrix3d();
		m.rotY(yAngle);
		Matrix3d z = new Matrix3d();
		m.rotZ(zAngle);
		return m;
	}
	int rrr;
	double index;

	private double next() {
		return randomAngle();/*
		if (rrr % 2 == 0) {
			return randomAngle();
		} else {
			return randomAngle() + Math.PI/2;
		}*/

 /*if (rrr % 2 == 0) {
			return index;
		} else {
			return index + 1;
		}*/
	}

	private double randomAngle() {
		return (random.nextDouble() - 0.5) * Math.PI * 2;
	}
}
