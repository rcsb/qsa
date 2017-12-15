package algorithm.hierarchical;

import algorithm.Search;
import algorithm.SearchAlgorithm;
import alignment.Alignments;
import alignment.Alignment;
import biword.index.Index;
import biword.index.Indexes;
import global.Parameters;
import global.io.Directories;
import java.util.ArrayList;
import java.util.List;
import pdb.StructureSource;
import pdb.Structures;
import pdb.cath.Cath;
import util.Time;

/**
 *
 * @author Antonin Pavelka
 */
public class HierarchicalSearch implements Search {

	private final Parameters parameters;
	private final Directories dirs;
	private final Indexes indexes;
	private final Cath cath;
	private final Hierarchy hierarchy;
	private final StructureSource query;

	public HierarchicalSearch(Parameters parameters, Directories dirs, Cath cath, Hierarchy hierarchy, StructureSource query) {
		this.parameters = parameters;
		this.dirs = dirs;
		this.indexes = new Indexes(parameters, dirs);
		this.cath = cath;
		this.hierarchy = hierarchy;
		this.query = query;
	}

	@Override
	public Alignments run() {
		dirs.createJob();
		Structures root = hierarchy.getRoot();
		Alignments representativeHits = flatSearch(query, root);
		List<StructureSource> selected = select(representativeHits);
		Alignments results = new Alignments(parameters, dirs);
		for (StructureSource representant : selected) {
			System.out.println("selected " + representant);
			Structures child = hierarchy.getChild(representant);
			Alignments domainHits = flatSearch(representant, child);
			results.merge(domainHits);
		}
		return results;
	}

	private List<StructureSource> select(Alignments alignmentSummaries) {
		List<StructureSource> list = new ArrayList<>();
		List<Alignment> summaries = alignmentSummaries.getBestSummariesSorted();
		for (Alignment summary : summaries) {
			if (summary.getTmScore() >= parameters.getTmThresholdForRepresentants()) {
				StructureSource source = summary.getStructureSourcePair().getSecond();
				list.add(source);
			}
		}
		return list;

	}

	private Alignments flatSearch(StructureSource query, Structures targets) {
		dirs.createTask("task_" + query);
		Time.start("init");
		Index index = indexes.getIndex(targets);
		System.out.println("Biword index created.");
		Time.stop("init");
		Structures queryStructures = new Structures(parameters, dirs, cath, "query");
		queryStructures.add(query);
		System.out.println("Query size: " + queryStructures.size() + " residues.");
		SearchAlgorithm baa = new SearchAlgorithm(parameters, dirs, queryStructures.getSingle(),
			targets, index, parameters.isVisualize());
		Time.start("query");
		Alignments alignmentSummaries = baa.search();
		Time.stop("query");
		Time.print();

		return alignmentSummaries;

	}

}
