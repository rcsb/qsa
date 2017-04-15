package analysis;

import data.SubstructurePair;
import data.SubstructurePairs;
import fragments.FragmentsAligner;
import io.Directories;
import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.align.StructureAlignment;
import org.biojava.nbio.structure.align.StructureAlignmentFactory;
import org.biojava.nbio.structure.align.fatcat.FatCatRigid;
import org.biojava.nbio.structure.align.fatcat.calc.FatCatParameters;
import org.biojava.nbio.structure.align.gui.DisplayAFP;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.align.model.AfpChainWriter;
import org.biojava.nbio.structure.jama.Matrix;
import pdb.StructureFactory;
import pdb.SimpleStructure;
import script.PairGenerator;
import spark.interfaces.AlignablePair;
import spark.interfaces.Alignment;
import util.Pair;

public class PairTest {

	private Directories dirs;

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
			Alignment al = fa.align(new AlignablePair(a, b));
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
				Alignment al = fa.align(new AlignablePair(a, b));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	public void test() {
		long time1 = System.nanoTime();
		PairGenerator pg = new PairGenerator(dirs.getCathS20());
		for (int i = 0; i < 1000000; i++) {
			try {
				Pair<String> pair = pg.getRandom();
				System.out.println(pair);

				if (true) {
					fatcat(pair);
				} else {
					fragment(pair);
				}

				long time2 = System.nanoTime();
				double ms = ((double) (time2 - time1)) / 1000000;
				System.out.println("Total time: " + ms);
				System.out.println("Per alignment: " + (ms / i));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void fragment(Pair<String> pair) throws IOException {
		StructureFactory provider = new StructureFactory(dirs);
		SimpleStructure a = provider.getSimpleStructure(pair.x);
		SimpleStructure b = provider.getSimpleStructure(pair.y);
		FragmentsAligner fa = new FragmentsAligner(dirs);
		Alignment al = fa.align(new AlignablePair(a, b));
	}

	private void fatcat(Pair<String> pair) throws IOException {
		StructureFactory provider = new StructureFactory(dirs);
		Structure structure1 = provider.getStructure(pair.x);
		Structure structure2 = provider.getStructure(pair.y);

		try {
			// To run FATCAT in the flexible variant say  
			// FatCatFlexible.algorithmName below  
			StructureAlignment algorithm = StructureAlignmentFactory.getAlgorithm(FatCatRigid.algorithmName);

			Atom[] ca1 = StructureTools.getAtomCAArray(structure1);
			Atom[] ca2 = StructureTools.getAtomCAArray(structure2);

			// get default parameters  
			FatCatParameters params = new FatCatParameters();

			System.out.println("xxxxxxxxxxxxxxx");
			System.out.println(ca1[0]);
			System.out.println(ca2[0]);

			

			AFPChain afpChain = algorithm.align(ca1, ca2, params);
			
			Structure s = DisplayAFP.createArtificalStructure(afpChain, ca1, ca2);
			System.out.println(s.toPDB());
			
			

			Matrix[] m = afpChain.getBlockRotationMatrix();
			System.out.println(m.length + " AAAAAAAAAAAAAAAAAAA");
			System.out.println(m[0]);
			afpChain.getBlockShiftVector();

			System.out.println(ca1[0]);
			System.out.println(ca2[0]);

			afpChain.setName1(pair.x);
			afpChain.setName2(pair.y);

			// show original FATCAT output:  
			System.out.println(afpChain.toFatcat(ca1, ca2));

			// show a nice summary print  
			System.out.println(AfpChainWriter.toWebSiteDisplay(afpChain, ca1, ca2));

			// print rotation matrices  
			System.out.println(afpChain.toRotMat());
			//System.out.println(afpChain.toCE(ca1, ca2));  

			// print XML representation  
			//System.out.println(AFPChainXMLConverter.toXML(afpChain,ca1,ca2));  
			//StructureAlignmentDisplay.display(afpChain, ca1, ca2);
		} catch (Exception e) {
			e.printStackTrace();
			return;
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
