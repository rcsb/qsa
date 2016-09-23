package analysis;

import fragments.FragmentsAligner;
import io.Directories;
import pdb.MmtfStructureProvider;
import pdb.SimpleStructure;
import spark.interfaces.AlignablePair;
import spark.interfaces.Alignment;
import spark.interfaces.FatcatAlignment;
import spark.interfaces.StructureAlignmentAlgorithm;

public class PairTest {

	public void test() {
		//String codeA = "1cv2";
		//String codeB = "1iz7";
		String codeA = "3qt3";
		String codeB = "4gp8";
		Directories dir = Directories.createDefault();
		FragmentsAligner saa = new FragmentsAligner(dir);
		saa.setVisualize(true);

		// saa = new Fatcat();
		MmtfStructureProvider provider = new MmtfStructureProvider(dir.getMmtf().toPath());
		SimpleStructure a = provider.getStructure(codeA);
		SimpleStructure b = provider.getStructure(codeB);
		Alignment al = saa.align(new AlignablePair(a, b));
		FatcatAlignment aw = (((FatcatAlignment) al));
		System.out.println(aw.get());
	}

	public static void main(String[] args) {
		PairTest m = new PairTest();
		m.test();
	}

}
