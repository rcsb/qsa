package geometry.primitives;

import geometry.metric.ChebyshevDistance;
import geometry.metric.MetricDistance;
import geometry.superposition.Superposer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import junit.framework.TestCase;
import language.Pair;
import testing.TestResources;

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

	public void testGetVectorRepresentation() {
		File file = resources.getDirectoris().getAxisAngleGraph();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			for (int i = 0; i < cycles; i++) {
				compare(bw);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void compare(BufferedWriter bw) throws IOException {
		long seed = randomSeed.nextInt();
		//seed = 33976762;
		random.setSeed(seed);

		Point[] x = rotateRandomly(sphere);
		Point[] y = rotateRandomly(sphere);

		Point vectorX = getVectorizedRotation(new Pair(sphere, x));
		Point vectorY = getVectorizedRotation(new Pair(sphere, y));

		AxisAngle aaX = AxisAngleFactory.toAxisAngle(getRotationMatrix(new Pair(sphere, x)));
		AxisAngle aaY = AxisAngleFactory.toAxisAngle(getRotationMatrix(new Pair(sphere, y)));

		//MetricDistance metric = new EuclideanDistance();
		MetricDistance metric = new ChebyshevDistance();

		double vectorDistance = vectorDistance(new Pair(vectorX, vectorY), metric); // diff of vectors for both sphere pairs
		double objectDistance = getObjectDistance(new Pair(x, y)); // how different the second, rotated, spheres are

		if (vectorDistance < 0.1 && objectDistance > 170) {
			System.out.println("seed " + seed);
			Point vx = aaX.getVectorRepresentation();
			Point vy = aaY.getVectorRepresentation();
			System.out.println("  " + vectorDistance + " " + objectDistance);
			System.out.println("vx " + vx.size());
			System.out.println("vy " + vy.size());
			System.out.println("angle " + vx.angle(vy) / Math.PI * 180);
			System.out.println("");
			System.out.println(vectorDistance + "," + objectDistance + " ***");
			System.out.println();
			System.out.println(vectorX);
			System.out.println(vectorY);
			System.out.println("aa " + aaX);
			System.out.println("aa " + aaY);
			System.out.println("---");
			for (Point p : x) {
				System.out.println(p);
			}
			System.out.println("---");
			for (Point p : y) {
				System.out.println(p);
			}
			System.out.println("------");
		}
		bw.write(vectorDistance + "," + objectDistance + "\n");
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
		/*		double max = 0;
		for (int i = 0; i < objects._1.length; i++) {
			double d = objects._1[i].distance(objects._2[i]);
			if (d > max) {
				max = d;
			}
		}
		return max;*/
	}

	/**
	 * for grid: simply search both d and e areas no need for square morphing
	 *
	 * this is the best for postprocess, to replace QCP
	 *
	 */
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

	private Point[] createSphereSurface() {
		Point[] sphere = {
			new Point(1, 0, 0),
			new Point(-1, 0, 0),
			new Point(0, 1, 0),
			new Point(0, -1, 0),
			new Point(0, 0, 1),
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

	// check if this corresponds to axis4d, try to use axis to generatte this
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
