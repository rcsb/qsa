package geometry.primitives;

import geometry.superposition.Transformer;
import global.io.LineFile;
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
	private int n = 1000;
	private Point[] sphere = createSphereSurface();
	private TestResources resources = new TestResources();

	int m = 10000;

	public AxisAngleTest(String testName) {
		super(testName);
	}

	public void testGetVectorRepresentation() {
		File file = resources.getDirectoris().getAxisAngleGraph();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			for (int i = 0; i < m; i++) {
				compare(bw);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void compare(BufferedWriter bw) throws IOException {
		Point[] x = rotateRandomly(sphere);
		Point[] y = rotateRandomly(sphere);

		Point vectorX = getVectorizedRotation(new Pair(sphere, x));
		Point vectorY = getVectorizedRotation(new Pair(sphere, y));

		double vectorDistance = vectorDistanceEuclidean(new Pair(vectorX, vectorY)); // diff of vectors for both sphere pairs
		//double vectorDistance = vectorDistanceChebyshev(new Pair(vectorX, vectorY)); 
		double objectDistance = getObjectDistance(new Pair(x, y)); // how different the second, rotated, spheres are

		bw.write(vectorDistance + "," + objectDistance + "\n");
	}

	private Point getVectorizedRotation(Pair<Point[]> objects) {
		AxisAngle axisAngle = new AxisAngle(getRotationMatrix(objects));
		Point vector = axisAngle.getVectorRepresentation();
		return vector;
	}

	private double getObjectDistance(Pair<Point[]> objects) {
		double max = 0;
		for (int i = 0; i < n; i++) {
			double d = objects._1[i].distance(objects._2[i]);
			if (d > max) {
				max = d;
			}
		}
		return max;
	}

	private double vectorDistanceEuclidean(Pair<Point> vectors) {
		double sum = 0;
		for (int i = 0; i < 3; i++) {
			double d = closedDistance(vectors._1.getCoords()[i], vectors._2.getCoords()[i]);
			sum += d * d;
		}
		return Math.sqrt(sum);
	}

	private double vectorDistanceChebyshev(Pair<Point> vectors) {
		double max = 0;
		for (int i = 0; i < 3; i++) {
			double d = closedDistance(vectors._1.getCoords()[i], vectors._2.getCoords()[i]);
			if (d > max) max = d;
		}
		return max;
	}

	private double closedDistance(double a, double b) {
		if (a > b) {
			double pom = a;
			a = b;
			b = pom;
		}
		double dif = b - a;
		if (b - a > 1) {
			dif = 2 - dif;
		}
		assert dif >= 0;
		assert dif <= 1 : a + " " + b + " " + dif;
		return dif;
	}

	private Matrix3d getRotationMatrix(Pair<Point[]> objects) {
		Transformer transformer = new Transformer();
		transformer.set(objects._1, objects._2);
		return transformer.getRotationMatrix();
	}

	private Point[] createSphereSurface() {
		Point[] sphere = new Point[n];
		for (int i = 0; i < n; i++) {
			sphere[i] = createRandomUnit();
		}
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
