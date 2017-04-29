package fragments;

import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class AlignmentByExpansion implements Alignment {

	private final Set<AwpNode> nodes = new HashSet<>();
	private final AwpGraph graph;

	private final PriorityQueue<Edge> queue = new PriorityQueue<>();

	public AlignmentByExpansion(AwpNode origin, AwpGraph graph) {
		this.graph = graph;
		add(origin);
		expand();
	}

	private final void add(AwpNode n) {
		nodes.add(n);
		List<Edge> edges = graph.getConnections(n);
		if (edges != null) {
			queue.addAll(edges);
		} else { // does it happen for some good reason or is it a bug?
		}
	}

	private final void expand() {
		while (!queue.isEmpty()) {
			Edge e = queue.poll();
			AwpNode x = e.getX();
			AwpNode y = e.getY();
			if (nodes.contains(y)) {
				continue;
			}
			assert nodes.contains(x);
			// let's add y
			add(y);
		}
	}

	@Override
	public Set<AwpNode> getNodes() {
		return nodes;
	}
}
