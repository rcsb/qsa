package io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import profiling.ProfilingFileUtils;

public class Directories {

	private File home;

	/**
	 * Specify home directory.
	 */
	public Directories(File home) {
		this.home = home;
		System.out.println("Using directory " + home.getAbsolutePath());
		if (home.exists()) {
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

	public File getAverages() {
		return new File(home + "/averages.csv");
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

	public String getHadoopSequenceFile() {
		return getHome() + "/hadoop/full";
	}

	public String getHadoopSequenceFileUnzipped() {
		return getHome() + "/hadoop/full_unzipped";
	}

	public File getHadoopSequenceFileDir() {
		return new File(getHome() + "/hadoop");
	}

	public Path getMmtfPath(String code) {
		try {
			Path dir = getHome().toPath().resolve("mmtf/" + code.substring(1, 3));
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
						ProfilingFileUtils.unzip(fin, fout);
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

	public File getPdbEntries() {
		return getHome().toPath().resolve(Paths.get("pdb_entry_type.txt")).toFile();
	}

}
