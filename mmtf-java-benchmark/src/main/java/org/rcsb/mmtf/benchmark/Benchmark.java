package org.rcsb.mmtf.benchmark;

import org.rcsb.mmtf.benchmark.io.Directories;
import org.rcsb.mmtf.benchmark.io.HadoopSequenceFileConverter;
import org.rcsb.mmtf.benchmark.io.LineFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.rcsb.mmtf.utils.Lines;
import org.rcsb.mmtf.benchmark.util.Timer;

/**
 *
 * Main class to run all the benchmarks.
 *
 * @author Antonin Pavelka
 */
public class Benchmark {

	private Directories dirs;

	String samplePrefix = "/mmtf-benchmark/";
	String[][] datasets;
	String[] sampleNames = {"sample_of_1000", "quantile_25", "median",
		"quantile_75"};

	private String[] selectedCodes = {"3j3q"};

	public Benchmark(String path) throws IOException {
		dirs = new Directories(new File(path));
		String[][] ds = {
			extractCodes(Lines.readResource(samplePrefix + "sample_1000.gz")),
			extractCodes(Lines.readResource(samplePrefix + "sample_25.csv.gz")),
			extractCodes(Lines.readResource(samplePrefix + "sample_50.csv.gz")),
			extractCodes(Lines.readResource(samplePrefix + "sample_75.csv.gz"))
		};
		datasets = ds;
	}

	private final String[] extractCodes(String[] lines) {
		String[] codes = new String[lines.length];
		for (int i = 0; i < lines.length; i++) {
			codes[i] = lines[i].trim().substring(0, 4);
		}
		return codes;
	}

	public void downloadHadoopSequenceFiles() {
		DatasetGenerator d = new DatasetGenerator(dirs);
		System.out.println("Downloading Hadoop sequence files:");
		Timer.start("hsf-download");
		d.downloadHadoopSequenceFiles();
		Timer.stop("hsf-download");
		Timer.print();
	}

	/**
	 * Downloads the whole PDB in MMTF, PDB and mmCIF file format.
	 */
	public void downloadWholeDatabase() {
		DatasetGenerator d = new DatasetGenerator(dirs);

		downloadHadoopSequenceFiles();

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
		if (!Files.exists(dirs.getHsfReduced())) {
			HadoopSequenceFileConverter.convert(dirs.getHsfReducedOriginal().toString(),
				dirs.getHsfReducedOriginalUntared().toString(),
				dirs.getHsfReduced().toString());
		}
		if (!Files.exists(dirs.getHsfFull())) {
			HadoopSequenceFileConverter.convert(dirs.getHsfFullOriginal().toString(),
				dirs.getHsfFullOriginalUntared().toString(),
				dirs.getHsfFull().toString());

		}
	}

	public void benchmarkHadoopSequenceFiles() throws IOException {

		transformHsf();

		Results results = new Results(dirs);
		Parser p = new Parser(dirs);
		Timer timer = new Timer();
		timer.start();
		List<String> fails = p.parseHadoop(dirs.getHsfReduced().toFile());
		timer.stop();
		results.add("hadoop_sequence_file_reduced_mmtf", timer.get(), "ms");
		System.out.print("Reduced HSF fails: ");
		for (String s : fails) {
			System.out.print(s + " ");
		}
		System.out.println();
	}

	/**
	 * Runs the benchmark on the whole PDB measuring total time of parsing Hadoop sequence file
	 * (unzipped) and the times for entries in individual MMTF, PDB and mmCIF files.
	 */
	public void benchmarkWholeDatabase(boolean onlyMmtf) throws IOException {
		Timer timer;
		Counter counter;
		Parser p = new Parser(dirs);
		DatasetGenerator d = new DatasetGenerator(dirs);
		List<String> codes = d.getCodes();
		Results results = new Results(dirs);

		jit();

		benchmarkHadoopSequenceFiles();

		timer = new Timer();
		timer.start();
		List<String> fails = p.parseHadoop(dirs.getHsfReduced().toFile());
		timer.stop();
		results.add("hadoop_sequence_file_reduced_mmtf", timer.get(), "ms");
		System.out.print("Reduced HSF fails: ");
		for (String s : fails) {
			System.out.print(s + " ");
		}
		System.out.println();

		timer = new Timer();
		timer.start();
		p.parseHadoop(dirs.getHsfFull().toFile());
		timer.stop();
		results.add("hadoop_sequence_file_mmtf", timer.get(), "ms");

		counter = new Counter("all mmtf parsing ", 10, codes.size());
		timer = new Timer();
		timer.start();
		for (String c : codes) {
			p.parseMmtfToBiojava(c);
			counter.next();
		}
		timer.stop();
		results.add("all_mmtf", timer.get(), "ms");

		if (!onlyMmtf) {
			counter = new Counter("all pdb parsing ", 10, codes.size());
			timer = new Timer();
			timer.start();
			for (String c : codes) {
				p.parsePdbToBiojava(c);
				counter.next();
			}
			timer.stop();
			results.add("all_pdb", timer.get(), "ms");

			counter = new Counter("all cif parsing ", 10, codes.size());
			timer = new Timer();
			timer.start();
			for (String c : codes) {
				p.parseCifToBiojava(c);
				counter.next();
			}
			timer.stop();
			results.add("all_cif", timer.get(), "ms");
		}

		results.end();
	}

	/**
	 * Measures times of parsing of 1000 random PDB entries, then times for 100 entries of three
	 * characteristic sizes and finally the times for the largest entry.
	 */
	public void benchmarkSamples() throws IOException {
		Parser p = new Parser(dirs);
		Results results = new Results(dirs);

		System.out.println("Just-in-time compilation.");
		jit();

		for (int index = 0; index < sampleNames.length; index++) {
			System.out.println("Measuring " + sampleNames[index]);
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
			results.add(sampleNames[index] + "_mmtf", timer.get(), "ms");

			timer = new Timer();
			timer.start();
			for (String code : codes) {
				p.parsePdbToBiojava(code);
			}
			timer.stop();
			results.add(sampleNames[index] + "_pdb", timer.get(), "ms");

			timer = new Timer();
			timer.start();
			for (String code : codes) {
				p.parseCifToBiojava(code);
			}
			timer.stop();
			results.add(sampleNames[index] + "_cif", timer.get(), "ms");
		}
		System.out.println("Measuring 3j3q...");
		Timer timer;
		int rep = 100;
		for (String code : selectedCodes) {
			timer = new Timer();
			timer.start();
			for (int i = 0; i < rep; i++) {
				p.parseMmtfReducedToBiojava(code);
			}
			timer.stop();
			results.add("selected_mmtf_reduced_" + code, timer.get() / rep, "ms");

			timer = new Timer();
			timer.start();
			for (int i = 0; i < rep; i++) {
				p.parseMmtfToBiojava(code);
			}
			timer.stop();
			results.add("selected_mmtf_" + code, timer.get() / rep, "ms");

		}
		results.end();
	}

	public void benchmarkSelected() throws IOException {
		Parser p = new Parser(dirs);
		Results results = new Results(dirs);

		System.out.println("Just-in-time compilation...");
		for (int i = 0; i < 50; i++) {
			for (String code : selectedCodes) {
				p.parseMmtfReducedToBiojava(code);
				p.parseMmtfToBiojava(code);
			}
		}
		System.out.println("Measurement...");
		for (String code : selectedCodes) {
			System.gc();
			Timer timer = new Timer();
			timer.start();
			p.parseMmtfReducedToBiojava(code);
			timer.stop();
			results.add("selected_mmtf_reduced_" + code, timer.get(), "ms");

			System.gc();
			timer = new Timer();
			timer.start();
			p.parseMmtfToBiojava(code);
			timer.stop();
			results.add("selected_mmtf_" + code, timer.get(), "ms");

		}

		results.end();

	}

	private void prepareFiles(FileType fileType, File codesFile) throws IOException {
		LineFile lf = new LineFile(codesFile);
		for (String line : lf.readLines()) {
			String code = line.trim().substring(0, 4);
			dirs.prepareBatch(code, fileType, codesFile.getName());
		}
	}

	public void run(Set<String> flags) throws IOException {
		System.out.println("Total memory: " + Runtime.getRuntime().totalMemory() / Math.pow(2, 30) + " GB");
		System.out.println("Max memory: " + Runtime.getRuntime().maxMemory() / Math.pow(2, 30) + " GB");
		if (flags.contains("prepare_files_for_javascript")) { // for NGL
			for (FileType fileType : FileType.values()) {
				prepareFiles(fileType, dirs.getSample1000());
				prepareFiles(fileType, dirs.getSampleSmallest());
				prepareFiles(fileType, dirs.getSample25());
				prepareFiles(fileType, dirs.getSample50());
				prepareFiles(fileType, dirs.getSample75());
			}
		} else if (flags.contains("hsf")) {
			if (flags.contains("download")) {
				System.out.println("Downloading the whole PDB in Hadoop Sequence "
					+ "File format, it may take 30 minutes or more.");
				downloadHadoopSequenceFiles();
			}
			benchmarkHadoopSequenceFiles();
		} else if (flags.contains("full")) {
			System.out.println("Measuring parsing time on the whole PDB, this "
				+ "can take about 9 hours without time to download files "
				+ "(files are downloaded only if optional parameter "
				+ "\"download\" is provided).");
			if (flags.contains("download")) {
				System.out.println("Starting to download the whole PDB in MMTF,"
					+ "PDB and mmCIF file formats, total size is about 80 GB.");
				downloadWholeDatabase();
			}
			benchmarkWholeDatabase(flags.contains("only_mmtf"));
		} else if (flags.contains("sample")) {
			DatasetGenerator d = new DatasetGenerator(dirs);
			d.generateSample(1000);
			QuantileSamples qs = new QuantileSamples(dirs);
			qs.generateDatasets(100);
		} else if (flags.contains("large")) {
			DatasetGenerator d = new DatasetGenerator(dirs);
			d.downloadSelected(selectedCodes);
			benchmarkSelected();
		} else {
			DatasetGenerator d = new DatasetGenerator(dirs);
			d.downloadAllFormats(Lines.readResource(samplePrefix + "jit.gz"));
			d.downloadSelected(selectedCodes);
			for (int i = 0; i < datasets.length; i++) {
				d.downloadSelected(datasets[i]);
			}
			benchmarkSamples();
		}
	}

	/**
	 * Does some parsing before measurements, so that the first measurement is not at disadvantage
	 * due to Just In Time compilation.
	 */
	private void jit() throws IOException {
		Parser p = new Parser(dirs);
		String[] codes = extractCodes(Lines.readResource(samplePrefix + "jit.gz"));
		for (String code : codes) {
			try {
				p.parseMmtfToBiojava(code);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				p.parsePdbToBiojava(code);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				p.parseCifToBiojava(code);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
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
