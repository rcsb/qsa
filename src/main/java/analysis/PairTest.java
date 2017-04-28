package analysis;

import alignment.score.Equivalence;
import alignment.score.EquivalenceFactory;
import alignment.score.EquivalenceOutput;
import fragments.FragmentsAligner;
import io.Directories;
import io.LineFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.align.StructureAlignment;
import org.biojava.nbio.structure.align.StructureAlignmentFactory;
import org.biojava.nbio.structure.align.fatcat.FatCatRigid;
import org.biojava.nbio.structure.align.fatcat.calc.FatCatParameters;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.align.util.AlignmentTools;
import pdb.StructureFactory;
import pdb.SimpleStructure;
import spark.interfaces.AlignablePair;
import util.Pair;

public class PairTest {

	private Directories dirs;
	private EquivalenceOutput eo;
	private StructureFactory provider;
	private int pairNumber = 10000;

	private enum Mode {
		FRAGMENT, FATCAT, CLICK_SAVE, CLICK_EVAL
	}
	//private Mode mode = Mode.CLICK_EVAL;
	//private Mode mode = Mode.CLICK_SAVE;
	//private Mode mode = Mode.FATCAT;
	private Mode mode = Mode.FRAGMENT;

	public void test() {
		long time1 = System.nanoTime();
		PairGeneratorRandom pg = new PairGeneratorRandom(dirs.getCathS20());
		//PairLoader pg = new PairLoader(dirs.getTopologyIndependentPairs(), false);
		//PairLoader pg = new PairLoader(dirs.getCustomPairs(), false);
		//PairLoader pg = new PairLoader(dirs.getHomstradPairs(), true);
		//PairLoader pg = new PairLoader(dirs.getFailedPairs(), false);
		//PairLoaderClick pg = new PairLoaderClick(dirs.getClickOutputDir());
		//pg.setNoDomain(true);
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
				//System.out.println("Total time: " + ms);
				//System.out.println("Per alignment: " + (ms / i));

			} catch (Error ex) {
				ex.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		long time2 = System.nanoTime();
		double s = ((double) (time2 - time1)) / 1000000000;
		System.out.println("Total time: " + s);

	}

	public void saveStructures(Pair<String> pair) throws IOException {
		String[] ids = {pair.x, pair.y};
		for (String id : ids) {
			Path p = dirs.getClickInput(pair, id);
			List<Chain> chains = provider.getSingleChain(id);
			assert chains.size() == 1 : pair.x + " " + pair.y + " " + chains.size();
			LineFile lf = new LineFile(p.toFile());
			lf.write(chains.get(0).toPDB());
		}

	}

	public SimpleStructure getSimpleStructure(String id) throws IOException {
		return StructureFactory.convert(provider.getSingleChain(id), id);
	}

	private void fragment(Pair<String> pair, int alignmentNumber) throws IOException {
		SimpleStructure a = getSimpleStructure(pair.x);
		SimpleStructure b = getSimpleStructure(pair.y);
		FragmentsAligner fa = new FragmentsAligner(dirs, true);
		fa.align(new AlignablePair(a, b), eo, alignmentNumber);
	}

	private void clickEvaluation(Pair<String> pair, int alignmentNumber) throws IOException {
		// TODO solve changes case

		System.out.println(dirs.getClickOutput(pair, pair.x, pair.y).toString());
		System.out.println(dirs.getClickOutput(pair, pair.x, pair.y).toString());
		Structure sa = provider.getStructurePdb(dirs.getClickOutput(pair, pair.x, pair.y).toString());
		Structure sb = provider.getStructurePdb(dirs.getClickOutput(pair, pair.y, pair.x).toString());
		SimpleStructure a = StructureFactory.convert(sa.getModel(0), pair.x);
		SimpleStructure b = StructureFactory.convert(sb.getModel(0), pair.y);
		Equivalence eq = EquivalenceFactory.create(a, b);
		eo.saveResults(eq);
		eo.visualize(eq, null, alignmentNumber, 1);
	}

	private void fatcat(Pair<String> pair, int alignmentNumber) throws IOException {
		List<Chain> c1 = provider.getSingleChain(pair.x);
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
			SimpleStructure a = StructureFactory.convert(s.getModel(0), pair.x);
			SimpleStructure b = StructureFactory.convert(s.getModel(1), pair.y);
			Equivalence eq = EquivalenceFactory.create(a, b);
			eo.saveResults(eq);
			eo.visualize(eq, null, alignmentNumber, 1);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Structure createArtificalStructure(AFPChain afpChain, Atom[] ca1,
		Atom[] ca2) throws StructureException {
		if (afpChain.getNrEQR() < 1) {
			return AlignmentTools.getAlignedStructure(ca1, ca2);
		}
		Group[] twistedGroups = AlignmentTools.prepareGroupsForDisplay(afpChain, ca1, ca2);
		List<Atom> twistedAs = new ArrayList<Atom>();
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
				dirs = Directories.createDefault();
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
			eo = new EquivalenceOutput(dirs);
			provider = new StructureFactory(dirs);
			test();
		} catch (ParseException exp) {
			System.err.println("Parsing arguments has failed: " + exp.getMessage());
		}

	}

	public static void main(String[] args) {
		PairTest m = new PairTest();
		m.run(args);
	}

}
