package biword;

import fragments.Biword;
import fragments.Biwords;
import fragments.Parameters;
import fragments.WordImpl;
import fragments.space.Cluster;
import fragments.space.WordSpace;
import geometry.Transformer;
import io.Directories;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordHashClustering {

	private static Parameters params = Parameters.create();
	private static Random random = new Random(1);
	private static Directories dirs = Directories.createDefault();

	private List<WordImpl> a = new ArrayList<>(); // biword
	private List<WordImpl> b = new ArrayList<>(); // biword
	private List<Cluster> clusters = new ArrayList<>();
	private Transformer tr = new Transformer();
	private int bwCount;
	private long totalCount;
	private long biwordCount;

	Point3d[][] wordRepresentants;

	private List[][] tree; // if all fits here, test the fraction returned upon random query
	// switch to 8-word

	public BiwordHashClustering() throws IOException {
		wordRepresentants = WordSpace.readAll(dirs.getWordRepresentants(params.getWordClusteringThreshold()));
		int n = wordRepresentants.length; // number of word clusters
		tree = new List[n][n];
	}

	private void createBase() {
		// load sufficiently large sample
		// pick base one by one and in several iterations select bw with greatest distance to all known bases
	}
	
	
	// try multidimensional array with hashmap, should be fast for this, no other choice
	private byte[] getCell(Biword bw) {
		
	}
	
	private int getHashCode(byte[] cell) {
		
		
	}
	
	private void add(Biword bw) {
		WordImpl[] words = bw.getWords();
		List<Integer> as = getLabels(words[0].getPoints3d());
		List<Integer> bs = getLabels(words[1].getPoints3d());
		for (int a : as) {
			for (int b : bs) {
				if (tree[a][b] == null) {
					tree[a][b] = new ArrayList<>();
				}
				tree[a][b].add(bw);
				//if (tree[a][b] > 1) System.out.println("t " + tree[a][b]);
				totalCount++;
				// save points into file
				// try dist, maybe some int.
				
			}
		}
		biwordCount++;
		System.out.println(totalCount + " " + biwordCount);
	}

	public void measure() {
		BiwordsProvider bp = new BiwordsProvider();
		Biwords bs;
		while ((bs = bp.next()) != null) {
			for (Biword bw : bs.getBiwords()) {
				int count = queryCount(bw);
				System.out.println("count " + count + " / " + totalCount);
			}
		}
	}

	public int queryCount(Biword bw) {
		int a = getBestLabel(bw.getWords()[0].getPoints3d());
		int b = getBestLabel(bw.getWords()[1].getPoints3d());
		return tree[a][b].size();
	}

	private int getBestLabel(Point3d[] word) {
		int best = -1;
		double bestRmsd = Double.POSITIVE_INFINITY;
		for (int i = 0; i < wordRepresentants.length; i++) {
			Point3d[] r = wordRepresentants[i];
			double rmsd = rmsd(word, r);
			if (rmsd < bestRmsd) {
				bestRmsd = rmsd;
				best = i;
			}
		}
		return best;
	}

	private List<Integer> getLabels(Point3d[] word) {
		List<Integer> labels = new ArrayList<>();
		for (int i = 0; i < wordRepresentants.length; i++) {
			Point3d[] r = wordRepresentants[i];
			if (rmsd(word, r) < params.getWordLabelThreshold()) {
				labels.add(i);
			}
		}
		return labels;
	}

	private double rmsd(Point3d[] x, Point3d[] y) {
		tr.set(x, y);
		double rmsd = tr.getRmsd();
		return rmsd;
	}

	public void build() {
		BiwordsProvider bp = new BiwordsProvider();
		Biwords bs;
		while ((bs = bp.next()) != null) {
			for (Biword bw : bs.getBiwords()) {
				add(bw);
			}
		}
	}

	public void run() {
		build();
		measure();
	}

	public static void main(String[] args) throws IOException {
		BiwordHashClustering m = new BiwordHashClustering();
		m.run();
	}

}
