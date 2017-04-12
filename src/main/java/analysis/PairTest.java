package analysis;

import data.SubstructurePair;
import data.SubstructurePairs;
import fragments.FragmentsAligner;
import io.Directories;
import java.io.File;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import pdb.StructureFactory;
import pdb.SimpleStructure;
import script.PairGenerator;
import spark.interfaces.AlignablePair;
import spark.interfaces.Alignment;
import util.Pair;

public class PairTest {

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

	public void testSmallDataset(Directories dirs) {
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

	public void test(Directories dirs) {
		PairGenerator pg = new PairGenerator(dirs.getCathS20());
		for (int i = 0; i < 100000; i++) {
			try {
				Pair<String> pair = pg.getRandom();
				System.out.println(pair);
				FragmentsAligner fa = new FragmentsAligner(dirs);
				StructureFactory provider = new StructureFactory(dirs);
				SimpleStructure a = provider.getStructure(pair.x);
				SimpleStructure b = provider.getStructure(pair.y);
				Alignment al = fa.align(new AlignablePair(a, b));
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

		Directories dirs;

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

			test(dirs);

		} catch (ParseException exp) {
			System.err.println("Parsing arguments has failed: " + exp.getMessage());
		}

	}

	public static void main(String[] args) {
		PairTest m = new PairTest();
		m.run(args);
	}

}
