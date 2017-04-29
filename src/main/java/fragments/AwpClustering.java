package fragments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AwpClustering implements Alignments {

	private final Map<Integer, AwpCluster> clusters = new HashMap<>();
	private final int minStrSize;

	public AwpClustering(int minStrSize) {
		this.minStrSize = minStrSize;
	}

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

	@Override
	public List<Alignment> getAlignments() {
		Collection<AwpCluster> all = clusters.values();
		int max = 0;
		//System.out.println("before " + all.size());
		for (AwpCluster c : all) {
			int r = c.sizeInResidues();
			if (max < r) {
				max = r;
			}
		}
		//System.out.println("after");
		List<Alignment> good = new ArrayList<>();
		for (AwpCluster c : all) {
			int n = c.sizeInResidues();
			if (n >= 15 && (n >= minStrSize / 5) && n >= (max / 5)) {
				good.add(c);
				//System.out.print(n + " ");
			}
		}
		//System.out.println("");
		return good;
	}

	public AwpCluster getCluster(int id) {
		return clusters.get(id);
	}

	public void dist() {
		for (AwpCluster c : clusters.values()) {
			System.out.println(c.sizeInWords());
		}
	}
}
