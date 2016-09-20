package analysis;

import java.io.File;

import alignment.MyFatcat;
import benchmark.PairsProvider;
import benchmark.PdbListParser;
import fragments.FragmentsAligner;
import io.Directories;
import pdb.PdbChainId;
import spark.interfaces.MassAligner;

public class AnalysisLauncher {

	Directories dir;

	public AnalysisLauncher(File home) {
		dir = new Directories(home);
	}

	private PdbChainId[] getTmAlignDataset() {
		// return new FixedArrayBenchmark(dir.getTmBenchmark(),
		// dir.getMmtf()).get();
		return PdbListParser.readPdbCodes(dir.getTmBenchmark(), 0, 5);
	}
	
	private PdbChainId[] getPdbDataset() {
		return PdbListParser.readPdbCodes(dir.getPdbBenchmark(), 0, 4);
	}


	/*
	 * public void runFatcat() { MassAligner ma = new MassAligner(new
	 * MyFatcat(), getDataset(), new MySerializer(dir.getFatcatResults()));
	 * ma.run(); }
	 * 
	 * public void runFragments() { MassAligner ma = new MassAligner(new
	 * FragmentsAligner(dir), getDataset(), new
	 * MySerializer(dir.getFragmentsResults())); ma.addAlgorithm(saa, dir.get);
	 * ma.run(); }
	 */

	public void runBoth() {	
		PdbChainId[] ids = getPdbDataset();	
		System.out.println(ids.length + " pdb id loaded.");
		PairsProvider b = new PairsProvider(dir);				
		MassAligner ma = new MassAligner(b, new MySerializer(dir.getAlignmentObjects()), dir.getAlignmentCsv());
		ma.addAlgorithm(new MyFatcat());
		ma.addAlgorithm(new FragmentsAligner(dir));
		ma.run();
	}

	public void run() {
		// runFatcat();
		// runFragments();
		runBoth();
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
