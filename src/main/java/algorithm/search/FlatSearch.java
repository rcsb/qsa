package algorithm.search;

import alignment.Alignments;
import fragment.index.Grid;
import fragment.index.Grids;
import global.Parameters;
import global.io.Directories;
import structure.SimpleStructure;
import structure.Structures;
import cath.Cath;
import util.Time;

/**
 *
 * @author Antonin Pavelka
 */
public class FlatSearch implements Search {

	private final Parameters parameters;
	private final Directories dirs;
	private final Structures targets;
	private final SimpleStructure query;

	public FlatSearch(Parameters parameters, Directories dirs, Cath cath, SimpleStructure query, Structures targets) {
		this.parameters = parameters;
		this.dirs = dirs;
		this.targets = targets;
		this.query = query;
	}

	@Override
	public Alignments run() {
		Grids indexes = new Grids(parameters, dirs);
		dirs.createTask("task_" + query.getSource());
		Time.start("init");
		Grid index = indexes.getGrid(targets);
		System.out.println("Biword index created.");
		Time.stop("init");
		SearchAlgorithm baa = new SearchAlgorithm(parameters, dirs, query, targets, index);
		Time.start("query");
		Alignments alignmentSummaries = baa.search();
		Time.stop("query");
		Time.print();
		return alignmentSummaries;
	}

}
