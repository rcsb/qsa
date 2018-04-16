package fragment.cluster.tree;

import fragment.ObjectSample;
import fragment.cluster.Cluster;
import fragment.cluster.Clustering;
import fragment.cluster.Clusters;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonin Pavelka
 */
public class TreeFactory {
/*
	private Fragments fragments;
	private double[] thresholds = {5, 4, 3, 2, 1, 0.5, 0.1};

	public TreeFactory(Fragments fragments) {
		this.fragments = fragments;
	}

	public Tree build() {
		Node root = new Node(fragments);
		Tree tree = new Tree(root);
		List<Node> level = new ArrayList<>();
		level.add(root);
		for (int i = 0; i < thresholds.length; i++) {
			level = expand(level, thresholds[i]);
		}
		return tree;
	}

	private List<Node> expand(List<Node> parrents, double threshold) {
		List<Node> children = new ArrayList<>();
		for (Node parrent : parrents) {
			Clustering clustering = new Clustering(parrent.getFragments());
			Clusters clusters = clustering.cluster(threshold);
			for (Cluster cluster : clusters) {
				Node child = new Node(cluster.getContent());
				parrent.addNode(child);
				children.add(child);
			}
			parrent.removeFragments();
		}
		return children;
	}
*/
}
