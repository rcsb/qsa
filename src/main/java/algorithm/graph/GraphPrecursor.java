package algorithm.graph;

import java.util.ArrayList;
import java.util.List;
import fragment.Fragment;

/**
 *
 * @author Antonin Pavelka
 */
public class GraphPrecursor {

	private final ArrayList<Edge> edges = new ArrayList<>();
	private final int twn;
	private final AwpNode[][] graph;
	private int n = 0;

	public GraphPrecursor(int qwn, int twn) {
		this.twn = twn;
		graph = new AwpNode[qwn][];
	}

	public AwpNode addNode(AwpNode node) {
		Fragment[] ws = node.getWords();
		int qw = ws[0].getId();
		int tw = ws[1].getId();
		if (graph[qw] == null) {
			graph[qw] = new AwpNode[twn];
			graph[qw][tw] = node;
			n++;
			return node;
		} else {
			if (graph[qw][tw] == null) {
				graph[qw][tw] = node;
				n++;
				return node;
			} else {
				return graph[qw][tw];
			}
		}
	}

	public void addEdge(Edge e) {
		edges.add(e);
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public AwpNode[] getNodes() {
		AwpNode[] a = new AwpNode[n];
		int i = 0;
		for (int x = 0; x < graph.length; x++) {
			if (graph[x] == null) {
				continue;
			}
			for (int y = 0; y < graph[x].length; y++) {
				if (graph[x][y] != null) {
					a[i] = graph[x][y];
					i++;
				}
			}
		}
		return a;
	}
}
