package embedding.measure;

import algorithm.Biword;
import algorithm.BiwordsFactory;
import cath.Cath;
import embedding.lipschitz.LipschitzEmbedding;
import fragment.Fragments;
import fragment.cluster.Fragment;
import geometry.metric.LpSpace;
import geometry.primitives.Point;
import geometry.superposition.Superposer;
import global.Parameters;
import global.io.Directories;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import language.MathUtil;
import language.Util;
import structure.SimpleStructure;
import structure.StructureSource;
import structure.Structures;
import structure.VectorizationException;
import testing.TestResources;

/**
 *
 * @author Antonin Pavelka
 */
public class LipschitzVectorizerMeasurement {

	private Random random = new Random(1);
	private LpSpace space;

	private TestResources resources = new TestResources();
	private final int cycles = 10000;
	private double[] xs = new double[cycles];
	private double[] ys = new double[cycles];

	private Directories dirs = resources.getDirectoris();
	private Parameters parameters = resources.getParameters();

	private int numberOfStructures = 10000000;
	private int fragmentSampleSize = 10000000;

	private Fragments fragments;
	private LipschitzEmbedding embedding;

	public void run() throws IOException, VectorizationException {
		generateFragments();
		embedding = new LipschitzEmbedding(fragments.getArray(), 20);
		measure();
	}

	public void generateFragments() throws IOException, VectorizationException {
		File fragmentFile = dirs.getCoordinateFragments();
		if (!fragmentFile.exists()) {
			fragments = generate(numberOfStructures); //!! 
			fragments.save(fragmentFile);
		} else {
			fragments = new Fragments();
			fragments.load(fragmentFile);
		}
		fragments.subsample(random, fragmentSampleSize); //!!
	}

	public void measure() throws IOException, VectorizationException {
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
		System.out.println("");
		System.out.println("correlation: " + correlation);
		System.out.println("");
	}

	private void compare(BufferedWriter bw, int index) throws IOException, VectorizationException {
		/*long seed = random.nextLong();
		seed = 5369118208594259420L;
		randomBodies.initSeed(seed);*/
		Fragment a = fragments.get(random.nextInt(fragments.size()));
		Fragment b = fragments.get(random.nextInt(fragments.size()));

		double rmsd = a.getDistance(b);

		float[] va = embedding.getCoordinates(a);
		float[] vb = embedding.getCoordinates(b);

		double euclideanDistance = space.euclidean(va, vb);
		double chebyshevDistance = space.chebyshev(va, vb);

		bw.write(rmsd + "," + euclideanDistance + "," + chebyshevDistance + "," + "\n");
		xs[index] = rmsd;
		ys[index] = euclideanDistance;		
	}

	private Fragments generate(int max) {
		Fragments fragments = new Fragments();
		int counter = 0;
		Cath cath = new Cath(resources.getDirectoris());
		Structures structures = new Structures(resources.getParameters(), resources.getDirectoris(), cath, "clustering");
		List<StructureSource> sources = cath.getHomologousSuperfamilies().getRepresentantSources();
		structures.addAll(sources);
		for (SimpleStructure structure : structures) {
			try {
				System.out.println(counter);
				counter++;
				if (counter > max) {
					return fragments;
				}
				System.out.println("  " + structure.getSource());
				BiwordsFactory biwordsFactory = new BiwordsFactory(resources.getParameters(), resources.getDirectoris(), structure, 1, true);
				Biword[] biwords = biwordsFactory.getBiwords().getBiwords();
				for (Biword biword : biwords) {
					Fragment fragment = new Fragment(biword.getPoints3d());
					fragments.add(fragment);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return fragments;
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

	public static void main(String[] args) throws Exception {
		LipschitzVectorizerMeasurement m = new LipschitzVectorizerMeasurement();
		m.run();
	}

}
