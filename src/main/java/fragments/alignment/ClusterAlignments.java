package fragments.alignment;

import fragments.Edge;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterAlignments implements Alignments {

	private final Map<Integer, ClusterAlignment> clusters = new HashMap<>();
	private final int minStrSize;

	public ClusterAlignments(int minStrSize) {
		this.minStrSize = minStrSize;
	}

	public void add(ClusterAlignment c) {
		clusters.put(c.getId(), c);
	}

	public void merge(Edge e) {
		int x = e.getX().getClusterId();
		int y = e.getY().getClusterId();
		ClusterAlignment cx = clusters.get(x);
		ClusterAlignment cy = clusters.get(y);
		cx.add(cy);
		cx.connectWords(e.getX(), e.getY());
		clusters.remove(y);
		cy.replaceBy(cx);
	}

	@Override
	public List<Alignment> getAlignments() {
		Collection<ClusterAlignment> all = clusters.values();
		int max = 0;
		//System.out.println("before " + all.size());
		for (ClusterAlignment c : all) {
			int r = c.sizeInResidues();
			if (max < r) {
				max = r;
			}
		}
		//System.out.println("after");
		List<Alignment> good = new ArrayList<>();
		for (ClusterAlignment c : all) {
			int n = c.sizeInResidues();
			if (n >= 15 && (n >= minStrSize / 5) && n >= (max / 5)) {
				good.add(c);
				//System.out.print(n + " ");
			}
		}
		//System.out.println("");
		return good;
	}

	public ClusterAlignment getCluster(int id) {
		return clusters.get(id);
	}

	public void dist() {
		for (ClusterAlignment c : clusters.values()) {
			System.out.println(c.sizeInWords());
		}
	}
}
