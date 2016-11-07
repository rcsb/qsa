package analysis;

import fragments.FragmentsAligner;
import io.Directories;
import pdb.MmtfStructureProvider;
import pdb.SimpleStructure;
import spark.interfaces.AlignablePair;
import spark.interfaces.Alignment;

public class PairTest {

	public void test() {

		// String[][] cases = { { "1cv2", "1iz7" }, { "3qt3", "4gp8" }, {
		// "1fxi", "1ubq" } };
		// int c = 1;
		// tough: 1

		String[][] cases = { { "1fxi", "1ubq" }, { "1ten", "3hhr" }, { "3hla", "2rhe" }, { "2aza", "1paz" },
				{ "1cew", "1mol" }, { "1cid", "2rhe" }, { "1crl", "1ede" }, { "2sim", "1nsb" }, { "1bge", "2gmf" },
				{ "1tie", "4fgf" } };
		// int c = 0;

		// TODO 1,2,3 loaders

		// 6 is fail all alpha
		// 7 is probably ok
		// 8 maybe fail, definitelly not first
		// 9 clear fail

		for (int c = 0; c < cases.length; c++) {
			// 8, 9 kind of undecided
			Table table = new Table();
			String codeA = cases[c][0];
			String codeB = cases[c][1];
			Directories dir = Directories.createDefault();
			FragmentsAligner saa = new FragmentsAligner(dir);
			saa.setVisualize(true);

			// saa = new Fatcat();
			MmtfStructureProvider provider = new MmtfStructureProvider(dir.getMmtf().toPath());
			SimpleStructure a = provider.getStructure(codeA);
			SimpleStructure b = provider.getStructure(codeB);
			Alignment al = saa.align(new AlignablePair(a, b));
			System.out.println("SCORE " + al.getScore() + " !!");	
			table.add(al.getScore()).add(c).line();
			table.print();
		}
		
	}

	public static void main(String[] args) {
		PairTest m = new PairTest();
		m.test();
	}

}
