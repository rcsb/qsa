package fragments;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AwpClustering {
	Map<Integer, AwpCluster> clusters = new HashMap<>();

	public void add(AwpCluster c) {
		clusters.put(c.getId(), c);
	}

	public void merge(int x, int y) {
		AwpCluster cx = clusters.get(x);
		AwpCluster cy = clusters.get(y);
		cx.add(cy);
		clusters.remove(y);
		cy.replaceBy(cx);
	}

	public int size() {
		return clusters.size();
	}

	public Collection<AwpCluster> getClusters() {
		return clusters.values();
	}

	public void dist() {
		for (AwpCluster c : clusters.values()) {
			System.out.println(c.size());
		}
	}
}
