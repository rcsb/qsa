package fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Antonin Pavelka
 */
public class GraphPrecursor {

	private final Map<AwpNode, AwpNode> nodes = new HashMap<>();
	private final ArrayList<Edge> edges = new ArrayList<>();
	private int qwn, twn;
	private AwpNode[][] tree;
	private int n = 0;

	public GraphPrecursor(int qwn, int twn) {
		this.qwn = qwn;
		this.twn = twn;
		tree = new AwpNode[qwn][];
	}

	public AwpNode addNode(AwpNode node) {
		Word[] ws = node.getWords();
		int qw = ws[0].getId();
		int tw = ws[1].getId();
		if (tree[qw] == null) {
			tree[qw] = new AwpNode[twn];
			tree[qw][tw] = node;
			n++;
			return node;
		} else {
			if (tree[qw][tw] == null) {
				tree[qw][tw] = node;
				n++;
				return node;
			} else {
				return tree[qw][tw];
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
		for (int x = 0; x < tree.length; x++) {
			if (tree[x] == null) {
				continue;
			}
			for (int y = 0; y < tree[x].length; y++) {
				if (tree[x][y] != null) {
					a[i] = tree[x][y];
					i++;
				}
			}
		}
		return a;
	}
}
