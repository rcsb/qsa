package org.rcsb.mmtf.benchmark.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.rcsb.mmtf.benchmark.FileType;

public class Directories {

	private File home;

	/**
	 * Specify home directory.
	 */
	public Directories(File home) {
		this.home = home;
		System.out.println("Using directory " + home.getAbsolutePath());
		if (!home.exists()) {
			home.mkdir();
		}
	}

	/**
	 * Use default directory.
	 */
	public static Directories createDefault() {
		File f = new File("mmtf-benchmark");
		return new Directories(f);
	}

	public File getHome() {
		return home;
	}

	public Path getSub(String name) {
		return getHome().toPath().resolve(name);
	}

	public File getResults() {
		return new File(home + "/results.csv");
	}

	public void prepareBatch(String code, FileType fileType, String batchName) throws IOException {
		Path src = getStructurePath(code, fileType);
		Path destDir = getHome().toPath().resolve("batch_" + batchName + "_" + fileType.toString());
		if (!Files.exists(destDir)) {
			Files.createDirectory(destDir);
		}
		Path dest = destDir.resolve(src.getFileName());
		if (!Files.exists(dest)) {
			Files.copy(src, dest);
		}
	}

	/**
	 * All PDB codes that will be used during benchmarking, but the file is just generated, never
	 * read by benchmark.
	 */
	public File getPdbCodes() {
		return new File(home + "/pdb_codes.txt");
	}

	public File getPdbSizes() {
		return new File(home + "/pdb_sizes.csv");
	}

	public File getAverages() {
		return new File(home + "/averages.csv");
	}

	public File getSample25() {
		return new File(home + "/sample_25.csv");
	}

	public File getSample50() {
		return new File(home + "/sample_50.csv");
	}

	public File getSample75() {
		return new File(home + "/sample_75.csv");
	}

	public File getSampleSmallest() {
		return new File(home + "/sample_smallest.csv");
	}

	public File getSample1000() {
		return new File(home + "/sample_1000");
	}

	public File getMmtf() {
		return FileOperations.safeSub(getHome(), "mmtf");
	}

	public File getMmtfFlat() {
		return FileOperations.safeSub(getHome(), "mmtf_flat");
	}

	public File getMmtfUnzipped() {
		return FileOperations.safeSub(getHome(), "mmtf_unzipped");
	}

	public File getPdb() {
		return FileOperations.safeSub(getHome(), "pdb");
	}

	public File getPdbUnzipped() {
		return FileOperations.safeSub(getHome(), "pdb_unzipped");
	}

	public Path getHsfFullOriginal() {
		return getHome().toPath().resolve("full.tar");
	}

	public Path getHsfReducedOriginal() {
		return getHome().toPath().resolve("reduced.tar");
	}

	public Path getHsfFullOriginalUntared() {
		return getHome().toPath().resolve("full_original");
	}

	public Path getHsfReducedOriginalUntared() {
		return getHome().toPath().resolve("reduced_original");
	}

	public Path getHsfFull() {
		return getHome().toPath().resolve("full_dir");
	}

	public Path getHsfReduced() {
		return getHome().toPath().resolve("reduced_dir");
	}

	/*	public String getHadoopSequenceFile() {
		return getHome() + "/hadoop/full";
	}

	public String getHadoopSequenceFileUnzipped() {
		return getHome() + "/hadoop/full_unzipped";
	}

	public File getHadoopSequenceFileDir() {
		return new File(getHome() + "/hadoop");
	}
	 */
	public Path getMmtfPath(String code) {
		try {
			Path dir = getHome().toPath().resolve("mmtf/" + code.substring(1, 3));
			Files.createDirectories(dir);
			return dir.resolve(code + ".mmtf.gz");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Path getMmtfReducedPath(String code) {
		try {
			Path dir = getHome().toPath().resolve("mmtf_reduced/" + code.substring(1, 3));
			Files.createDirectories(dir);
			return dir.resolve(code + ".mmtf.gz");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Path getMmtfUnzippedPath(String code) {
		try {
			Path dir = getMmtfUnzipped().toPath().resolve(code.substring(1, 3));
			Files.createDirectories(dir);
			return dir.resolve(code);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Path getStructurePath(String code, FileType fileType) {
		switch (fileType) {
			case PDB:
				return getPdbPath(code);
			case MMTF:
				return getMmtfPath(code);
			case CIF:
				return getCifPath(code);
			default:
				throw new RuntimeException();
		}
	}

	public Path getPdbPath(String code) {
		try {
			Path dir = getHome().toPath().resolve("pdb/" + code.substring(1, 3));
			Files.createDirectories(dir);
			return dir.resolve(code + ".pdb.gz");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Path getCifPath(String code) {
		try {
			Path dir = getHome().toPath().resolve("cif/" + code.substring(1, 3));
			Files.createDirectories(dir);
			return dir.resolve(code + ".cif.gz");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Set<String> getStructureNames(File home) {
		Set<String> set = new HashSet<>();
		for (File d : getMmtf().listFiles()) {
			if (d.isDirectory() && d.getName().length() == 2) {
				for (File f : d.listFiles()) {
					String fn = f.getName();
					if (fn.length() == 4) {
						set.add(fn);
					} else if (fn.startsWith("pdb") && fn.endsWith(".ent.gz")) {
						set.add(fn);
					} else {
						throw new RuntimeException(fn);
					}
				}
			}
		}
		return set;
	}

	public Set<String> getMmtfAndPdb() {

		Set<String> mmtf = getStructureNames(getMmtf());
		Set<String> pdb = getStructureNames(getPdb());

		System.out.println("mmtf " + mmtf.size());
		System.out.println("pdb " + pdb.size());

		Set<String> a = new HashSet<>(mmtf);
		a.removeAll(pdb);
		System.out.println(a.size() + " mmtf - pdb");

		Set<String> b = new HashSet<>(pdb);
		b.removeAll(mmtf);
		System.out.println(b.size() + " pdb - mmtf");

		mmtf.retainAll(pdb);

		return new TreeSet<>(mmtf);
	}

	public void unzipDb(File in, File out) throws IOException {
		for (File d : in.listFiles()) {
			if (d.isDirectory() && d.getName().length() == 2) {
				for (File fin : d.listFiles()) {
					try {
						File fout = new File(
							out + "/" + d.getName() + "/" + fin.getName());
						MmtfFileUtils.unzip(fin, fout);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	public String getPdbCache() {
		return getHome().toPath().resolve("cache").toString();
	}

}
