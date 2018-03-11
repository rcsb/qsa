package geometry.primitives;

import geometry.metric.ChebyshevDistance;
import geometry.metric.EuclideanDistance;
import geometry.metric.MetricDistance;
import geometry.superposition.Superposer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import junit.framework.TestCase;
import language.Pair;
import structure.VectorizationException;
import testing.TestResources;
import vectorization.QuaternionObjectPairVectorizer;
import vectorization.RigidBody;

/**
 *
 * @author Antonin Pavelka
 */
public class AxisAngleTest extends TestCase {

	private Random random = new Random(1);
	private Random randomSeed = new Random(1);
	private Point[] sphere = createSphereSurface();
	private TestResources resources = new TestResources();

	//int cycles = 1000000000;
	int cycles = 10000;

	public AxisAngleTest(String testName) {
		super(testName);
	}

	public void testSphereToCube() {
		Matrix3d matrix = new Matrix3d();
		matrix.rotX(50);
		AxisAngle axisAngle = AxisAngleFactory.toAxisAngle(matrix);
		Point sphere = new Point(1, 2, 3).normalize().divide(2);
		Point cube = axisAngle.sphereToCube(sphere);
		assert cube.close(new Point(0.16666666666666666, 0.3333333333333333, 0.5));
		double colinearity = Math.abs(1 - sphere.dot(cube) / sphere.size() / cube.size());
		assert colinearity < 0.000001;

	}

	public void testGetVectorRepresentation() throws VectorizationException {
		File file = resources.getDirectoris().getAxisAngleGraph();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write("angle,objectDistance,euclideanDistance,chebyshevDistance\n");
			for (int i = 0; i < cycles; i++) {
				compare(bw);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private double angle(Matrix3d m1, Matrix3d m2) {
		Matrix3d m = new Matrix3d(m1);
		m.transpose();
		m.mul(m2);
		AxisAngle axisAngle = AxisAngleFactory.toAxisAngle(m);
		return axisAngle.getAngleInDegrees();
	}

	private void compare(BufferedWriter bw) throws IOException, VectorizationException {
		long seed = randomSeed.nextInt();
		random.setSeed(seed);

		Point[] x = rotateRandomly(sphere);
		Point[] y = rotateRandomly(sphere);

		Point vectorX = getVectorizedRotation(new Pair(sphere, x));
		Point vectorY = getVectorizedRotation(new Pair(sphere, y));

		Matrix3d m1 = getRotationMatrix(new Pair(sphere, x));
		Matrix3d m2 = getRotationMatrix(new Pair(sphere, y));

		Quat4d q1 = new Quat4d();
		q1.set(m1);
		Quat4d q2 = new Quat4d();
		q2.set(m2);

		AxisAngle aaX = AxisAngleFactory.toAxisAngle(m1);
		AxisAngle aaY = AxisAngleFactory.toAxisAngle(m2);

		double euclideanDistance = vectorDistance(new Pair(vectorX, vectorY), new EuclideanDistance());
		double chebyshevDistance = vectorDistance(new Pair(vectorX, vectorY), new ChebyshevDistance());
		double objectDistance = getObjectDistance(new Pair(x, y)); // how different the second, rotated, spheres are
		double angle = angle(m1, m2);

		bw.write(angle + "," + objectDistance + "," + euclideanDistance + "," + chebyshevDistance + ","
			+ getQuaternionDistance(x, y) + "," + getEuclidean(q1, q2) + "," + getChebyshev(q1, q2) + "\n");
	}

	private double getQuaternionDistance(Point[] x, Point[] y) throws VectorizationException {
		RigidBody b1 = new RigidBody(sphere);
		RigidBody b2 = new RigidBody(x);
		RigidBody b3 = new RigidBody(y);
		QuaternionObjectPairVectorizer vectorizer = new QuaternionObjectPairVectorizer();
		float[] u = vectorizer.vectorize(b1, b2, 0);
		float[] v = vectorizer.vectorize(b1, b3, 0);
		return getEuclidean(u, v);
	}

	private double getEuclidean(Quat4d u, Quat4d v) {
		double a = Math.abs(u.x - v.x);
		double b = Math.abs(u.y - v.y);
		double c = Math.abs(u.z - v.z);
		double d = Math.abs(u.w - v.w);
		return Math.sqrt(a * a + b * b + c * c + d * d);
	}

	private double getChebyshev(Quat4d u, Quat4d v) {
		double a = Math.abs(u.x - v.x);
		double b = Math.abs(u.y - v.y);
		double c = Math.abs(u.z - v.z);
		double d = Math.abs(u.w - v.w);
		return Math.max(a, Math.max(b, Math.max(c, d)));
	}

	private double getEuclidean(float[] u, float[] v) {
		double sum = 0;
		for (int i = 0; i < u.length; i++) {
			double d = Math.abs(u[i] - v[i]);
			sum += d * d;
		}
		return Math.sqrt(sum);
	}

	private Point getVectorizedRotation(Pair<Point[]> objects) {
		AxisAngle axisAngle = AxisAngleFactory.toAxisAngle(getRotationMatrix(objects));
		Point vector = axisAngle.getVectorRepresentation();
		return vector;
	}

	private double getObjectDistance(Pair<Point[]> objects) {
		Matrix3d rotation = getRotationMatrix(objects);
		AxisAngle axisAngle = AxisAngleFactory.toAxisAngle(rotation);
		return axisAngle.getAngleInDegrees();
	}

	private double vectorDistance(Pair<Point> vectors, MetricDistance metric) {
		vectors = order(vectors); // zero vector has no direction
		Point x = vectors._1; // bigger
		Point y = vectors._2;
		double distance = metric.distance(x, y);
		Point xWrapped = wrap(x);
		double distanceWrapped = metric.distance(xWrapped, y);
		return Math.min(distance, distanceWrapped);
	}

	private Pair<Point> order(Pair<Point> vectors) {
		Point x = vectors._1;
		Point y = vectors._2;
		if (x.size() < y.size()) {
			Point z = x;
			x = y;
			y = z;
		}
		return new Pair(x, y);
	}

	private Point wrap(Point x) {
		Point unit = x.normalize();
		return x.minus(unit.multiply(2));
	}

	private Matrix3d getRotationMatrix(Pair<Point[]> objects) {
		Superposer transformer = new Superposer();
		transformer.set(objects._1, objects._2);
		return transformer.getRotationMatrix();
	}

	private Versor getQuaternion(Pair<Point[]> objects) {
		Superposer transformer = new Superposer();
		transformer.set(objects._1, objects._2);
		return transformer.getQuaternion();
	}

	private Point[] createSphereSurface() {
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

	private Point createRandomUnit() {
		Point point = new Point(1, 0, 0);
		Matrix3d rotation = randomRotation();
		Point3d randomUnit = point.toPoint3d();
		rotation.transform(randomUnit);
		return new Point(randomUnit);
	}

	private Point[] rotateRandomly(Point[] points) {
		Matrix3d rotation = randomRotation();
		Point[] rotated = new Point[points.length];
		for (int i = 0; i < points.length; i++) {
			rotated[i] = points[i].transform(rotation);
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

	private double randomAngle() {
		return random.nextDouble() * Math.PI * 2;
	}

}
