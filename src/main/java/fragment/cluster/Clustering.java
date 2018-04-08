package fragment.cluster;

import fragment.Fragments;
import language.search.Best;

/**
 *
 * @author Antonin Pavelka
 */
public class Clustering {

	private Fragments fragments;

	public Clustering(Fragments fragments) {
		this.fragments = fragments;
	}

	/*private double max(FragmentPoints f) {
		double max = 0;
		Point3d[] points = f.getPoints();
		for (Point3d x : points) {
			for (Point3d y : points) {
				if (x.distance(y) > max) {
					max = x.distance(y);
				}
			}
		}
		return max;
	}

	public boolean validate(FragmentPoints center, FragmentPoints fragment) {
		boolean ok = true;
		Superposer superposer = new Superposer();
		superposer.set(center.getPoints(), fragment.getPoints());
		double rrr = superposer.getRmsd();

		if (rrr > 10) {
			ok = false;
			System.out.println("############ " + rrr);
		}

		return ok;
	}*/
	public Clusters cluster(double threshold) {
		Clusters clusters = new Clusters();
		int counter = 0;
		for (Fragment fragment : fragments) {

			Cluster cluster = findOrCreateCluster(fragment, threshold, clusters);
			cluster.add(fragment);
			counter++;
			if (counter % 1000 == 0) {
			//	System.out.println("clustered " + counter + " / " + fragments.size());
			}
		}
		return clusters;
	}

	private Cluster findOrCreateCluster(Fragment fragment, double threshold, Clusters clusters) {
		Best<Cluster> best = findNearestWithinRange(fragment, threshold, clusters);
		Cluster cluster = best.getBestObject();
		double distance = best.getBestProperty();
		if (cluster == null) {
			cluster = new Cluster(fragment);
			clusters.add(cluster);
		} else {
			cluster.updateRadius(distance);
		}
		return cluster;
	}

	private Best<Cluster> findNearestWithinRange(Fragment query, double range, Clusters clusters) {
		Best<Cluster> nearest = Best.createSmallest();
		for (Cluster candidate : clusters) {
			Fragment center = candidate.getCentroid();
			double rmsd = query.getDistance(center);
			if (rmsd <= range) {
				nearest.update(candidate, rmsd);
			}
		}
		return nearest;
	}

}
