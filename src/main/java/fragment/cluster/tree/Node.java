package fragment.cluster.tree;

import fragment.ObjectSample;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonin Pavelka
 */
public class Node {

	private List<Node> children = new ArrayList<>();
	private ObjectSample fragments;

	public Node(ObjectSample fragments) {
		this.fragments = fragments;
	}

	public void removeFragments() {
		this.fragments = null;
	}

	public void addNode(Node node) {
		children.add(node);
	}

	public ObjectSample getFragments() {
		return fragments;
	}
}
