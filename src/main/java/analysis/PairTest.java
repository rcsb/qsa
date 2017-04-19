package analysis;

import alignment.score.Equivalence;
import alignment.score.EquivalenceFactory;
import alignment.score.EquivalenceOutput;
import data.SubstructurePair;
import data.SubstructurePairs;
import fragments.FragmentsAligner;
import io.Directories;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.align.StructureAlignment;
import org.biojava.nbio.structure.align.StructureAlignmentFactory;
import org.biojava.nbio.structure.align.fatcat.FatCatRigid;
import org.biojava.nbio.structure.align.fatcat.calc.FatCatParameters;
import org.biojava.nbio.structure.align.gui.DisplayAFP;
import org.biojava.nbio.structure.align.model.AFPChain;
import pdb.StructureFactory;
import pdb.SimpleStructure;
import spark.interfaces.AlignablePair;
import util.Pair;

public class PairTest {

	private Directories dirs;
	private EquivalenceOutput eo;
	StructureFactory provider;

	public void test() {
		long time1 = System.nanoTime();
		//PairGeneratorRandom pg = new PairGeneratorRandom(dirs.getCathS20());
		PairLoader pg = new PairLoader(dirs.getTopologyIndependentPairs());
		for (int i = 0; i < Math.min(1000, pg.size()); i++) {
			try {
				Pair<String> pair = pg.getNext();
				System.out.println(i + " " + pair.x + " " + pair.y);
				if (false) {
					fatcat(pair);
				} else {
					fragment(pair);
				}

				long time2 = System.nanoTime();
				double ms = ((double) (time2 - time1)) / 1000000;
				//System.out.println("Total time: " + ms);
				//System.out.println("Per alignment: " + (ms / i));

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private List<Chain> getStructure(String id) throws IOException {
		if (id.length() == 4 || id.length() == 5) { // PDB code
			if (id.length() == 4) {
				return provider.getStructureMmtf(id).getChains();
			} else {
				String code = id.substring(0, 4);
				String chain = id.substring(4, 5);
				List<Chain> chains = provider.getStructureMmtf(code).getChains();
				return StructureFactory.filter(chains, chain);
			}
		} else { // CATH domain id
			return provider.getStructurePdb(id).getChains();
		}
	}

	public SimpleStructure getSimpleStructure(String id) throws IOException {
		return StructureFactory.convert(getStructure(id), id);
	}

	private void fragment(Pair<String> pair) throws IOException {
		SimpleStructure a = getSimpleStructure(pair.x);
		SimpleStructure b = getSimpleStructure(pair.y);
		FragmentsAligner fa = new FragmentsAligner(dirs);
		fa.align(new AlignablePair(a, b), eo);
	}

	private void fatcat(Pair<String> pair) throws IOException {
		List<Chain> c1 = getStructure(pair.x);
		List<Chain> c2 = getStructure(pair.y);
		try {
			StructureAlignment algorithm = StructureAlignmentFactory.getAlgorithm(
				FatCatRigid.algorithmName);
			Atom[] ca1 = StructureFactory.getAtoms(c1);
			Atom[] ca2 = StructureFactory.getAtoms(c2);
			FatCatParameters params = new FatCatParameters();
			AFPChain afpChain = algorithm.align(ca1, ca2, params);
			afpChain.setName1(pair.x);
			afpChain.setName2(pair.y);
			Structure s = DisplayAFP.createArtificalStructure(afpChain, ca1, ca2);
			SimpleStructure a = StructureFactory.convert(s.getModel(0), pair.x);
			SimpleStructure b = StructureFactory.convert(s.getModel(1), pair.y);
			Equivalence eq = EquivalenceFactory.create(a, b);
			eo.saveResults(eq);
			eo.visualize(eq);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void testSimple() {

		// String[][] cases = { { "1cv2", "1iz7" }, { "3qt3", "4gp8" }, {
		// "1fxi", "1ubq" } };
		// int c = 1;	
		// tough: 1
		String[][] cases = {{"1fxi", "1ubq"}, {"1ten", "3hhr"}, {"3hla", "2rhe"}, {"2aza", "1paz"},
		{"1cew", "1mol"}, {"1cid", "2rhe"}, {"1crl", "1ede"}, {"2sim", "1nsb"}, {"1bge", "2gmf"},
		{"1tie", "4fgf"}};

		//String[][] cases = { { "1fxi", "1ubq" }/*, { "1cv2", "1iz7" } */};
		//String[][] cases = { { "1cv2", "1iz7" } };
		//String[][] cases = { { "1mol", "2sim" } };
		// int c = 0;
		// TODO 1,2,3 loaders
		// 6 is fail all alpha
		// 7 is probably ok
		// 8 maybe fail, definitelly not first
		// 9 clear fail
		//for (int c = 7; c <= 7; c++) {
		for (int c = 0; c < cases.length; c++) {
			// 8, 9 kind of undecided
			Table table = new Table();
			String codeA = cases[c][0];
			String codeB = cases[c][1];
			Directories dirs = Directories.createDefault();
			FragmentsAligner fa = new FragmentsAligner(dirs);

			// saa = new Fatcat();
			StructureFactory provider = new StructureFactory(dirs);

			SimpleStructure a = provider.getStructure(codeA, null);
			SimpleStructure b = provider.getStructure(codeB, null);
			fa.align(new AlignablePair(a, b), eo);

			//System.out.println("SCORE " + al.getScore() + " !!");
			//table.add(al.getScore()).add(c).line();
			//table.print();
		}

	}

	public void testSmallDataset() {
		//SubstructurePairs ssps = SubstructurePairs.parseClick(dirs);
		//SubstructurePairs ssps = SubstructurePairs.parseCustom(dirs);
		SubstructurePairs ssps = SubstructurePairs.generate(dirs);
		System.out.println("Pairs: " + ssps.size());
		for (SubstructurePair ssp : ssps) {
			try {
				FragmentsAligner fa = new FragmentsAligner(dirs);
				StructureFactory provider = new StructureFactory(dirs);
				SimpleStructure a = provider.getStructure(ssp.a.code, ssp.a.cid);
				SimpleStructure b = provider.getStructure(ssp.b.code, ssp.b.cid);
				fa.align(new AlignablePair(a, b), eo);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

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

		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("h")) {
				File home = new File(line.getOptionValue("h"));
				dirs = new Directories(home);
			} else {
				dirs = Directories.createDefault();
			}
			if (line.hasOption("s")) {
				String structures = line.getOptionValue("s");
				dirs.setStructures(structures);
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
