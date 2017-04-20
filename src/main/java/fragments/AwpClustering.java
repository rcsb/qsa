package fragments;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AwpClustering {

	Map<Integer, AwpCluster> clusters = new HashMap<>();

	public void add(AwpCluster c) {
		clusters.put(c.getId(), c);
	}

	public void merge(Edge e) {
		int x = e.getX().getClusterId();
		int y = e.getY().getClusterId();
		AwpCluster cx = clusters.get(x);
		AwpCluster cy = clusters.get(y);
		cx.add(cy);
		cx.connectWords(e.getX(), e.getY());
		clusters.remove(y);
		cy.replaceBy(cx);
	}

	public int size() {
		return clusters.size();
	}

	public Collection<AwpCluster> getClusters() {
		return clusters.values();
	}

	public AwpCluster getCluster(int id) {
		return clusters.get(id);
	}

	public void dist() {
		for (AwpCluster c : clusters.values()) {
			System.out.println(c.size());
		}
	}
}
