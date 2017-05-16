package fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import pdb.SimpleStructure;

/**
 *
 * @author Antonin Pavelka
 */
public class GraphPrecursor {

	public final SimpleStructure structure;
	public final Map<AwpNode, AwpNode> nodes = new HashMap<>();
	public final ArrayList<Edge> edges = new ArrayList<>(100000);

	public static long nodeCounter;
	public static long edgeCounter;

	public GraphPrecursor(SimpleStructure structure) {
		this.structure = structure;
	}

	public AwpNode addNode(AwpNode n) {
		AwpNode existing = nodes.get(n);
		if (existing == null) {
			nodes.put(n, n);
			nodeCounter++;
			return n;
		} else {
			return existing; // use existing object
		}
	}

	public void addEdge(Edge e) {
		edgeCounter++;
		edges.add(e);
	}
}
