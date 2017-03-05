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

	public Benchmark() {
		dirs = new Directories(new File("e:/data/mmtf-benchmark"));
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

		Timer.start("mmtf-hadoop");
		p.parseHadoop();
		Timer.stop("mmtf-hadoop");

		Timer.start("mmtf");
		for (String c : codes) {
			p.parseMmtfToBiojava(c);
		}
		Timer.stop("mmtf");

		/*Timer.start("pdb");
		for (String c : codes) {
			p.parsePdbToBiojava(c);
		}
		Timer.stop("pdb");

		Timer.start("mmcif");
		for (String c : codes) {
			p.parseCifToBiojava(c);
		}
		Timer.stop("mmcif");
		*/
		Timer.print();
	}

	public void run() throws Exception {
		download();
		benchmark();
	}

	public static void main(String[] args) throws Exception {
		Benchmark b = new Benchmark();
		b.run();
	}

}
