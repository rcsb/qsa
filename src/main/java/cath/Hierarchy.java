package cath;

import java.util.HashMap;
import java.util.Map;
import structure.StructureSource;
import structure.Structures;

/**
 *
 * @author Antonin Pavelka
 *
 * Two level tree of protein structures: top level are representatives of clusters, lower level the cluster contents.
 * The class hides and separates the CATH logic, for easier use and an option to exchange of CATH for a different
 * clusters.
 *
 */
public class Hierarchy {

	private final Structures root;
	private final Map<StructureSource, Structures> children = new HashMap<>();

	public Hierarchy(Structures root) {
		this.root = root;
	}

	public void addChild(StructureSource key, Structures child) {
		children.put(key, child);
	}

	public Structures getRoot() {
		return root;
	}

	public Structures getChild(StructureSource key) {
		return children.get(key);
	}

}
