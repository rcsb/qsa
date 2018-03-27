package embedding;

import embedding.BallsDistanceTorsionVectorizer;
import embedding.RigidBody;
import geometry.metric.LpSpace;
import geometry.primitives.Point;
import geometry.superposition.Superposer;
import geometry.test.RandomBodies;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import junit.framework.TestCase;
import language.MathUtil;
import language.Pair;
import language.Util;
import structure.VectorizationException;
import testing.TestResources;
import vectorization.force.RigidBodyPair;

/**
 *
 * @author Antonin Pavelka
 */          
public class BallsDistanceTorsionVectorizerTest extends TestCase {

	private Random random = new Random(1);
	private RandomBodies randomBodies = new RandomBodies();
	//private BallsDistanceVectorizer vectorizer = new BallsDistanceVectorizer();
	private BallsDistanceTorsionVectorizer vectorizer = new BallsDistanceTorsionVectorizer();	
	//private QuaternionObjectPairVectorizer vectorizer = new QuaternionObjectPairVectorizer();
	//private DualQuaternionObjectPairVectorizer vectorizer = new DualQuaternionObjectPairVectorizer();
	//private BallsDihedralVectorizer vectorizer = new BallsDihedralVectorizer();
	private LpSpace space = new LpSpace(vectorizer.getDimensions());

	private TestResources resources = new TestResources();
	private final int cycles = 1;
	//private final int cycles = 1;
	private double[] xs = new double[cycles];
	private double[] ys = new double[cycles];

	public BallsDistanceTorsionVectorizerTest(String testName) {
		super(testName);
	}

	public void testVectorize() throws IOException, VectorizationException {
		File file = resources.getDirectoris().getQuaternionGraph();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write("rmsd,euclidean,chebyshev\n");
			for (int i = 0; i < cycles; i++) {
				compare(bw, i);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		double correlation = MathUtil.correlation(xs, ys);
		//if (correlation < 0.2) {
		System.out.println("");
		System.out.println("correlation: " + correlation);
		System.out.println("");
		//throw new RuntimeException("correlation: " + correlation);
		//}
	}

	private void compare(BufferedWriter bw, int index) throws IOException, VectorizationException {

		long seed = random.nextLong();
		seed = 5369118208594259420L;
		randomBodies.initSeed(seed);

		randomBodies.singularity = false;
		Point[][] x = randomBodies.createRandomOctahedronPair();
		randomBodies.singularity = false;
		Point[][] y = randomBodies.createRandomOctahedronPair();
		Pair<RigidBody> a = new Pair(RigidBody.create(x[0]), RigidBody.create(x[1]));
		Pair<RigidBody> b = new Pair(RigidBody.create(y[0]), RigidBody.create(y[1]));
		
		RigidBodyPair aa = new RigidBodyPair(RigidBody.create(x[0]), RigidBody.create(x[1]));
		/*System.out.println("rigid");
		System.out.println(a);
		System.out.println(b);
		System.out.println("//");*/

		float[] vx = vectorizer.vectorize(a._1, a._2, 0); // just first image ...
		double rmsd = rmsd(x, y);
		double[] euclideanDistances = new double[vectorizer.getNumberOfImages()];
		double[] chebyshevDistances = new double[vectorizer.getNumberOfImages()];
		for (int i = 0; i < vectorizer.getNumberOfImages(); i++) { // ... agains all images
			float[] vy = vectorizer.vectorize(b._1, b._2, i);
			euclideanDistances[i] = space.euclidean(vx, vy);
			chebyshevDistances[i] = space.chebyshev(vx, vy);
		}
		double euclideanDistance = Util.min(euclideanDistances);// / 1.7;
		double chebyshevDistance = Util.min(chebyshevDistances);
		//AxisAngle aa = RandomBodies.lastAxisAngle;
		bw.write(rmsd + "," + euclideanDistance + "," + chebyshevDistance + "," + "\n");
		//if (rmsd > 1 && euclideanDistance < 0.5) {
			//System.out.println("seed " + seed + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		//}
		xs[index] = rmsd;
		ys[index] = euclideanDistance;
		//throw new RuntimeException();
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

		/*double sum = 0;
		Point[] a = superposer.getTransformedYPoints();
		Point[] b = superposer.getXPoints();
		for (int i = 0; i < a.length; i++) {
			sum += a[i].distance(b[i]);
		}
		return sum / a.length;*/
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
