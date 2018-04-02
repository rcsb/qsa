package fragment.cluster.search;

import fragment.Fragments;
import fragment.cluster.Cluster;
import fragment.cluster.Clustering;
import fragment.cluster.Clusters;
import fragment.cluster.Fragment;
import geometry.superposition.Superposer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import statistics.Distribution;

/**
 *
 * @author Antonin Pavelka
 */
public class ClusterSearch {
	
	private Fragments fragments;
	private Random random = new Random(1);

	public ClusterSearch(Fragments fragments) {
		this.fragments = fragments;
		
	}

	private void testTriangleInequality() {
		Distribution distribution = new Distribution();
		for (int i = 0; i < 100000; i++) {
			Fragment[] triangle = getTriangle();
			Fragment a = triangle[0];
			Fragment b = triangle[1];
			Fragment c = triangle[2];
			double ab = a.rmsd(b);
			double ac = a.rmsd(c);
			double bc = b.rmsd(c);

			distribution.add(ab + bc - ac);
			distribution.add(ac + bc - ab);
			distribution.add(ab + ac - bc);
		}
		System.out.println("stats");
		distribution.printHistogram(10);
		System.out.println("---");
	}

	private Fragment[] getTriangle() {
		Fragment a = getRandomFragment();
		Fragment b = getRandomFragment();
		while (a == b) {
			b = getRandomFragment();
		}
		Fragment c = getRandomFragment();
		while (a == c || b == c) {
			c = getRandomFragment();
		}
		Fragment[] triangle = {a, b, c};
		return triangle;
	}

	private Fragment getRandomFragment() {
		return fragments.get(random.nextInt(fragments.size()));
	}

	public List<Cluster> search(Fragment query, double queryRange) {
		/*List<Cluster> list = new ArrayList<>();
		for (Cluster cluster : clusters) {
			double threshold = cluster.getRadius() + queryRange;
			Superposer superposer = new Superposer(query.getPoints(), cluster.getCentroid().getPoints());
			double rmsd = superposer.getRmsd();
			if (rmsd <= threshold) {
				list.add(cluster);
			}
		}
		return list;*/
		return null;
	}
	
	public void buildTree() {
		Clustering rootClustering = new Clustering(fragments);
		Clusters root = rootClustering.cluster(4);
		for (Cluster cluster : root) {
			Clustering clustering = new Clustering(new Fragments(cluster.getContent()));
			clustering.cluster(2);
		}
		
	}

}
