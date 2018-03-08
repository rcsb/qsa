package vectorization;

import geometry.metric.Metric;
import geometry.primitives.Point;
import geometry.superposition.Superposer;
import geometry.test.RandomBodies;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import junit.framework.TestCase;
import structure.VectorizationException;
import testing.TestResources;

/**
 *
 * @author Antonin Pavelka
 */
public class QuaternionObjectPairVectorizerTest extends TestCase {

	private RandomBodies randomBodies = new RandomBodies();
	private QuaternionObjectPairVectorizer vectorizer = new QuaternionObjectPairVectorizer();
	private TestResources resources = new TestResources();
	private final int cycles = 200000;

	public QuaternionObjectPairVectorizerTest(String testName) {
		super(testName);
	}

	public void testVectorize() throws IOException, VectorizationException {
		File file = resources.getDirectoris().getQuaternionGraph();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write("rmsd,euclidean,chebyshev\n");
			for (int i = 0; i < cycles; i++) {
				compare(bw);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void compare(BufferedWriter bw) throws IOException, VectorizationException {
		Point[][] x = randomBodies.createRandomOctahedronPair();
		Point[][] y = randomBodies.createRandomOctahedronPair();

		RigidBody bx1 = new RigidBody(x[0]);
		RigidBody bx2 = new RigidBody(x[1]);

		RigidBody by1 = new RigidBody(y[0]);
		RigidBody by2 = new RigidBody(y[1]);

		float[] vx = vectorizer.vectorize(bx1, bx2);
		float[] vy = vectorizer.vectorize(by1, by2);

		double rmsd = rmsd(x, y);
		double objectDistancePrimitive = computeObjectDistancePrimitive(x, y);

		double euclideanDistance = Metric.euclidean(vx, vy);
		double chebyshevDistance = Metric.chebyshev(vx, vy);

		
		double internal = Metric.euclidean(
			internalDistances(flat(x)),
			internalDistances(flat(y)));
		
		//System.out.println(objectDistance);
		//System.out.println("");
		bw.write(rmsd + "," + euclideanDistance + "," + chebyshevDistance + "\n");
		//bw.write(rmsd + "," + internal + "\n");

		

		//if (rmsd < 0.1 && euclideanDistance > 1) {
		if ((internal < 0.2 && rmsd > 0.5) || (rmsd < 0.2 && internal > 0.5)) {
			System.out.println("internal = " + internal);
			System.out.println("rmsd = " + rmsd);
			System.out.println("quaternions");
			printVector(vx);
			printVector(vy);
			System.out.println("object1");
			print(flat(x));
			System.out.println("object2");
			print(flat(y));
			System.out.println("------");
		}
	}

	private double computeObjectDistancePrimitive(Point[][] x, Point[][] y) {
		double sum = 0;
		int n = 0;
		for (int i = 0; i < 2; i++) {
			for (int k = 0; k < 2; k++) {
				sum += x[i][k].distance(y[i][k]);
				n++;
			}
		}
		return sum / n;
	}

	private double rmsd(Point[][] x, Point[][] y) {

		Superposer superposer = new Superposer();

		Point[] xf = flat(x);
		Point[] yf = flat(y);

		//System.out.println("vvv");
		//print(xf);
		//System.out.println("-");
		//print(yf);
		//System.out.println("---");
		superposer.set(xf, yf);
		return superposer.getRmsd();
	}

	private Point[] flat(Point[][] points) {
		Point[] flat = new Point[points[0].length + points[1].length];
		for (int i = 0; i < points[0].length; i++) {
			flat[i] = points[0][i];
		}
		for (int i = 0; i < points[1].length; i++) {
			flat[i + points[0].length] = points[1][i];
		}
		return flat;
	}

	private float[] internalDistances(Point[] points) {
		float[] a = new float[points.length * (points.length - 1) / 2];
		int i = 0;
		for (int x = 0; x < points.length; x++) {
			for (int y = 0; y < x; y++) {
				a[i++] = (float) points[x].distance(points[y]);
			}
		}
		return a;
	}

	private void printVector(float[] vector) {
		for (float v : vector) {
			System.out.print(v + " ");
		}
		System.out.println("");
	}

	private void print(Point[] points) {
		for (Point p : points) {
			System.out.println(p);
		}
	}

}
