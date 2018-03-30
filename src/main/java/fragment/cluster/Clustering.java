package fragment.cluster;

import fragment.Fragments;

/**
 *
 * @author Antonin Pavelka
 */
public class Clustering {

	private Fragments fragments;

	public Clustering(Fragments fragments) {
		this.fragments = fragments;
	}

	public Clusters cluster(double threshold) {
		Clusters clusters = new Clusters();
		int counter = 0;
		for (FragmentPoints fragment : fragments) {
			Cluster bestCluster = pickCluster(fragment, threshold, clusters);
			bestCluster.add(fragment);
			System.out.println("clusters " + clusters.size() + " " + counter + " / " + fragments.size());
			counter++;

			if (clusters.size() > 50) { // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				return clusters;
			}

		}
		return clusters;
	}

	private Cluster pickCluster(FragmentPoints fragment, double threshold, Clusters clusters) {
		Cluster bestCluster = null;
		double bestDistance = Double.MAX_VALUE;
		for (Cluster candidate : clusters) {
			FragmentPoints center = candidate.getCentroid();
			double rmsd = fragments.rmsd(center, fragment);
			if (rmsd <= threshold && rmsd < bestDistance) {
				bestCluster = candidate;
				bestDistance = rmsd;
			}
		}
		if (bestCluster == null) { // creating new
			bestCluster = new Cluster(fragment);
			clusters.add(bestCluster);
		} else { // found
			bestCluster.updateRadius(bestDistance);
		}
		double rmsd = fragments.rmsd(fragment, bestCluster.getCentroid());
		/*if (rmsd > 10) {
			assert false;
		}
		System.out.println(rmsd);*/
		return bestCluster;
	}

}
