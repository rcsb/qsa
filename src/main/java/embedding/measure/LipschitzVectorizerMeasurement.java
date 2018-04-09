package embedding.measure;

import algorithm.Biword;
import algorithm.BiwordsFactory;
import analysis.Heatmap;
import analysis.statistics.Distribution2d;
import cath.Cath;
import embedding.lipschitz.LipschitzEmbedding;
import fragment.Fragments;
import fragment.cluster.Fragment;
import metric.LpSpace;
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
import java.util.function.BiFunction;
import structure.SimpleStructure;
import structure.StructureSource;
import structure.Structures;
import structure.VectorizationException;
import testing.TestResources;
import vectorization.dimension.DimensionOpen;
import vectorization.dimension.Dimensions;

/**
 *
 * @author Antonin Pavelka
 */
public class LipschitzVectorizerMeasurement {

	private int dimensions = 10;
	private Random random = new Random(2);
	private LpSpace space = new LpSpace(new Dimensions(new DimensionOpen(), dimensions));

	private TestResources resources = new TestResources();
	private final int cycles = 1000000;

	private Directories dirs = resources.getDirectoris();
	private Parameters parameters = resources.getParameters();

	private int numberOfStructures = 10000000;
	private int fragmentSampleSize = 10000000;
	private int optimizationCycles = 1000;

	private Fragments fragments;
	private LipschitzEmbedding embedding;

	Distribution2d rmsdChebyshev = new Distribution2d();

	public void run() throws IOException, VectorizationException {
		generateFragments();
		createEmbedding();
		measure();
		analyze();
		plot();
	}

	private void plot() {
		int resolution = 600;
		Heatmap plot = new Heatmap(0, 0, 10, 10, resolution, resolution, dirs.getHeatmapColors());
		for (int i = 0; i < rmsdChebyshev.size(); i++) {
			plot.add(rmsdChebyshev.getX(i), rmsdChebyshev.getY(i));
		}
		plot.save(dirs.getRmsdChebyshevPlot());
	}

	private void generateFragments() throws IOException, VectorizationException {
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

	private void createEmbedding() {
		embedding = new LipschitzEmbedding(fragments.getArray(), dimensions, optimizationCycles);
	}

	private void measure() throws IOException, VectorizationException {
		File file = resources.getDirectoris().getQuaternionGraph();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write("rmsd,euclidean,chebyshev\n");
			for (int i = 0; i < cycles; i++) {
				compare(bw);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		//double correlation = MathUtil.correlation(xs, ys);
		//System.out.println("");
		//System.out.println("correlation: " + correlation);
		//System.out.println("");
	}

	private void analyze() {
		for (double threshold = 1; threshold <= 2.1; threshold += 0.25) {
			final double t = threshold;
			BiFunction<Double, Double, Boolean> tpSelection = (x, y) -> {
				return x <= t && y <= t;
			};
			BiFunction<Double, Double, Boolean> fpSelection = (x, y) -> {
				return x >= t && y <= t;
			};
			double tp = rmsdChebyshev.getPercentage(tpSelection);
			double fp = rmsdChebyshev.getPercentage(fpSelection);
			System.out.println("threshold = " + threshold);
			System.out.println("TP = " + tp);
			System.out.println("FP = " + fp);
			System.out.println("efficiency = " + (tp / fp));

		}
	}

	private void compare(BufferedWriter bw) throws IOException, VectorizationException {
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
		rmsdChebyshev.add(rmsd, chebyshevDistance);
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

	public static void main(String[] args) throws Exception {
		LipschitzVectorizerMeasurement m = new LipschitzVectorizerMeasurement();
		m.run();
	}

}
