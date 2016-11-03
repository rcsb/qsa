package analysis;

import fragments.FragmentsAligner;
import io.Directories;
import pdb.MmtfStructureProvider;
import pdb.SimpleStructure;
import spark.interfaces.AlignablePair;
import spark.interfaces.Alignment;

public class PairTest {

	public void test() {
		 /*String[][] cases = { { "1cv2", "1iz7" }, { "3qt3", "4gp8" }, {
		 "1fxi", "1ubq" } };
		int c = 2;*/
		String[][] cases = { { "1fxi", "1ubq" }, { "1ten", "3hhr" }, { "3hla", "2rhe" }, { "2aza", "1paz" },
				{ "1cew", "1mol" }, { "1cid", "2rhe" }, { "1crl", "1ede" }, { "2sim", "1nsb" }, { "1bge", "2gmf" },
				{ "1tie", "4fgf" } };
		int c = 9;
		// 8, 9 kind of undecided 		
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
		// FatcatAlignment aw = (((FatcatAlignment) al));
		// System.out.println(aw.get());
	}

	public static void main(String[] args) {
		PairTest m = new PairTest();
		m.test();
	}

}
