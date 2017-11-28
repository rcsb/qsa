package analysis;

import biword.Index;
import analysis.benchmarking.StructurePair;
import analysis.benchmarking.PairsSource;
import algorithm.SearchAlgorithm;
import global.Parameters;
import global.io.Directories;
import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import pdb.SimpleStructure;
import pdb.StructureFilter;
import pdb.Structures;
import util.Pair;
import util.Time;

/**
 *
 * Main class. Allows to run searches and pairwise comparisons and batches of those.
 *
 * TODO move batch functionality above.
 *
 * @author Antonin Pavelka
 */
public class Job {

	private Directories dirs;
	private int pairNumber = 100000;

	private enum Mode {
		FRAGMENT_DB_SEARCH, PAIRWISE, CLICK_SAVE, CLICK_EVAL
	}
	private Mode mode = Mode.FRAGMENT_DB_SEARCH;

	public void runJob(Parameters parameters) {
		long time1 = System.nanoTime();
		if (mode == Mode.PAIRWISE) {
			runPairwiseAlignment(parameters);
		} else if (mode == Mode.FRAGMENT_DB_SEARCH) {
			runSearch(parameters);
		} else { // TODO move to scripts
			PairLoader pg = new PairLoader(dirs.getTopologyIndependentPairs(), false);
			for (int i = 0; i < Math.min(pairNumber, pg.size()); i++) {
				try {
					Pair<String> pair = pg.getNext();
					System.out.println(i + " " + pair.x + " " + pair.y);
					switch (mode) {
						case CLICK_SAVE:
							saveStructures(pair);
							break;
						case CLICK_EVAL:
							clickEvaluation(pair, i + 1);
							break;
					}
					long time2 = System.nanoTime();
					double ms = ((double) (time2 - time1)) / 1000000;
				} catch (Error ex) {
					ex.printStackTrace();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		long time2 = System.nanoTime();
		double s = ((double) (time2 - time1)) / 1000000000;
		System.out.println("Total time: " + s);
	}

	private void runPairwiseAlignment(Parameters parameters) {
		try {
			dirs.createJob();
			PairsSource pairs = new PairsSource(dirs, PairsSource.Source.MALISAM);
			for (StructurePair pair : pairs) {
				dirs.createTask(pair.a + "_" + pair.b);
				Time.start("init"); // 5cgo, 1w5h
				Structures target = new Structures(parameters, dirs);
				target.add(pair.a);
				//StructureProvider target = StructureProvider.createFromPdbCodes();
				target.setMax(1);
				target.shuffle(); // nejak se to seka, s timhle nebo bez, kde?					
				Index index = new Index(parameters, dirs, target);
				System.out.println("Biword index created.");
				Structures query = new Structures(parameters, dirs);
				query.add(pair.b);
				SearchAlgorithm baa = new SearchAlgorithm(parameters, dirs, query.get(0, 0), target, index,
					parameters.isVisualize());

				//	public SearchAlgorithm(SimpleStructure queryStructure, Structures sp, Index index, Directories dirs,
				//  boolean visualize, EquivalenceOutput eo, int alignmentNumber) {
				Time.stop("init");
				baa.search();
			}
			CsvMerger csv = new CsvMerger(dirs);
			csv.print();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void runSearch(Parameters parameters) {
		dirs.createJob();
		Structures targetStructures = new Structures(parameters, dirs);
		targetStructures.setFilter(new StructureFilter(parameters));
		targetStructures.addFromPdbCodes(dirs.getPdbEntryTypes());
		targetStructures.setMax(parameters.getMaxDbSize());
		targetStructures.shuffle();
		Time.start("init");
		Index index = new Index(parameters, dirs, targetStructures);
		System.out.println("Biword index created.");
		Time.stop("init");
		Structures queryStructures = new Structures(parameters, dirs);
		queryStructures.addFromPdbCodes(dirs.getQueryCodes());
		for (SimpleStructure queryStructure : queryStructures) {
			dirs.createTask("task");
			System.out.println("Query size: " + queryStructures.size() + " residues.");
			SearchAlgorithm baa = new SearchAlgorithm(parameters, dirs, queryStructure, targetStructures,
				index, parameters.isVisualize());
			Time.start("query");
			baa.search();
			Time.stop("query");
			Time.print();
		}
	}

	public void saveStructures(Pair<String> pair) throws IOException {
		throw new UnsupportedOperationException();
		/*	String[] ids = {pair.x, pair.y};
		for (String id : ids) {
			Path p = dirs.getClickInput(pair, id);
			List<Chain> chains = provider.getSingleChain(id);
			assert chains.size() == 1 : pair.x + " " + pair.y + " " + chains.size();
			LineFile lf = new LineFile(p.toFile());
			lf.write(chains.get(0).toPDB());
		}
		 */
	}

	private void clickEvaluation(Pair<String> pair, int alignmentNumber) throws IOException {
		/*System.out.println(dirs.getClickOutput(pair, pair.x, pair.y).toString());
		System.out.println(dirs.getClickOutput(pair, pair.x, pair.y).toString());
		Structure sa = provider.getStructurePdb(dirs.getClickOutput(pair, pair.x, pair.y).toString());
		Structure sb = provider.getStructurePdb(dirs.getClickOutput(pair, pair.y, pair.x).toString());
		SimpleStructure a = StructureFactory.convertProteinChains(sa.getModel(0), pair.x);
		SimpleStructure b = StructureFactory.convertProteinChains(sb.getModel(0), pair.y);
		ResidueAlignment eq = WordAlignmentFactory.create(a, b);
		eo.saveResults(eq, 0, 0);
		eo.visualize(eq, null, 0, alignmentNumber, 1);*/
	}

	private void run(String[] args) {
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
				File home = new File(cl.getOptionValue("h"));
				dirs = new Directories(home);
			} else {
				throw new ParseException("No -h parameter, please specify the home directory.");
			}
			if (cl.hasOption("s")) {
				String structures = cl.getOptionValue("s");
				dirs.setStructures(structures);
			}
			if (cl.hasOption("m")) {
				String sm = cl.getOptionValue("m");
				switch (sm) {
					case "search":
						mode = Mode.FRAGMENT_DB_SEARCH;
						break;
					case "pairwise":
						mode = Mode.PAIRWISE;
						break;
					case "save_click":
						mode = Mode.CLICK_SAVE;
						break;
					case "eval_click":
						mode = Mode.CLICK_EVAL;
						break;
				}
			}
			if (cl.hasOption("n")) {
				String s = cl.getOptionValue("n");
				pairNumber = Integer.parseInt(s);
			}
			Parameters pars = Parameters.create(dirs.getParameters());
			runJob(pars);
		} catch (ParseException exp) {
			System.err.println("Parsing arguments has failed: " + exp.getMessage());
		}

	}

	public static void main(String[] args) {
		Job m = new Job();
		m.run(args);
	}

}
