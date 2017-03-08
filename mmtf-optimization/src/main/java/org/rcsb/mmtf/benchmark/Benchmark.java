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

	public Benchmark(String path) {
		dirs = new Directories(new File(path));
	}

	public void download() {
		Downloader d = new Downloader(dirs);

		System.out.println("Downloading MMTF files:");
		Timer.start("mmtf-download");
		d.downloadMmtf();
		Timer.stop("mmtf-download");

		/*System.out.println("Downloading PDB files:");
		Timer.start("pdb-download");
		d.downloadPdb();
		Timer.stop("pdb-download");
		
		System.out.println("Downloading mmCIF files:");
		Timer.start("mmcif-download");
		d.downloadCif();
		Timer.stop("mmcif-download");
		 */
		Timer.print();
	}

	public void benchmark() {
		Parser p = new Parser(dirs);
		Downloader d = new Downloader(dirs);
		List<String> codes = d.getCodes();
		Results r = new Results(dirs.getResults());
		
		p.timesPerStructure(r);

		/*Timer.start("mmtf-hadoop");
		p.parseHadoop();
		Timer.stop("mmtf-hadoop");
		Timer.print();

		Counter counter = new Counter();
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
		Timer.print();

		counter = new Counter();
		Timer.start("mmcif");
		for (String c : codes) {
			p.parseCifToBiojava(c);
		counter.next();
		}
		Timer.stop("mmcif");
		Timer.print();
		 */
	}

	public void run() throws Exception {
		//download();
		benchmark();
	}

	public static void main(String[] args) throws Exception {
		Benchmark b = new Benchmark(args[0]);
		b.run();
	}

}
