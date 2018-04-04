package fragment.cluster.tree;

import fragment.Fragments;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonin Pavelka
 */
public class Node {

	private List<Node> children = new ArrayList<>();
	private Fragments fragments;

	public Node(Fragments fragments) {
		this.fragments = fragments;
	}

	public void removeFragments() {
		this.fragments = null;
	}

	public void addNode(Node node) {
		children.add(node);
	}

	public Fragments getFragments() {
		return fragments;
	}
}
