package org.rcsb.mmtf.benchmark;

import io.Directories;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureTools;
import util.Timer;

/**
 *
 * @author Antonin Pavelka
 */
public class Benchmark {

	private Directories dirs;
	private String beforeDate = "2016-12-01";

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

	private void saveSizes(File f) throws IOException {
		Downloader d = new Downloader(dirs, beforeDate);
		Parser p = new Parser(dirs);
		Counter counter = new Counter();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for (String c : d.getCodes()) {
				counter.next();
				Structure s = p.parseMmtfToBiojava(c);
				bw.write(c + "," + StructureTools.getNrAtoms(s) + "\n");
			}
		}
	}

	private void sample(File in, File out, int seed) throws IOException {
		Random random = new Random(seed);

		Map<Integer, Bucket> buckets = new HashMap<>();
		List<PdbEntry> all = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(in))) {
			String line;
			while ((line = br.readLine()) != null) {
				String code = line.substring(0, 4);
				int size = Integer.parseInt(line.substring(5));
				PdbEntry pe = new PdbEntry(code, size);
				all.add(pe);
			}
		}
		Collections.sort(all);
		System.out.println(all.get(0));
		System.out.println(all.get(all.size() - 1));

		int max = all.get(all.size() - 1).getNumAtoms();

		int bNumber = 1000;
		int bMax = 100;
		int bMin = 1;

		System.out.println(max);

		for (int i = 0; i < bNumber; i++) {
			double d = (double) max / bNumber;
			double lo = i * d;
			double hi = (i + 1) * d;
			Bucket b = new Bucket(lo, hi);
			buckets.put(i, b);
			for (PdbEntry pe : all) {
				int na = pe.getNumAtoms();
				if (lo < na && na <= hi) {
					b.add(pe);
				}
			}
			b.sort();
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
			for (int i : buckets.keySet()) {
				Bucket b = buckets.get(i);
				if (b.size() < bMin) {
					continue;
				}
				while (b.size() > bMax) {
					int r = random.nextInt(b.size());
					b.remove(r);
				}
				bw.write(b.getLo() + "-" + b.getHi() + "\n");
				bw.write(Math.round((b.getLo() + b.getHi()) / 2) + "\n");
				for (PdbEntry pe : b.getAll()) {
					bw.write(pe.getCode() + "," + pe.getNumAtoms() + ";");
				}
				bw.write("\n");
			}
		}

	}

	public void mmtfGraph(File in, File out) throws IOException {
		Parser p = new Parser(dirs);
		try (BufferedReader br = new BufferedReader(new FileReader(in));
			BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
			bw.write("average size,average time (microseconds)\n");
			String line;
			while ((line = br.readLine()) != null) {
				line = br.readLine();
				int center = Integer.parseInt(line);
				line = br.readLine();
				String[] entries = line.split(";");

				for (String e : entries) {
					String code = e.substring(0, 4);
					int size = Integer.parseInt(e.substring(5));
					Timer timer = new Timer();
					timer.start();
					p.parseMmtfToBiojava(code);
					timer.stop();
					long t = timer.getMicro();
					bw.write(size + "," + t + "\n");
				}
			}
		}
	}

	public void run() throws Exception {
		//download();

		/*Downloader d = new Downloader(dirs, beforeDate);
		System.out.println("Downloading MMTF files:");
		d.downloadMmtf();*/
		//saveSizes(new File("c:/kepler/rozbal/pdb_sizes.csv"));
		File sampleF1 = new File("c:/kepler/rozbal/sample_1.csv");
		File sampleF2 = new File("c:/kepler/rozbal/sample_2.csv");
		sample(new File("c:/kepler/rozbal/pdb_sizes.csv"), sampleF1, 1);
		sample(new File("c:/kepler/rozbal/pdb_sizes.csv"), sampleF2, 2);
		mmtfGraph(sampleF1, new File("c:/kepler/rozbal/mmtf_graph_1.csv"));
		mmtfGraph(sampleF2, new File("c:/kepler/rozbal/mmtf_graph_2.csv"));
		//benchmark();
	}

	public static void main(String[] args) throws Exception {
		Benchmark b = new Benchmark(args[0]);
		b.run();
	}

}
