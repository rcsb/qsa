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
import structure.SimpleStructure;
import structure.StructureFactory;
import structure.StructureSource;
import structure.Structures;

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
	private final SimpleStructure query;

	public HierarchicalSearch(Parameters parameters, Directories dirs, Cath cath,
		SimpleStructure query, Hierarchy hierarchy) {
		this.parameters = parameters;
		this.dirs = dirs;
		this.cath = cath;
		this.hierarchy = hierarchy;
		this.query = query;
	}

	@Override
	public Alignments run() {
		dirs.createJob();
		Structures root = hierarchy.getRoot();
		Alignments representativeHits = search(query, root).run();
		List<StructureSource> representativesFiltered
			= filterRepresentativeHits(representativeHits, parameters.getTmThresholdForRepresentants());
		System.out.println(representativesFiltered + " cluster representatives found.");
		generateOutput(representativeHits, parameters.getTmFilter());
		Alignments results = new Alignments();
		for (StructureSource representative : representativesFiltered) {
			Structures child = hierarchy.getChild(representative);
			Alignments hits = search(query, child).run();
			generateOutput(hits, parameters.getTmFilter());
			results.merge(hits);
		}
		return results;
	}

	private void generateOutput(Alignments alignments, double tmScore) {
		OutputTable outputTable = new OutputTable(dirs.getTableFile());
		outputTable.generateTable(alignments, tmScore);
		if (parameters.isVisualize()) {
			StructureFactory structureFactory = new StructureFactory(dirs, cath);
			OutputVisualization outputVisualization
				= new OutputVisualization(parameters, dirs, alignments, structureFactory);
			outputVisualization.generate();
		}
	}

	private List<StructureSource> filterRepresentativeHits(Alignments alignmentSummaries, double tmScore) {
		List<StructureSource> list = new ArrayList<>();
		List<Alignment> alignments = alignmentSummaries.getBestSummariesSorted(tmScore);
		Alignment best = null;
		for (Alignment alignment : alignments) {
			if (alignment.getTmScore() >= parameters.getTmThresholdForRepresentants()) {
				StructureSource source = alignment.getStructureSourcePair().getSecond();
				list.add(source);
			}
			if (best == null || best.getTmScore() < alignment.getTmScore()) {
				best = alignment;
			}
		}
		if (list.isEmpty() && best != null) {
			list.add(best.getStructureSourcePair().getSecond());
		}
		return list;
	}

	private Search search(SimpleStructure query, Structures targets) {
		return new FlatSearch(parameters, dirs, cath, query, targets);
	}

}
