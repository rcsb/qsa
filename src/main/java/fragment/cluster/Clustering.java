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
		for (FragmentPoints fragment : fragments) {
			Cluster cluster = findOrCreateCluster(fragment, threshold, clusters);
			cluster.add(fragment);
		}
		return clusters;
	}

	private Cluster findOrCreateCluster(FragmentPoints fragment, double threshold, Clusters clusters) {
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

	private Best<Cluster> findNearestWithinRange(FragmentPoints query, double range, Clusters clusters) {
		Best<Cluster> nearest = Best.createSmallest();
		for (Cluster candidate : clusters) {
			FragmentPoints center = candidate.getCentroid();
			double rmsd = query.rmsd(center);
			if (rmsd <= range) {
				nearest.update(candidate, rmsd);
			}
		}
		return nearest;
	}

}
