package analysis;

import java.io.File;

import alignment.Fatcat;
import benchmark.TmAlignBenchmark;
import io.Directories;
import spark.interfaces.StructureAligner;
import spark.interfaces.StructurePairsProvider;

public class Main {

	Directories dir;
	
	public Main(File home) {
		dir = new Directories(home);
	}
	
ß	public void runFatcat() {	
		StructurePairsProvider spp = new TmAlignBenchmark(dir.getTmBenchmark(), dir.getMmtf());
		StructureAligner aligner = new StructureAligner(new Fatcat());
		aligner.run(spp.getPairs());		
	}
	
	public static void main(String[] args) {
		String home;
		if (args.length == 0) {
			home = "/Users/antonin/data/qsa";
		} else {
			home = args[0];
		}
		Main m = new Main(new File(home));
		m.runFatcat();
	}
}
