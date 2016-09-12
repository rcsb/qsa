package fragments;

import org.junit.Test;

import alignment.Fatcat;
import io.Directories;
import pdb.MmtfStructureProvider;
import pdb.SimpleStructure;
import spark.interfaces.AlignablePair;
import spark.interfaces.Alignment;
import spark.interfaces.AlignmentWrapper;
import spark.interfaces.StructureAlignmentAlgorithm;

public class FragmentsAlignerTest {

	@Test
	public void test() {
		Directories dir = Directories.createDefault();
		Parameters par = new Parameters();
		StructureAlignmentAlgorithm saa = new FragmentsAligner(par, dir);
		//saa = new Fatcat();
		MmtfStructureProvider provider = new MmtfStructureProvider(dir.getMmtf().toPath());
		SimpleStructure a = provider.getStructure("1cv2");
		SimpleStructure b = provider.getStructure("1iz7");
		Alignment al = saa.align(new AlignablePair(a, b));
		AlignmentWrapper aw = (((AlignmentWrapper) al));
		System.out.println(aw.get());
	}

}
