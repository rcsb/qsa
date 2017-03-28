package org.rcsb.mmtf.benchmark;

import io.Directories;
import io.HadoopSequenceFileConverter;
import io.Tar;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.rcsb.mmtf.utils.Lines;
import util.Timer;

/**
 *
 * Main class to run all the benchmarks.
 *
 * @author Antonin Pavelka
 */
public class Benchmark {

	private Directories dirs;

	public Benchmark(String path) {
		dirs = new Directories(new File(path));
	}

	/**
	 * Downloads the whole PDB in MMTF, PDB and mmCIF file format.
	 */
	public void downloadWholeDatabase() {
		DatasetGenerator d = new DatasetGenerator(dirs);

		System.out.println("Downloading Hadoop sequence files:");
		Timer.start("hsf-download");

		d.downloadHadoopSequenceFiles();
		Timer.stop("hsf-download");
		Timer.print();

		System.exit(5);

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

		System.out.println("Downloading Hadoop sequence file:");
		Timer.start("hsf-download");
		d.downloadHadoopSequenceFiles();
		Timer.stop("hsf-download");
		Timer.print();

	}

	private void transformHsf() throws IOException {
		HadoopSequenceFileConverter.convert(dirs.getHsfReducedOriginal().toString(),
			dirs.getHsfReducedOriginalUntared().toString(),
			dirs.getHsfReduced().toString());
		HadoopSequenceFileConverter.convert(dirs.getHsfFullOriginal().toString(),
			dirs.getHsfFullOriginalUntared().toString(),
			dirs.getHsfFull().toString());
	}

	/**
	 * Runs the benchmark on the whole PDB measuring total time of parsing Hadoop sequence file
	 * (unzipped) and the times for entries in individual MMTF, PDB and mmCIF files.
	 */
	public void benchmarkWholeDatabase() throws IOException {
		Parser p = new Parser(dirs);
		DatasetGenerator d = new DatasetGenerator(dirs);
		List<String> codes = d.getCodes();
		Counter counter;
		Results results = new Results(dirs);

		jit();

		//Timer.start("mmtf-hadoop-reduced");
		//p.parseHadoop(dirs.getHsfReduced().toFile());
		//Timer.stop("mmtf-hadoop-reduced");
		//Timer.print();

		Timer.start("mmtf-hadoop-full");
		p.parseHadoop(dirs.getHsfFull().toFile());
		Timer.stop("mmtf-hadoop-full");
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

		counter = new Counter();
		Timer.start("mmcif");
		for (String c : codes) {
			p.parseCifToBiojava(c);
			counter.next();
		}
		Timer.stop("mmcif");
		Timer.print();

		results.end();
	}

	/**
	 * Does some parsing before measurements, so that the first measurement is not at disadvantage
	 * due to Just In Time compilation.
	 */
	private void jit() {
		Parser p = new Parser(dirs);
		DatasetGenerator d = new DatasetGenerator(dirs);
		List<String> allCodes = d.getCodes();
		Random random = new Random(2); // 2 to work with different structures
		for (int i = 0; i < 100; i++) {
			try {
				p.parseMmtfToBiojava(allCodes.get(random.nextInt(allCodes.size())));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				p.parsePdbToBiojava(allCodes.get(random.nextInt(allCodes.size())));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				p.parseCifToBiojava(allCodes.get(random.nextInt(allCodes.size())));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Measures times of parsing of 1000 random PDB entries, then times for 100 entries of three
	 * characteristic sizes and finally the times for the largest entry.
	 */
	public void benchmarkSamples() throws IOException {
		String prefix = "/mmtf-benchmark/";
		String[][] datasets = {
			Lines.readResource(prefix + "sample_1000.gz"),
			Lines.readResource(prefix + "sample_25.csv.gz"),
			Lines.readResource(prefix + "sample_50.csv.gz"),
			Lines.readResource(prefix + "sample_75.csv.gz")
		};
		String[] names = {"sample_of_1000", "quantile_25", "median",
			"quantile_75"};
		Parser p = new Parser(dirs);
		Results results = new Results(dirs);

		System.out.println("Just-in-time compilation.");
		jit();

		for (int index = 0; index < names.length; index++) {
			System.out.println("Measuring " + names[index]);
			String[] lines = datasets[index];
			String[] codes = new String[lines.length];
			for (int i = 0; i < codes.length; i++) {
				codes[i] = lines[i].trim().substring(0, 4);
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

		Timer timer = new Timer();
		timer.start();
		p.parseMmtfToBiojava("3j3q");
		timer.stop();
		results.add("largest_mmtf_3j3q", timer.get(), "ms");

		timer = new Timer();
		timer.start();
		p.parseCifToBiojava("3j3q");
		timer.stop();
		results.add("largest_cif_3j3q", timer.get(), "ms");

		results.end();

	}
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

	public void run(Set<String> flags) throws IOException {
		if (flags.contains("full")) {
			System.out.println("Measuring parsing time on the whole PDB, this "
				+ "can take about 9 hours without time to download files "
				+ "(files are downloaded only if optional parameter "
				+ "\"download\" is provided).");
			if (flags.contains("download")) {
				System.out.println("Starting to download the whole PDB in MMTF,"
					+ "PDB and mmCIF file formats, total size is about 80 GB.");
				//downloadWholeDatabase();
				transformHsf();
			}
			benchmarkWholeDatabase();
		} else if (flags.contains("generate_samples")) {
			// call program with parameters "full" and "download" before
			System.out.println("Generating samples of PDB codes.");
			DatasetGenerator d = new DatasetGenerator(dirs);
			d.generateSample(1000);
			QuantileSamples qs = new QuantileSamples(dirs);
			qs.generateDatasets(100);
		} else {
			benchmarkSamples();
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("Please provide a path to the directory where"
				+ "the data should be stored.");
			System.exit(1);
		}
		Benchmark b = new Benchmark(args[0]);
		Set<String> flags = new HashSet<>();
		for (int i = 1; i < args.length; i++) {
			flags.add(args[i].toLowerCase());
		}
		b.run(flags);
	}

}
