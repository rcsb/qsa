package org.rcsb.mmtf.benchmark;

import io.Directories;
import io.LineFile;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import util.Timer;

/**
 *
 * @author Antonin Pavelka
 */
public class Benchmark {

	private Directories dirs;

	public static final String beforeDate = "2016-12-01";

	public Benchmark(String path) {
		dirs = new Directories(new File(path));
	}

	public void download() {
		Downloader d = new Downloader(dirs, beforeDate);
		System.out.println("Downloading MMTF files:");
		Timer.start("mmtf-download");
		d.downloadMmtf();
		Timer.stop("mmtf-download");
		Timer.print();
		System.out.println("Downloading PDB files:");
		Timer.start("pdb-download");
		d.downloadPdb();
		Timer.stop("pdb-download");
		Timer.print();
		System.out.println("Downloading mmCIF files:");
		Timer.start("mmcif-download");
		d.downloadCif();
		Timer.stop("mmcif-download");
		Timer.print();
	}
	
	public void download1000() {
		Downloader d = new Downloader(dirs, beforeDate);
		d.downloadSample(10);
	}

	public void benchmark() {
		Parser p = new Parser(dirs);
		Downloader d = new Downloader(dirs, beforeDate);
		List<String> codes = d.getCodes();
		Counter counter;

		counter = new Counter();
		Timer.start("mmcif");
		for (String c : codes) {
			p.parseCifToBiojava(c);
			counter.next();
		}
		Timer.stop("mmcif");
		Timer.print();

		Timer.start("mmtf-hadoop");
		p.parseHadoop();
		Timer.stop("mmtf-hadoop");
		Timer.print();

		counter = new Counter();
		Timer.start("mmtf");
		for (String c : codes) {
			p.parseMmtfToBiojava(c);
			counter.next();
		}
		Timer.stop("mmtf");
		Timer.print();

		counter = new Counter();
		Timer.start("pdb");
		for (String c : codes) {
			p.parsePdbToBiojava(c);
			counter.next();
		}
		Timer.stop("pdb");
	}

	public void benchmarkSamples() throws IOException {
		File[] files = {dirs.getSample1000(), dirs.getSample25(),
			dirs.getSample50(), dirs.getSample75()};
		String[] names = {"sample_of_1000", "quantile_25", "median",
			"quantile_75"};
		Parser p = new Parser(dirs);
		Results results = new Results(dirs);

		// JIT
		Downloader d = new Downloader(dirs, beforeDate);
		List<String> allCodes = d.getCodes();
		Random random = new Random(1);
		for (int i = 0; i < 10000; i++) {
			p.parseMmtfToBiojava(allCodes.get(random.nextInt(allCodes.size())));
		}

/*		for (int index = 0; index < files.length; index++) {
			File f = files[index];
			LineFile lf = new LineFile(f);
			List<String> lines = lf.readLines();
			String[] codes = new String[lines.size()];
			for (int i = 0; i < codes.length; i++) {
				codes[i] = lines.get(i).substring(0, 4);
			}
			
			Timer timer = new Timer();
			timer.start();
			for (String code : codes) {
				p.parseMmtfToBiojava(code);
			}
			timer.stop();
			results.add(names[index] + "_mmtf", timer.get(), "ms");
			
			timer = new Timer();
			timer.start();
			for (String code : codes) {
				p.parsePdbToBiojava(code);
			}
			timer.stop();
			results.add(names[index] + "_pdb", timer.get(), "ms");
			
			timer = new Timer();
			timer.start();
			for (String code : codes) {
				p.parseCifToBiojava(code);
			}
			timer.stop();
			results.add(names[index] + "_cif", timer.get(), "ms");
		}
*/
		Timer timer = new Timer();
		timer.start();
		p.parseMmtfToBiojava("3j3q");
		timer.stop();
		results.add("largest_mmtf", timer.get(), "ms");

		timer = new Timer();
		timer.start();
		p.parseCifToBiojava("3j3q");
		timer.stop();
		results.add("largest_cif", timer.get(), "ms");
		
	}

	public void run() throws Exception {
		//benchmarkSamples();
		//download();
		download1000();

		/*Downloader d = new Downloader(dirs, beforeDate);
		System.out.println("Downloading MMTF files:");
		d.downloadMmtf();*/
		//saveSizes(new File("c:/kepler/rozbal/pdb_sizes.csv"));
		/*File sampleF1 = new File("c:/kepler/rozbal/sample_1.csv");
		File sampleF2 = new File("c:/kepler/rozbal/sample_2.csv");
		sample(new File("c:/kepler/rozbal/pdb_sizes.csv"), sampleF1, 1);
		sample(new File("c:/kepler/rozbal/pdb_sizes.csv"), sampleF2, 2);
		mmtfGraph(sampleF1, new File("c:/kepler/rozbal/mmtf_graph_1.csv"));
		mmtfGraph(sampleF2, new File("c:/kepler/rozbal/mmtf_graph_2.csv"));*/
		//benchmark();
	}

	public static void main(String[] args) throws Exception {
		Benchmark b = new Benchmark(args[0]);
		b.run();
	}

}
