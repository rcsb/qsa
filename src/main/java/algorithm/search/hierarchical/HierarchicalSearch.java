package algorithm.search.hierarchical;

import cath.Hierarchy;
import cath.Cath;
import algorithm.search.FlatSearch;
import algorithm.search.Search;
import alignment.Alignments;
import alignment.Alignment;
import global.Parameters;
import global.io.Directories;
import java.util.ArrayList;
import java.util.List;
import output.OutputTable;
import output.OutputVisualization;
import pdb.SimpleStructure;
import pdb.StructureFactory;
import pdb.StructureSource;
import pdb.Structures;

/**
 *
 * @author Antonin Pavelka
 *
 * Finds similar cluster representatives first, then scans cluster contents.
 *
 */
public class HierarchicalSearch implements Search {

	private final Parameters parameters;
	private final Directories dirs;
	private final Cath cath;
	private final Hierarchy hierarchy;
	private final Structures query;

	public HierarchicalSearch(Parameters parameters, Directories dirs, Cath cath,
		Structures query, Hierarchy hierarchy) {
		this.parameters = parameters;
		this.dirs = dirs;
		this.cath = cath;
		this.hierarchy = hierarchy;
		this.query = query;

		//for (String id : cath.getTopologyContent("4ea9A01")) {
		//	System.out.println("GGGGGGGG " + id);
		//}
	}

	@Override
	public Alignments run() {
		dirs.createJob();
		Structures root = hierarchy.getRoot();
		Alignments representativeHits = search(query, root).run();
		List<StructureSource> selected = filterRepresentativeHits(representativeHits);

		System.out.println(representativeHits.getBestSummariesSorted().size() + " topologies found.");
		generateOutput(representativeHits);
		System.out.println(selected.size() + " topologies selected.");

		Alignments results = new Alignments(parameters, dirs);
		results.setTmFilter(parameters.getTmFilter());
		for (StructureSource representative : selected) {
			Structures child = hierarchy.getChild(representative);
			System.out.println("aaaaa " + representative);
			for (SimpleStructure ss : child) {
				if (ss.getSource().toString().toUpperCase().contains("1CV2")) {
					System.out.println("ccccccccc " + ss.getSource());
				}
			}
			Alignments hits = search(query, child).run();
			generateOutput(hits);
			results.merge(hits);
		}
		return results;
	}

	private void generateOutput(Alignments alignments) {
		OutputTable outputTable = new OutputTable(dirs.getTableFile());
		outputTable.generateTable(alignments);
		if (parameters.isVisualize()) {
			StructureFactory structureFactory = new StructureFactory(dirs, cath);
			OutputVisualization outputVisualization = new OutputVisualization(dirs, alignments, structureFactory);
			outputVisualization.generate();
		}
	}

	private List<StructureSource> filterRepresentativeHits(Alignments alignmentSummaries) {
		List<StructureSource> list = new ArrayList<>();		
		List<Alignment> alignments = alignmentSummaries.getBestSummariesSorted();		
		for (Alignment alignment : alignments) {
			if (alignment.getTmScore() >= parameters.getTmThresholdForRepresentants()) {
				StructureSource source = alignment.getStructureSourcePair().getSecond();
				list.add(source);
			}
		}
		return list;
	}

	private Search search(Structures query, Structures targets) {
		return new FlatSearch(parameters, dirs, cath, query, targets);
	}

}
