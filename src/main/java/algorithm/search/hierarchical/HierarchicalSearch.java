package algorithm.search.hierarchical;

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
import pdb.StructureSource;
import pdb.Structures;
import pdb.cath.Cath;

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
	}

	@Override
	public Alignments run() {
		dirs.createJob();
		Structures root = hierarchy.getRoot();
		Alignments representativeHits = search(query, root).run();
		List<StructureSource> selected = filterRepresentativeHits(representativeHits);
		Alignments results = new Alignments(parameters, dirs);
		
		
		OutputTable outputTable = new OutputTable(dirs.getTableFile());
		outputTable.generateTable(results);
		OutputVisualization outputVisualization = new OutputVisualization(dirs, results);
		outputVisualization.generate();

		
		for (StructureSource representative : selected) {
			Structures child = hierarchy.getChild(representative);
			Alignments hits = search(query, child).run();
			results.merge(hits);
		}
		return results;
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
