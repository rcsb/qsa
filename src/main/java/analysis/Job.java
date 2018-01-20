package analysis;

import algorithm.search.FlatSearch;
import algorithm.search.Search;
import algorithm.search.hierarchical.HierarchicalSearch;
import cath.Hierarchy;
import cath.CathHierarchyFactory;
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
import cath.Cath;

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

	public void run() {
		Search search;
		long time1 = System.nanoTime();
		Structures query = createQueryStructure();
		if (mode == Mode.FLAT_SEARCH) {
			Structures target = createTargetStructures();
			dirs.createJob();
			dirs.createTask("flat");
			search = new FlatSearch(parameters, dirs, cath, query, target);
			System.out.println(target.getFailed() + " target structures failed to parse, successfull " + target.size());
		} else if (mode == Mode.HIERARCHICAL_SEARCH) {
			Hierarchy hierarchy = createHierarchy();
			search = new HierarchicalSearch(parameters, dirs, cath, query, hierarchy);
		} else {
			throw new RuntimeException();
		}
		Alignments alignments = search.run();
		OutputTable outputTable = new OutputTable(dirs.getTableFile());
		outputTable.generateTable(alignments);
		if (parameters.isVisualize()) {
			OutputVisualization outputVisualization = new OutputVisualization(dirs, alignments);
			outputVisualization.generate();
		}
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
			targetStructures.addAll(cath.getHomologousSuperfamilies().getRepresentantSources());
		}
		targetStructures.setMax(parameters.getMaxDbSize());
		targetStructures.shuffle();
		return targetStructures;
	}

	private Hierarchy createHierarchy() {
		CathHierarchyFactory hierarchyFactory = new CathHierarchyFactory(parameters, dirs, cath);
		Hierarchy hierarchy = hierarchyFactory.createFromCath();
		return hierarchy;
	}

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
