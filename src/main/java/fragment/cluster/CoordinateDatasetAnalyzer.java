package fragment.cluster;

import algorithm.Biword;
import algorithm.BiwordsFactory;
import cath.Cath;
import fragment.Fragments;
import fragment.cluster.visualize.Visualizer;
import global.Parameters;
import global.io.Directories;
import java.io.File;
import java.util.List;
import java.util.Random;
import structure.SimpleStructure;
import structure.StructureSource;
import structure.Structures;
import testing.TestResources;

/**
 *
 * @author Antonin Pavelka
 */
public class CoordinateDatasetAnalyzer {

	private final Parameters parameters;
	private final Directories dirs;
	private final File fragmentFile;
	private final File clusterFile;
	private final Random random = new Random(1);

	private int fragmentSampleSize = 100000;
	private int numberOfStructures = 1000;

	private double clusteringTreshold = 3;
	private double searchThreshold = 2;

	public CoordinateDatasetAnalyzer(Parameters parameters, Directories dirs) {
		this.parameters = parameters;
		this.dirs = dirs;
		fragmentFile = dirs.getCoordinateFragments();
		clusterFile = dirs.getCluster();
	}

	public void run() {
		Fragments fragments;
		if (!fragmentFile.exists()) {
			fragments = generate(numberOfStructures); //!! 
			fragments.save(fragmentFile);
		} else {
			fragments = new Fragments();
			fragments.load(fragmentFile);
		}

		fragments.subsample(random, fragmentSampleSize); //!!

		Clusters clusters;
		/*if (!clusterFile.exists()) {
			clusters = cluster(fragments);
			clusters.save(clusterFile);
		} else {
			clusters = new Clusters();
			clusters.load(clusterFile);
		}*/
		clusters = cluster(fragments);

		System.out.println("Clusters: " + clusters.size());
		int total = 0;
		for (Cluster cluster : clusters) {
			total += cluster.size();
			//System.out.println("size " + cluster.size());
			//cluster.validate();
		}
		System.out.println("total in clusters: " + total);

		//clusters.shuffle(random);
		for (int i = 0; i < clusters.size(); i++) {
			Cluster cluster = clusters.get(i);
			//cluster.validate();
		}

		search(fragments, clusters);

		Visualizer visualizer = new Visualizer(clusters);
		visualizer.save(dirs.getFragmentPdb());
		//visualize(clusters.getRepresentants());
	}

	private void search(Fragments fragments, Clusters clusters) {
		FragmentPoints fragment = fragments.get(0);
		clusters.search(fragment, searchThreshold);
	}

	private Clusters cluster(Fragments fragments) {
		Clustering clustering = new Clustering(fragments);
		return clustering.cluster(clusteringTreshold);
	}

	private Fragments generate(int max) {
		Fragments fragments = new Fragments();
		int counter = 0;
		Cath cath = new Cath(dirs);
		Structures structures = new Structures(parameters, dirs, cath, "clustering");
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
				BiwordsFactory biwordsFactory = new BiwordsFactory(parameters, dirs, structure, 1, true);
				Biword[] biwords = biwordsFactory.getBiwords().getBiwords();
				for (Biword biword : biwords) {
					FragmentPoints fragment = new FragmentPoints(biword.getPoints3d());
					fragments.add(fragment);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return fragments;
	}

	public static void main(String[] args) {
		TestResources resources = new TestResources();
		CoordinateDatasetAnalyzer m = new CoordinateDatasetAnalyzer(
			resources.getParameters(),
			resources.getDirectoris());
		m.run();
	}
}
