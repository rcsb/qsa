package fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Graph of aligned word pairs, connected by edges iff the RMSD of aligned biwords (biword = any two non-overlapping
 * words from single protein) is low.
 *
 * Aligned pair = pair of similar elements from different proteins.
 *
 * @author antonin
 *
 */
public class AwpGraph {

	private final List<Edge> edges = new ArrayList<>();
	private final Map<AwpNode, List<Edge>> connections = new HashMap<>();
	private final Map<AwpNode, AwpNode> nodes = new HashMap<>();

	public Set<AwpNode> getNodes() {
		return nodes.keySet();
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public List<Edge> getConnections(AwpNode n) {
		return connections.get(n);
	}

	public void connect(AwpNode[] ps, double rmsd) {
		for (int i = 0; i < ps.length; i++) {
			AwpNode p = ps[i];
			if (nodes.containsKey(p)) { // guarantees no object is duplicated
				ps[i] = nodes.get(p);
			} else {
				nodes.put(p, p);
			}
		}
		Edge e = new Edge(ps[0], ps[1], rmsd);
		edges.add(e); // just one direction here, because fragments are created with both orderings of words, so this results in both directions eventually
		List<Edge> es = connections.get(ps[0]);
		if (es == null) {
			es = new ArrayList<>();
			connections.put(ps[0], es);
		}
		es.add(e);
	}
}
