package analysis;

import algorithm.scoring.EquivalenceOutput;
import biword.Index;
import analysis.benchmarking.StructurePair;
import analysis.benchmarking.PairsSource;
import algorithm.BiwordAlignmentAlgorithm;
import global.Parameters;
import global.io.Directories;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.align.util.AlignmentTools;
import pdb.SimpleStructure;
import pdb.Structures;
import util.Pair;
import util.Time;
import util.Timer;

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
	//private EquivalenceOutput eo;
	private int pairNumber = 100000;

	private enum Mode {
		FRAGMENT_DB_SEARCH, PAIRWISE_ALIGNMENTS, FRAGMENT, FATCAT, CLICK_SAVE, CLICK_EVAL
	}
	//private Mode mode = Mode.CLICK_EVAL;
	//private Mode mode = Mode.CLICK_SAVE;
	//private Mode mode = Mode.FATCAT;
	//private Mode mode = Mode.FRAGMENT;
	private Mode mode = Mode.FRAGMENT_DB_SEARCH;

	public void test() {
		long time1 = System.nanoTime();
		if (mode == Mode.PAIRWISE_ALIGNMENTS) {
			try {
				dirs.createJob();
				PairsSource pairs = new PairsSource(dirs, PairsSource.Source.MALISAM);
				for (StructurePair pair : pairs) {
					dirs.createTask(pair.a + "_" + pair.b);
					Time.start("init"); // 5cgo, 1w5h
					Structures target = new Structures(dirs);
					target.add(pair.a);
					//StructureProvider target = StructureProvider.createFromPdbCodes();
					target.setMax(1);
					target.shuffle(); // nejak se to seka, s timhle nebo bez, kde?					
					Index index = new Index(dirs, target);
					System.out.println("Biword index created.");
					BiwordAlignmentAlgorithm baa = new BiwordAlignmentAlgorithm(dirs, Parameters.create().visualize());
					Time.stop("init");
					Structures query = new Structures(dirs);
					query.add(pair.b);
					EquivalenceOutput eo = new EquivalenceOutput(dirs);
					baa.search(query.get(0), target, index, eo, 0);
				}
				CsvMerger csv = new CsvMerger(dirs);
				csv.print();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if (mode == Mode.FRAGMENT_DB_SEARCH) {
			dirs.createJob();
			dirs.createTask("");
			Structures targetStructures = new Structures(dirs);
			targetStructures.addFromPdbCodes();
			targetStructures.setMax(10);
			targetStructures.shuffle();
			Time.start("init");
			Index index = new Index(dirs, targetStructures);
			System.out.println("Biword index created.");
			BiwordAlignmentAlgorithm baa = new BiwordAlignmentAlgorithm(dirs, Parameters.create().visualize());
			Time.stop("init");
			Structures queryStructure = new Structures(dirs);
			queryStructure.addFromPdbCode("1cv2");
			EquivalenceOutput eo = new EquivalenceOutput(dirs);
			try {
				Time.start("query");
				baa.search(queryStructure.get(0), targetStructures, index, eo, 0);
				Time.stop("query");
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			Time.print();
		} else {
			PairLoader pg = new PairLoader(dirs.getTopologyIndependentPairs(), false);
			for (int i = 0; i < Math.min(pairNumber, pg.size()); i++) {
				try {
					Pair<String> pair = pg.getNext();
					System.out.println(i + " " + pair.x + " " + pair.y);
					switch (mode) {
						case FATCAT:
							fatcat(pair, i + 1);
							break;
						case FRAGMENT:
							fragment(pair, i + 1);
							break;
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

	public SimpleStructure getSimpleStructure(String id) throws IOException {
		throw new UnsupportedOperationException();
		//return StructureFactory.convertProteinChains(provider.getSingleChain(id), id);
	}

	/*private void fragmentSearch(String query, String[] database, int alignmentNumber) throws IOException {
		BiwordAlignmentAlgorithm baa = new BiwordAlignmentAlgorithm(dirs, Parameters.create().visualize());
		int i = 0;
		for (String databaseItem : database) {
			//System.out.println("db entry " + (i++) + " " + Runtime.getRuntime().totalMemory() / 1000000);
			baa.prepareBiwordDatabase(getSimpleStructure(databaseItem));
		}
		UniversalBiwordGrid grid = baa.build();
		baa.search(getSimpleStructure(query), grid, eo, alignmentNumber);
	}*/
	private void fragment(Pair<String> pair, int alignmentNumber) throws IOException {
		SimpleStructure a = getSimpleStructure(pair.x);
		SimpleStructure b = getSimpleStructure(pair.y);
		BiwordAlignmentAlgorithm fa = new BiwordAlignmentAlgorithm(dirs, Parameters.create().visualize());
		throw new UnsupportedOperationException();
		//fa.align(new AlignablePair(a, b), eo, alignmentNumber);
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

	private void fatcat(Pair<String> pair, int alignmentNumber) throws IOException {
		/*List<Chain> c1 = provider.getSingleChain(pair.x);
		List<Chain> c2 = provider.getSingleChain(pair.y);
		try {
			StructureAlignment algorithm = StructureAlignmentFactory.getAlgorithm(
				FatCatRigid.algorithmName);
			Atom[] ca1 = StructureFactory.getAtoms(c1);
			assert ca1.length > 0 : c1.get(0);
			Atom[] ca2 = StructureFactory.getAtoms(c2);
			assert ca1.length > 0 : c2.get(0);
			FatCatParameters params = new FatCatParameters();
			AFPChain afpChain = algorithm.align(ca1, ca2, params);
			afpChain.setName1(pair.x);
			afpChain.setName2(pair.y);
			Structure s = createArtificalStructure(afpChain, ca1, ca2);
			SimpleStructure a = StructureFactory.convertProteinChains(s.getModel(0), pair.x);
			SimpleStructure b = StructureFactory.convertProteinChains(s.getModel(1), pair.y);
			ResidueAlignment eq = WordAlignmentFactory.create(a, b);
			eo.saveResults(eq, 0, 0);
			eo.visualize(eq, null, 0, alignmentNumber, 1);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}*/
	}

	private Structure createArtificalStructure(AFPChain afpChain, Atom[] ca1,
		Atom[] ca2) throws StructureException {
		if (afpChain.getNrEQR() < 1) {
			return AlignmentTools.getAlignedStructure(ca1, ca2);
		}
		Group[] twistedGroups = AlignmentTools.prepareGroupsForDisplay(afpChain, ca1, ca2);
		List<Atom> twistedAs = new ArrayList<>();
		for (Group g : twistedGroups) {
			if (g == null) {
				continue;
			}
			if (g.size() < 1) {
				continue;
			}
			Atom a = g.getAtom(0);
			twistedAs.add(a);
		}
		Atom[] twistedAtoms = twistedAs.toArray(new Atom[twistedAs.size()]);
		List<Group> hetatms = StructureTools.getUnalignedGroups(ca1);
		List<Group> hetatms2 = StructureTools.getUnalignedGroups(ca2);
		//Atom[] arr1 = DisplayAFP.getAtomArray(ca1, hetatms);
		//Atom[] arr2 = DisplayAFP.getAtomArray(twistedAtoms, hetatms2);
		Structure artificial = AlignmentTools.getAlignedStructure(ca1, ca2);
		return artificial;
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
					case "fatcat":
						mode = Mode.FATCAT;
						break;
					case "fragment":
						mode = Mode.FRAGMENT;
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
			test();
		} catch (ParseException exp) {
			System.err.println("Parsing arguments has failed: " + exp.getMessage());
		}

	}

	public static void main(String[] args) {
		Job m = new Job();
		m.run(args);
	}

}
