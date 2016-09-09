package io;

import fragments.FlexibleLogger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import spark.Dataset;

public class Directories {

	private File home;
	private String pdbCode = "";
	private int counterX = 1;
	private int counterY = 1;
	private Random random = new Random();
	private int id = random.nextInt(1000000);

	public Directories(File home) {
		this.home = home;
	}

	public File getHome() {
		return home;
	}

	public File getTmBenchmark() {
		return FileOperations.safeSub(getHome(), "tm_benchmark.txt");
	}

	public File getMmtf() {
		return FileOperations.safeSub(getHome(), "mmtf");
	}

	public File getPdb() {
		return FileOperations.safeSub(getHome(), "pdb");
	}

	public File getPdbCodes() {
		return FileOperations.safeSubfile(getPdb(), "pdb_codes.txt");
	}

	public File getPdbFile(String fn) {
		return FileOperations.safeSubfile(getPdb(), fn);
	}

	public File getTemp() {
		return FileOperations.safeSub(getHome(), "temp");
	}

	public void setPdbCode(String pc) {
		this.pdbCode = pc;
	}

	private File getOut() {
		return FileOperations.safeSub(getHome(), "out");
	}

	public File getPairDirs() {
		return FileOperations.safeSubdir(getHome(), "quarters");
	}

	public File getClustersTxt() {
		String name = pdbCode + "clusters.txt";
		return FileOperations.safeSub(getOut(), name);
	}

	public File getClustersPy() {
		String name = pdbCode + "clusters.py";
		return FileOperations.safeSub(getOut(), name);
	}

	public File getClustersPng() {
		String name = pdbCode + "clusters.png";
		return FileOperations.safeSub(getOut(), name);
	}

	public File getCompactPdb() {
		String name = pdbCode + "compact.pdb";
		return FileOperations.safeSub(getOut(), name);
	}

	public Dataset getEasyBenchmark() {
		File f = FileOperations.safeSub(getHome(), "TM_L90_111111.csv");
		return new Dataset(f);
	}

	public List<String> loadBatch() {
		List<String> batch = new ArrayList<>();
		File f = FileOperations.safeSub(getHome(), "batch.txt");
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			while ((line = br.readLine()) != null) {
				batch.add(line.trim().toUpperCase());
			}
			br.close();
		} catch (IOException ex) {
			FlexibleLogger.error("Failed to load batch file " + f.getPath(), ex);
		}
		return batch;
	}

	public File getAlignmentResults() {
		return FileOperations.safeSub(getHome(), "alignment_results");
	}

	public File getFatcatResults() {
		return FileOperations.safeSubfile(getAlignmentResults(), "fatcat");
	}

	public File x() {
		String n = Integer.toString(counterX);
		counterX++;
		return FileOperations.safeSubfile(getOut(), "x_" + n + ".pdb");
	}

	public File y() {
		String n = Integer.toString(counterY);
		counterY++;
		return FileOperations.safeSubfile(getOut(), "y_" + n + ".pdb");
	}

	private String getScoreFilename() {
		return "scores_" + id + ".txt";
	}

	public void print(double[] ds) {
		try {
			File f = FileOperations.safeSubfile(getOut(), getScoreFilename());
			BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
			for (double d : ds) {
				bw.write(d + " ");
			}
			bw.write("\n");
			bw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void print(String[] ds) {
		try {
			File f = FileOperations.safeSubfile(getOut(), getScoreFilename());
			BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
			for (String d : ds) {
				bw.write(d + " ");
			}
			bw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
