package fragments;

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
		// System.out.println(x + " ->" + cx);
		// System.out.println(y + " -> " + cy);
		// System.out.println("****");
		cx.add(cy);
		// System.out.println(clusters.size());
		clusters.remove(y);
		cy.replaceBy(cx);
		// System.out.println(clusters.size());
	}

	public int size() {
		return clusters.size();
	}

	public void dist() {
		for (AwpCluster c : clusters.values()) {
			System.out.println(c.size());
		}
	}
}
