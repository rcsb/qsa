package analysis;

import algorithm.search.FlatSearch;
import algorithm.search.Search;
import algorithm.search.hierarchical.HierarchicalSearch;
import algorithm.search.hierarchical.Hierarchy;
import algorithm.search.hierarchical.HierarchyFactory;
import alignment.Alignments;
import biword.index.Indexes;
import global.Parameters;
import global.io.Directories;
import java.io.File;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import output.OutputVisualization;
import output.OutputTable;
import pdb.StructureFilter;
import pdb.Structures;
import pdb.cath.Cath;

/**
 *
 * Main class. Allows to run searches and pairwise comparisons and batches of those.
 *
 * TODO move batch functionality above.
 *
 * @author Antonin Pavelka
 */
public class Job {

	private Parameters parameters;
	private Directories dirs;
	private Indexes indexes;
	private int pairNumber = 100000;
	private Cath cath;

	private enum Mode {
		HIERARCHICAL_SEARCH, FLAT_SEARCH, PAIRWISE_BATCH
	}
	private Mode mode = Mode.HIERARCHICAL_SEARCH;
	//private Mode mode = Mode.FRAGMENT_DB_SEARCH;
	//private Mode mode = Mode.FLAT_SEARCH;

	// TODO pairwise dataset from single file
	public void run() {
		Search search;
		long time1 = System.nanoTime();
		Structures query = createQueryStructure();

		if (mode == Mode.FLAT_SEARCH) {
			Structures target = createTargetStructures();
			dirs.createJob();
			dirs.createTask("flat");			
			search = new FlatSearch(parameters, dirs, cath, query, target);
		} else if (mode == Mode.HIERARCHICAL_SEARCH) {
			Hierarchy hierarchy = createHierarchy();
			search = new HierarchicalSearch(parameters, dirs, cath, query, hierarchy);
		} else {
			throw new RuntimeException();
		}

		Alignments alignments = search.run();

		OutputTable outputTable = new OutputTable(dirs.getTableFile());
		outputTable.generateTable(alignments);

		OutputVisualization outputVisualization = new OutputVisualization(dirs, alignments);
		outputVisualization.generate();

		long time2 = System.nanoTime();
		double s = ((double) (time2 - time1)) / 1000000000;
		System.out.println("Total time: " + s);
	}

	private Structures createQueryStructure() {
		Structures queryStructures = new Structures(parameters, dirs, cath, "query");
		queryStructures.addFromIds(dirs.getQueryCodes());
		return queryStructures;
	}

	private Structures createTargetStructures() {
		Structures targetStructures = new Structures(parameters, dirs, cath, "custom_search1");
		targetStructures.setFilter(new StructureFilter(parameters));
		if (dirs.getCustomTargets().exists()) {
			System.out.println("custom");
			targetStructures.addFromIds(dirs.getCustomTargets());
		} else {
			System.out.println("Flat search over topologies");
			targetStructures.addAll(cath.getTopologyRepresentants());
		}
		targetStructures.setMax(parameters.getMaxDbSize());
		targetStructures.shuffle();
		return targetStructures;
	}

	private Hierarchy createHierarchy() {
		HierarchyFactory hierarchyFactory = new HierarchyFactory(parameters, dirs);
		Hierarchy hierarchy = hierarchyFactory.createFromCath(cath);
		return hierarchy;
	}

	/*private void runPairwiseAlignment() {
		try {
			dirs.createJob();
			//PairsSource pairs = new PairsSource(dirs, PairsSource.Source.MALISAM);
			//PairsSource pairs = new PairsSource(dirs, PairsSource.Source.TOPOLOGY89);
			PairsSource pairs = new PairsSource(dirs, PairsSource.Source.MALISAM);
			for (StructurePair pair : pairs) {
				dirs.createTask(pair.a + "_" + pair.b);
				Time.start("init"); // 5cgo, 1w5h
				Structure
	s target = new Structures(parameters, dirs, cath, "target");
				target.add(pair.a);
				target.setMax(1);
				target.shuffle();
				Index index = indexes.getIndex(target);
				System.out.println("Biword index created.");
				Structures query = new Structures(parameters, dirs, cath, "query");
				query.add(pair.b);
				SearchAlgorithm baa = new SearchAlgorithm(parameters, dirs, query.get(0, 0), target, index);
				Time.stop("init");
				baa.search();
			}
			CsvMerger csv = new CsvMerger(dirs);
			csv.print();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void runSearch() {
		dirs.createJob();
		Structures targetStructures = new Structures(parameters, dirs, cath, "cath_topology");
		targetStructures.setFilter(new StructureFilter(parameters));
		if (false) {
			targetStructures.addFromIds(dirs.getPdbEntryTypes());
		} else if (dirs.getCustomTargets().exists()) {
			targetStructures.addFromIds(dirs.getCustomTargets());
		} else {
			Cath cath = new Cath(dirs);
			targetStructures.addAll(cath.getTopologyRepresentants());
		}
		targetStructures.setMax(parameters.getMaxDbSize());
		targetStructures.shuffle();
		Time.start("init");
		Index index = indexes.getIndex(targetStructures);
		System.out.println("Biword index created.");
		Time.stop("init");
		Cath cath = new Cath(dirs);
		Structures queryStructures = new Structures(parameters, dirs, cath, "query");
		queryStructures.addAll(cath.getTopologyRepresentants());
		for (SimpleStructure queryStructure : queryStructures) {
			try {
				dirs.createTask("task");
				System.out.println("Query size: " + queryStructures.size() + " residues.");
				SearchAlgorithm baa = new SearchAlgorithm(parameters, dirs, queryStructure, targetStructures,
					index, parameters.isVisualize());
				Time.start("query");
				baa.search();
				Time.stop("query");
				Time.print();
			} catch (Exception ex) {
				FlexibleLogger.error(ex);
			}
		}
	}

	private void runHierarchicalSearch() {
		Cath cath = new Cath(dirs);
		HierarchyFactory hierarchyFactory = new HierarchyFactory(parameters, dirs);
		Hierarchy hierarchy = hierarchyFactory.createFromCath(cath);
		HierarchicalSearch hierarchicalSearch = new HierarchicalSearch(parameters, dirs, cath);

		StructureSource query = new StructureSource("1ajv");

		hierarchicalSearch.run(query, hierarchy);
	}
	 */
	private void init(String[] args) {
		Options options = new Options();
		options.addOption(Option.builder("h")
			.desc("path to home directory, where all the data will be stored")
			.hasArg()
			.build());
		options.addOption(Option.builder("s")
			.desc("name of directory with structure files, located in home directory")
			.hasArg()
			.build());
		options.addOption(Option.builder("m")
			.desc("mode - what task to run")
			.hasArg()
			.build());
		options.addOption(Option.builder("n")
			.desc("max number of pairs")
			.hasArg()
			.build());

		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cl = parser.parse(options, args);
			if (cl.hasOption("h")) {
				File home = new File(cl.getOptionValue("h").trim());
				dirs = new Directories(home);
			} else {
				throw new ParseException("No -h parameter, please specify the home directory.");
			}
			if (cl.hasOption("s")) {
				String structures = cl.getOptionValue("s").trim();
				dirs.setStructures(structures);
			}
			// TODO move to parameters?
			if (cl.hasOption("m")) {
				String sm = cl.getOptionValue("m").trim();
				switch (sm) {
					case "flat_search":
						mode = Mode.FLAT_SEARCH;
						break;
					case "hierarchical_search":
						mode = Mode.FLAT_SEARCH;
						break;
					case "pairwise":
						mode = Mode.PAIRWISE_BATCH;
						break;
				}
			}
			if (cl.hasOption("n")) {
				String s = cl.getOptionValue("n").trim();
				pairNumber = Integer.parseInt(s);
			}
			parameters = Parameters.create(dirs.getParameters());
			cath = new Cath(dirs);
			indexes = new Indexes(parameters, dirs);
		} catch (ParseException exp) {
			System.err.println("Parsing arguments has failed: " + exp.getMessage());
		}
	}

	public static void main(String[] args) {
		Job m = new Job();
		m.init(args);
		m.run();
	}

}
