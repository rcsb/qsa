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
import java.nio.file.Files;
import java.nio.file.Path;
import util.Pair;

/**
 * home/job/task
 */
public class Directories {

	private File job;
	private String pdbCode = "";
	private int counterX = 1;
	private int counterY = 1;
	private Random random = new Random();
	private int id = random.nextInt(1000000);
	private File task;
	private File structures;
	private File home;

	public Directories(File home) {
		this.home = home;
	}

	public void createDirs(Path p) {
		try {
			if (!Files.exists(p)) {
				Files.createDirectories(p);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	//public File getTask() {
	//	return job;
	//}
	public File getHome() {
		return home;
	}

	public Path getRoot() {
		return home.toPath();
	}

	public File getTask() {
		System.out.println(task);
		return task;
	}

	public File getJob() {
		return job;
	}

	public void createJob() {
		job = createNextUniqueDir(getHome(), "job", "");
		System.out.println(job);
	}

	public void createTask() {
		System.out.println(task + " !!!");
		task = createNextUniqueDir(getJob(), "out", "");
		System.out.println(task);
	}

	private File createNextUniqueDir(File dir, String prefix, String nameStart) {
		int max = 0;
		for (File f : dir.listFiles()) {
			if (f.getName().startsWith(prefix)) {
				StringTokenizer st = new StringTokenizer(f.getName(), "_");
				if (!st.hasMoreTokens()) {
					continue;
				}
				st.nextToken();
				if (!st.hasMoreTokens()) {
					continue;
				}
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
		return FileOperations.safeSubdir(dir, nameStart + prefix + "_" + (max + 1));
	}

	public void setStructures(String structuresDirName) {
		structures = getTask().toPath().resolve(structuresDirName).toFile();
	}

	public File getStructures() {
		return structures;
	}

	public File getPyFile() {
		return FileOperations.safeSub(getTask(), "alignments.py");
	}

	public File getWordConnections(String pdbCode) {
		return FileOperations.safeSub(getTask(), pdbCode + ".pdb");
	}

	public File getResultsFile() {
		return FileOperations.safeSub(getTask(), "results.txt");
	}

	public File getTableFile() {
		return FileOperations.safeSub(getTask(), "table.csv");
	}

	public File getCathS20() {
		return FileOperations.safeSub(getTask(), "cath-dataset-nonredundant-S20.list.txt");
	}

	public File getPdbClusters50() {
		return FileOperations.safeSubfile(getTask(), "bc-50.out.txt");
	}

	/**
	 * @deprecated
	 */
	public File getWordDataset() {
		return FileOperations.safeSubfile(getTask(), "word_dataset");
	}

	public File getWordDatabase() {
		return FileOperations.safeSubfile(getTask(), "word_database");
	}

	public File getBiwordIndex() {
		return FileOperations.safeSubfile(getTask(), "biword_index");
	}

	public File getBiwordPair() {
		return FileOperations.safeSubfile(getTask(), "biword_pair.pdb");
	}

	public File getBiwordDataset() {
		return FileOperations.safeSubfile(getTask(), "biword_dataset");
	}

	public File getBiwordSpace() {
		return FileOperations.safeSubfile(getTask(), "biword_space.cryo");
	}

	public File getBiwordDatasetShuffled() {
		return FileOperations.safeSubfile(getTask(), "biword_dataset_shuffled");
	}

	public File getWordDatasetShuffled() {
		return FileOperations.safeSubfile(getTask(), "word_dataset_shuffled");
	}

	public File getRealVsVector() {
		File f = null;
		int i = 0;
		while (f == null || f.exists()) {
			f = FileOperations.safeSubfile(getTask(), "real_vector_" + i + ".csv");
			i++;
		}
		return f;
	}

	public File getWordRepresentants(double threshold) {
		return FileOperations.safeSubfile(getTask(), "word_clusters_" + threshold);
	}

	public File getWordRepresentants(String threshold) {
		return FileOperations.safeSubfile(getTask(), "word_clusters_" + threshold);
	}

	public File getBiwordRepresentants(double threshold) {
		return FileOperations.safeSubfile(getTask(), "biword_clusters_" + threshold);
	}

	public File getBiwordRepresentants(String threshold) {
		return FileOperations.safeSubfile(getTask(), "biword_clusters_" + threshold);
	}

	public File getPdbFold(int i) {
		return FileOperations.safeSubfile(getTask(), "pdb_fold_" + i + ".txt");
	}

	public File getVectorFold(int i) {
		return FileOperations.safeSubfile(getTask(), "vector_fold_" + i + ".arff");
	}

	public File getTmBenchmark() {
		return FileOperations.safeSub(getTask(), "tm_benchmark.txt");
	}

	public File getPdbBenchmark() {
		return FileOperations.safeSub(getTask(), "entries.idx");
	}

	private Path getMmtf() {
		Path p = getRoot().resolve("mmtf");
		createDirs(p);
		return p;
	}

	public File getBiwordHits(int structureId) {
		Path p = getBiwordHitsDir();
		createDirs(p);
		return p.resolve(Integer.toString(structureId)).toFile();
	}

	public Path getBiwordHitsDir() {
		Path p = getTask().toPath().resolve("biword_hits");
		return p;
	}

	public File getBiwordsFile(int structureId) {
		Path p = getBiwordsDir();
		createDirs(p);
		return p.resolve(Integer.toString(structureId)).toFile();
	}

	public Path getBiwordsDir() {
		Path p = getTask().toPath().resolve("biwords");
		return p;
	}

	public Path getMmtf(String code) {
		return getMmtf().resolve(code + ".mmtf.gz");
	}

	public Path getPdb() {
		Path p = getRoot().resolve("pdb");
		createDirs(p);
		return p;
	}

	public Path getPdb(String code) {
		return getPdb().resolve(code + ".pdb.gz");
	}

	public File getPdbFasta() {
		return FileOperations.safeSub(getTask(), "pdb_seqres.txt");
	}

	public Path getPairs() {
		return FileOperations.safeSub(getTask(), "pairs.csv").toPath();
	}

	public File getTopologyIndependentPairs() {
		return FileOperations.safeSub(getTask(), "89_similar_structure_diff_topo.txt");
	}

	public File getHomstradPairs() {
		return FileOperations.safeSub(getTask(), "9537_pair_wise_HOMSTRAD.txt");
	}

	public File getFailedPairs() {
		return FileOperations.safeSub(getTask(), "fails.txt");
	}

	public File getCustomPairs() {
		return FileOperations.safeSub(getTask(), "pairs.txt");
	}

	public List<String> loadBatch() {
		List<String> batch = new ArrayList<>();
		File f = FileOperations.safeSub(getTask(), "batch.txt");
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

	public File getTemp() {
		return FileOperations.safeSub(getTask(), "temp");
	}

	public void setPdbCode(String pc) {
		this.pdbCode = pc;
	}

	public File getClustersTxt() {
		String name = pdbCode + "clusters.txt";
		return FileOperations.safeSub(getTask(), name);
	}

	public File getClustersPy() {
		String name = pdbCode + "clusters.py";
		return FileOperations.safeSub(getTask(), name);
	}

	public File getClusterPdb(int i) {
		String name = pdbCode + "cluster_" + i + ".pdb";
		return FileOperations.safeSub(getTask(), name);
	}

	public File getClustersPng() {
		String name = pdbCode + "clusters.png";
		return FileOperations.safeSub(getTask(), name);
	}

	public File getCluster() {
		return FileOperations.safeSubdir(getTask(), "fragment_clusters");
	}

	public File getCluster(int id) {
		return FileOperations.safeSubfile(getCluster(), id + ".pdb");
	}

	public File getCompactPdb() {
		String name = pdbCode + "compact.pdb";
		return FileOperations.safeSub(getTask(), name);
	}

	public File getAlignmentResults() {
		return FileOperations.safeSub(getTask(), "alignment_results");
	}

	public File getVisDir() {
		return FileOperations.safeSub(getTask(), "vis");
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

	public String getAligned(String name) {
		return FileOperations.safeSub(getAlignedPdbsDir(), name).
			getAbsolutePath().replace("\\", "/");
	}

	public String[] getNames(String name) {
		String[] names = new String[2];
		names[0] = name + "A";
		names[1] = name + "B";
		return names;
	}

	public String getFinalLines(String name) {
		return FileOperations.safeSub(getAlignedPdbsDir(), name + "_Fin.pdb").
			getAbsolutePath().replace("\\", "/");
	}

	public String getWordLines(String name) {
		return FileOperations.safeSub(getAlignedPdbsDir(), name + "_Word.pdb").
			getAbsolutePath().replace("\\", "/");
	}

	public String getInitialLines(String name) {
		return FileOperations.safeSub(getAlignedPdbsDir(), name + "_Ini.pdb").
			getAbsolutePath().replace("\\", "/");
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

	public File getLogicalProgram() {
		return FileOperations.safeSubfile(getTask(), "coords.lp");
	}

	private Path getClickInputDir(Pair<String> pair) throws IOException {
		Path dir = getTask().toPath().resolve("click_input").resolve(pair.x + "-" + pair.y);
		if (!Files.exists(dir)) {
			Files.createDirectories(dir);
		}
		return dir;
	}

	public Path getClickInput(Pair<String> pair, String id) throws IOException {
		return getClickInputDir(pair).resolve(id + ".pdb");
	}

	public Path getClickOutputDir() {
		System.err.println("WARNING, using click_input instead of output");
		Path dir = getTask().toPath().resolve("click_input");
		return dir;
	}

	public Path getClickOutput(Pair<String> pair, String a, String b) throws IOException {
		return getClickOutputDir().resolve(pair.x + "-" + pair.y).resolve(a + "-" + b + ".1.pdb");
	}

	public File x() {
		String n = Integer.toString(counterX);
		counterX++;
		return FileOperations.safeSubfile(getTask(), "x_" + n + ".pdb");
	}

	public File y() {
		String n = Integer.toString(counterY);
		counterY++;
		return FileOperations.safeSubfile(getTask(), "y_" + n + ".pdb");
	}

	private String getScoreFilename() {
		return "scores_" + id + ".txt";
	}

	public void print(double[] ds) {
		try {
			File f = FileOperations.safeSubfile(getTask(), getScoreFilename());
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
			File f = FileOperations.safeSubfile(getTask(), getScoreFilename());
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
		return FileOperations.safeSubfile(getTask(), "distances.csv");
	}

}
