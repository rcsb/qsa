package org.rcsb.mmtf.benchmark;

import io.Directories;
import java.io.File;
import java.util.List;
import util.Timer;

/**
 *
 * @author Antonin Pavelka
 */
public class Benchmark {

	private Directories dirs;
	private String beforeDate = "12/01/2016";

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

	public void benchmark() {
		Parser p = new Parser(dirs);
		Downloader d = new Downloader(dirs, beforeDate);
		List<String> codes = d.getCodes();
		ResultWriter r = new ResultWriter(dirs.getResults());
		Counter counter;

		
		Timer.print();
		for (int i = 0; i < 50; i++) {
			p.timesPerStructure(r);
		}
		
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

	public void run() throws Exception {
		//download();
		//benchmark();

		Downloader d = new Downloader(dirs, beforeDate);
		System.out.println("Downloading MMTF files:");
		d.downloadMmtf();
		
	}

	public static void main(String[] args) throws Exception {
		Benchmark b = new Benchmark(args[0]);
		b.run();
	}

}
