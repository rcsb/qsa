package fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Antonin Pavelka
 */
public class GraphPrecursor {

	public final Map<AwpNode, AwpNode> nodes = new HashMap<>();
	public final ArrayList<Edge> edges = new ArrayList<>();
	private int structureId;
	public static long nodeCounter;
	public static long edgeCounter;

	public GraphPrecursor(int structureId) {
		this.structureId = structureId;
	}

	public int getStructureId() {
		return structureId;
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
