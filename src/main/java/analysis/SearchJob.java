package analysis;

import algorithm.search.FlatSearch;
import algorithm.search.Search;
import algorithm.search.hierarchical.HierarchicalSearch;
import cath.Hierarchy;
import cath.CathHierarchyFactory;
import alignment.Alignments;
import global.Parameters;
import global.io.Directories;
import java.io.File;
import output.OutputVisualization;
import output.OutputTable;
import structure.StructureSizeFilter;
import structure.Structures;
import cath.Cath;
import structure.SimpleStructure;
import structure.StructureFactory;
import structure.StructuresId;
import util.Time;

/**
 *
 * Runs a search, a query structure against a database, and produces text file outputs and PyMOL visualization.
 * Implements the highest levels of search logic and result presentation. Can run multiple searches.
 *
 * @author Antonin Pavelka
 */
public class SearchJob {

	private Parameters parameters;
	private Directories dirs;
	private Cath cath;

	public SearchJob(File homeDir) {
		this.dirs = new Directories(homeDir);
		this.parameters = Parameters.create(dirs.getParameters());
		this.cath = new Cath(dirs);
	}

	public void run() {
		String totalTimerName = "total";
		Time.start(totalTimerName);

		runSearches();

		Time.stop(totalTimerName);
		System.out.println("Total time: " + Time.get(totalTimerName).getMiliseconds());
	}

	private void runSearches() {
		Structures queries = createQueryStructures();
		for (SimpleStructure query : queries) {
			runSearch(query);
		}
	}

	private void runSearch(SimpleStructure query) {
		Search search;
		if (parameters.isHierarchicalSearch()) {
			search = createHierarchicalSearch(query);
		} else {
			search = createFlatSearch(query);
		}
		Alignments alignments = search.run();
		generateOutput(alignments);
	}

	private Search createFlatSearch(SimpleStructure query) {
		Structures target = createTargetStructures();
		dirs.createJob();
		dirs.createTask("flat");
		Search search = new FlatSearch(parameters, dirs, cath, query, target);
		if (target.getFailed() > 0) {
			System.out.println(target.getFailed() + " target structures failed to parse, successfull " + target.size());
		}
		return search;
	}

	private Search createHierarchicalSearch(SimpleStructure query) {
		Hierarchy hierarchy = createHierarchy();
		Search search = new HierarchicalSearch(parameters, dirs, cath, query, hierarchy);
		return search;
	}

	private void generateOutput(Alignments alignments) {
		OutputTable outputTable = new OutputTable(dirs.getTableFile());
		outputTable.generateTable(alignments, parameters.getTmFilter());
		if (parameters.isVisualize()) {
			visualize(alignments);
		}
	}

	private void visualize(Alignments alignments) {
		StructureFactory structureFactory = new StructureFactory(dirs, cath);
		OutputVisualization outputVisualization
			= new OutputVisualization(parameters, dirs, alignments, structureFactory);
		outputVisualization.generate();
	}

	private Structures createQueryStructures() {
		Structures queryStructures = new Structures(parameters, dirs, cath, new StructuresId("query"));
		queryStructures.addFromIds(dirs.getQueryCodes());
		return queryStructures;
	}

	private Structures createTargetStructures() {
		Structures targetStructures = new Structures(parameters, dirs, cath, new StructuresId("custom_search1"));
		targetStructures.setFilter(new StructureSizeFilter(parameters.getMinResidues(), parameters.getMaxResidues()));
		if (dirs.getCustomTargets().exists()) {
			System.out.println("Searching in user specified database " + dirs.getCustomTargets());
			targetStructures.addFromIds(dirs.getCustomTargets());
		} else {
			targetStructures.addAll(cath.getHomologousSuperfamilies().getRepresentantSources());
			System.out.println("Searching in " + targetStructures.size()
				+ " CATH homologous superfamilies representatives");
		}
		targetStructures.setMaxSize(parameters.getMaxDbSize());
		System.out.println("Database size limited to " + parameters.getMaxDbSize());
		targetStructures.shuffle();
		return targetStructures;
	}

	private Hierarchy createHierarchy() {
		CathHierarchyFactory hierarchyFactory = new CathHierarchyFactory(parameters, dirs, cath);
		Hierarchy hierarchy = hierarchyFactory.createFromCath();
		return hierarchy;
	}

}
