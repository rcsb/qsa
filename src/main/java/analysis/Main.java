package analysis;

import java.io.File;

import alignment.Fatcat;
import benchmark.TmAlignBenchmark;
import io.Directories;
import spark.interfaces.MassAligner;
import spark.interfaces.StructureSetProvider;

public class Main {

	Directories dir;
	
	public Main(File home) {
		dir = new Directories(home);
	}
	
	public void runFatcat() {	
		StructureSetProvider ssp = new TmAlignBenchmark(dir.getTmBenchmark(), dir.getMmtf());
		MassAligner ma = new MassAligner(new Fatcat(), ssp.get(), new MySerializer(dir.getFatcatResults()));
		ma.run();
	}
	
	public void run() {
		runFatcat();
	}
	
	public static void main(String[] args) {		
		String home;
		if (args.length == 0) {
			home = "/Users/antonin/data/qsa";
		} else {
			home = args[0];
		}
		Main m = new Main(new File(home));		
		long time1 = System.nanoTime();
		m.run();
		long time2 = System.nanoTime();
		long time = (time2 - time1) / 1000000;
		System.out.println("Finished in " + time + " ms.");
	}
	
}
