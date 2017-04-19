package analysis;

import alignment.score.Equivalence;
import alignment.score.EquivalenceFactory;
import alignment.score.EquivalenceOutput;
import fragments.FragmentsAligner;
import io.Directories;
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

	public SimpleStructure getSimpleStructure(String id) throws IOException {
		return StructureFactory.convert(provider.getSingleChain(id), id);
	}

	private void fragment(Pair<String> pair) throws IOException {
		SimpleStructure a = getSimpleStructure(pair.x);
		SimpleStructure b = getSimpleStructure(pair.y);
		FragmentsAligner fa = new FragmentsAligner(dirs);
		fa.align(new AlignablePair(a, b), eo);
	}

	private void fatcat(Pair<String> pair) throws IOException {
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
			Structure s = DisplayAFP.createArtificalStructure(afpChain, ca1, ca2);
			SimpleStructure a = StructureFactory.convert(s.getModel(0), pair.x);
			SimpleStructure b = StructureFactory.convert(s.getModel(1), pair.y);
			Equivalence eq = EquivalenceFactory.create(a, b);
			eo.saveResults(eq);
			eo.visualize(eq, 1);
		} catch (Exception e) {
			throw new RuntimeException(e);
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
