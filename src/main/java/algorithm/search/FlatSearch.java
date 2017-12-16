package algorithm.search;

import alignment.Alignments;
import biword.index.Index;
import biword.index.Indexes;
import global.Parameters;
import global.io.Directories;
import pdb.SimpleStructure;
import pdb.Structures;
import pdb.cath.Cath;
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
	private final Cath cath;

	public FlatSearch(Parameters parameters, Directories dirs, Cath cath, Structures query, Structures targets) {
		this.parameters = parameters;
		this.dirs = dirs;
		this.targets = targets;
		this.query = query.getSingle();
		this.cath = cath;
	}

	@Override
	public Alignments run() {
		Indexes indexes = new Indexes(parameters, dirs);
		dirs.createTask("task_" + query);
		Time.start("init");
		Index index = indexes.getIndex(targets);
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
