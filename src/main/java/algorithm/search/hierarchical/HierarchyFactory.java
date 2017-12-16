package algorithm.search.hierarchical;

import global.Parameters;
import global.io.Directories;
import java.util.List;
import pdb.StructureSource;
import pdb.Structures;
import pdb.cath.Cath;

/**
 *
 * @author Antonin Pavelka
 */
public class HierarchyFactory {

	private final Directories dirs;
	private final Parameters parameters;

	public HierarchyFactory(Parameters parameters, Directories dirs) {
		this.parameters = parameters;
		this.dirs = dirs;
	}

	public Hierarchy createFromCath(Cath cath) {
		Structures root = new Structures(parameters, dirs, cath, "cath_topology");
		root.addAll(cath.getTopologyRepresentants());
		Hierarchy hierarchy = new Hierarchy(root);
		for (String id : cath.getTopologyRepresentants()) {
			//System.out.println("  "+ id);
			Structures structures = new Structures(parameters, dirs, cath, id);
			StructureSource source = new StructureSource(id);
			List<String> topologyContent = cath.getTopologyContent(source.getCathDomainId());
			//for (String s : topologyContent) {
			//	System.out.println(s);
			//}
			structures.addAll(topologyContent);
			hierarchy.addChild(source, structures);
		}
		return hierarchy;
	}

}
