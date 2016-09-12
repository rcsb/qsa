package analysis;

import java.io.File;

import alignment.Fatcat;
import benchmark.TmAlignBenchmark;
import fragments.FragmentsAligner;
import fragments.Parameters;
import io.Directories;
import pdb.SimpleStructure;
import spark.interfaces.MassAligner;

public class AnalysisLauncher {

	Directories dir;
	Parameters par;

	public AnalysisLauncher(File home) {
		dir = new Directories(home);
		par = new Parameters();

	}

	private SimpleStructure[] getDataset() {
		return new TmAlignBenchmark(dir.getTmBenchmark(), dir.getMmtf()).get();
	}

	public void runFatcat() {
		MassAligner ma = new MassAligner(new Fatcat(), getDataset(), new MySerializer(dir.getFatcatResults()));
		ma.run();
	}

	public void runFragments() {
		MassAligner ma = new MassAligner(new FragmentsAligner(par, dir), getDataset(),
				new MySerializer(dir.getFragmentsResults()));
		ma.run();
	}

	public void run() {
		//runFatcat();
		runFragments();
	}

	public static void main(String[] args) {
		String home;
		if (args.length == 0) {
			home = "/Users/antonin/data/qsa";
		} else {
			home = args[0];
		}
		AnalysisLauncher m = new AnalysisLauncher(new File(home));
		long time1 = System.nanoTime();
		m.run();
		long time2 = System.nanoTime();
		long time = (time2 - time1) / 1000000;
		System.out.println("Finished in " + time + " ms.");
	}

}
