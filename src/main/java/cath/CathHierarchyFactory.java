package cath;

import global.Parameters;
import global.io.Directories;
import java.util.List;
import pdb.StructureSource;
import pdb.Structures;

/**
 *
 * @author Antonin Pavelka
 */
public class CathHierarchyFactory {

	private final Directories dirs;
	private final Parameters parameters;
	private final Level level;
	private final Cath cath;

	public CathHierarchyFactory(Parameters parameters, Directories dirs, Cath cath) {
		this.parameters = parameters;
		this.dirs = dirs;
		this.level = cath.getHomologousSuperfamilies();
		this.cath = cath;
	}

	public Hierarchy createFromCath() {
		Structures root = createRootStructures();
		System.out.println("Root size: " + root.size());
		Hierarchy hierarchy = new Hierarchy(root);
		for (Classification classification : level.getGroupClasses()) {
			Group group = level.getGroup(classification);
			Structures children = createChildren(group);
			System.out.println("... of size " + children.size());
			StructureSource parrent = new StructureSource(group.getRepresentant().getId());
			hierarchy.addChild(parrent, children);
		}
		return hierarchy;
	}

	private Structures createRootStructures() {
		List<StructureSource> rootSources = level.getRepresentantSources();
		Structures root = new Structures(parameters, dirs, cath, "hierarchy_root");
		root.addAll(rootSources);
		System.out.println(root.getFailed() + " root CATH domains failed to parse, successfull " + root.size());
		return root;
	}

	private Structures createChildren(Group group) {
		Structures structures = new Structures(parameters, dirs, cath, group.getClassification() + "_child");
		structures.addAll(group.getMemberSources());
		System.out.println(structures.getFailed() + " CATH " + group.getClassification() + " domains failed to parse, "
			+ "successfull " + structures.size()
		);
		return structures;
	}

}
