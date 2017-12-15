package algorithm.hierarchical;

import java.util.HashMap;
import java.util.Map;
import pdb.StructureSource;
import pdb.Structures;

/**
 *
 * @author Antonin Pavelka
 *
 * Two level tree of protein structures: top level are representatives of clusters, lower level the cluster contents.
 *
 */
public class Hierarchy {

	private final Structures root;
	private final Map<StructureSource, Structures> children = new HashMap<>();

	Hierarchy(Structures root) {
		this.root = root;
	}

	void addChild(StructureSource key, Structures child) {
		children.put(key, child);
	}

	public Structures getRoot() {
		return root;
	}

	public Structures getChild(StructureSource key) {
		return children.get(key);
	}

}
