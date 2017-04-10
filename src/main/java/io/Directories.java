package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import fragments.FlexibleLogger;

public class Directories {

	private File home;
	private String pdbCode = "";
	private int counterX = 1;
	private int counterY = 1;
	private Random random = new Random();
	private int id = random.nextInt(1000000);
	private static File out = null;

	public Directories(File home) {
		this.home = home;
	}

	public static Directories createDefault() {
		//return new Directories(new File("/Users/antonin/data/qsa"));
		return new Directories(new File("c:/kepler/data/qsa"));
	}

	public File getHome() {
		return home;
	}

	public File getOut() {
		if (out == null) {
			int max = 0;
			for (File f : getHome().listFiles()) {
				if (f.isDirectory() && f.getName().startsWith("out")) {
					StringTokenizer st = new StringTokenizer(f.getName(), "_");
					st.nextToken();
					String s = st.nextToken();
					int i;
					try {
						i = Integer.parseInt(s);
						if (i > max) {
							max = i;
						}
					} catch (NumberFormatException e) {
					}

				}
			}
			out = FileOperations.safeSubdir(getHome(), "out_" + (max + 1));
		}
		return out;
	}

	public File getPyFile() {
		return FileOperations.safeSub(getOut(), "alignments.py");
	}

	public File getResultsFile() {
		return FileOperations.safeSub(getOut(), "results.txt");
	}

	public File getTableFile() {
		return FileOperations.safeSub(getOut(), "table.csv");
	}

	public File getTmBenchmark() {
		return FileOperations.safeSub(getHome(), "tm_benchmark.txt");
	}

	public File getPdbBenchmark() {
		return FileOperations.safeSub(getHome(), "entries.idx");
	}

	public File getMmtf() {
		return FileOperations.safeSub(getHome(), "mmtf");
	}

	public File getPdb() {
		return FileOperations.safeSub(getHome(), "pdb");
	}

	public File getPdbFasta() {
		return FileOperations.safeSub(getHome(), "pdb_seqres.txt");
	}

	public File getPairs() {
		return FileOperations.safeSub(getHome(), "pdb_pairs.txt");
	}

	public File getTopologyIndependentPairs() {
		return FileOperations.safeSub(getHome(), "89_similar_structure_diff_topo.txt");
	}

	public File getHomstradPairs() {
		return FileOperations.safeSub(getHome(), "9537_pair_wise_HOMSTRAD.txt");
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

	public File getPdbEntryTypes() {
		return FileOperations.safeSubfile(getHome(), "pdb_entry_type.txt");
	}

	public File getPdbFile(String fn) {
		return FileOperations.safeSubfile(getPdb(), fn);
	}

	public File getTemp() {
		return FileOperations.safeSub(getOut(), "temp");
	}

	public File getTemp(String name) {
		return FileOperations.safeSub(getTemp(), name);
	}

	public File getMatrixTest() {
		return FileOperations.safeSub(getOut(), "test_matrix");
	}

	public File getMatrixTest(String name) {
		return FileOperations.safeSubfile(getMatrixTest(), name);
	}

	public void setPdbCode(String pc) {
		this.pdbCode = pc;
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

	public File getCluster() {
		return FileOperations.safeSubdir(getOut(), "fragment_clusters");
	}

	public File getCluster(int id) {
		return FileOperations.safeSubfile(getCluster(), id + ".pdb");
	}

	public File getCompactPdb() {
		String name = pdbCode + "compact.pdb";
		return FileOperations.safeSub(getOut(), name);
	}

	public File getAlignmentResults() {
		return FileOperations.safeSub(getOut(), "alignment_results");
	}

	public File getVisDir() {
		return FileOperations.safeSub(getOut(), "vis");
	}

	public File getAlignedPdbs() {
		return FileOperations.safeSubfile(getVisDir(), "aligned_pdbs.txt");
	}

	public File getVisPdb() {
		return FileOperations.safeSub(getVisDir(), "v.pdb");
	}

	public File getVis(String id) {
		return FileOperations.safeSub(getVisDir(), id + ".pdb");
	}

	public File getAlignedPdbsDir() {
		return FileOperations.safeSub(getVisDir(), "aligned_pdbs");
	}

	public File getAlignedA(String name) {
		return FileOperations.safeSub(getAlignedPdbsDir(), "aln_" + name + "_a.pdb");
	}

	public File getAlignedB(String name) {
		return FileOperations.safeSub(getAlignedPdbsDir(), "aln_" + name + "_b.pdb");
	}

	public File getVisPy() {
		return FileOperations.safeSub(getVisDir(), "v.py");
	}

	public File getLauncher() {
		return FileOperations.safeSub(getVisDir(), "launcher.py");
	}

	public File getFragmentPairSelections() {
		return FileOperations.safeSub(getVisDir(), "afps.py");
	}

	public File getFatcatResults() {
		return FileOperations.safeSubfile(getAlignmentResults(), "fatcat.results");
	}

	public File getFragmentsResults() {
		return FileOperations.safeSubfile(getAlignmentResults(), "fragments.results");
	}

	public File getAlignmentObjects() {
		return FileOperations.safeSubfile(getAlignmentResults(), "alignemnt.cryo");
	}

	public File getAlignmentCsv() {
		return FileOperations.safeSubfile(getAlignmentResults(), "alignment.csv");
	}

	public File getAlignmentCsvBackup() {
		return FileOperations.safeSubfile(getAlignmentResults(), "alignment_backup.csv");
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

	public File getDistances() {
		return FileOperations.safeSubfile(getHome(), "distances.csv");
	}

}
