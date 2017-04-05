package org.rcsb.mmtf.benchmark;

import org.rcsb.mmtf.benchmark.io.Directories;
import org.rcsb.mmtf.benchmark.io.LineFile;
import org.rcsb.mmtf.benchmark.io.PdbCodeDates;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Random;
import org.rcsb.mmtf.benchmark.io.MmtfFileUtils;

/**
 * The class allows to download and prepare the lists of PDB files and PDB entries in MMTF, PDB and
 * mmCIF file format.
 */
public class DatasetGenerator {

	private final Directories dirs;
	private final List<String> codes;
	public static final String BEFORE = "2016-12-01";

	public DatasetGenerator(Directories dirs) {
		this.dirs = dirs;
		try {
			codes = PdbCodeDates.getCodesBefore(BEFORE);
			savePdbCodes();
		} catch (IOException | ParseException ex) {
			throw new RuntimeException(ex);
		}
	}

	private final void savePdbCodes() throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(
			dirs.getPdbCodes()))) {
			for (String code : codes) {
				bw.write(code + "\n");
			}
		}
	}

	public List<String> getCodes() {
		return codes;
	}

	public void downloadMmtf() {
		Counter c = new Counter("mmtf download", 10, codes.size());
		for (String code : codes) {
			try {
				Path p = dirs.getMmtfPath(code);
				MmtfFileUtils.downloadMmtf(code, p);
				c.next();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void downloadMmtf(String[] selectedCodes) {
		Counter c = new Counter("mmtf selected download", 10, selectedCodes.length);
		for (String code : selectedCodes) {
			try {
				Path p = dirs.getMmtfPath(code);
				MmtfFileUtils.downloadMmtf(code, p);
				c.next();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void downloadMmtfReduced(String[] selectedCodes) {
		Counter c = new Counter("mmtf reduced selected download", 10, selectedCodes.length);
		for (String code : selectedCodes) {
			try {
				Path p = dirs.getMmtfReducedPath(code);
				MmtfFileUtils.downloadMmtfReduced(code, p);
				c.next();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void downloadPdb() {
		Counter c = new Counter("pdb download", 10, codes.size());
		for (String code : codes) {
			try {
				Path p = dirs.getPdbPath(code);
				if (Files.notExists(p)) {
					MmtfFileUtils.downloadPdb(code, p);
				}
				c.next();
			} catch (Exception ex) {
				System.out.println("PDB file " + code + " not downloaded, probably because this "
					+ "format is not supported for this record.");
			}
		}
	}

	public void downloadCif() {
		Counter c = new Counter("cif download", 10, codes.size());
		for (String code : codes) {
			try {
				Path p = dirs.getCifPath(code);
				if (Files.notExists(p)) {
					MmtfFileUtils.downloadCif(code, p);
				}
				c.next();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void downloadHadoopSequenceFiles() {
		try {
			String msg = "Downloading the Hadoop sequence file in ";
			System.out.println(msg + "full representation...");
			MmtfFileUtils.downloadRobust(
				"http://mmtf.rcsb.org/v1.0/hadoopfiles/full.tar",
				dirs.getHsfFullOriginal());
			System.out.println(msg + "reduced representation...");
			MmtfFileUtils.downloadRobust(
				"http://mmtf.rcsb.org/v1.0/hadoopfiles/reduced.tar",
				dirs.getHsfReducedOriginal());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generates a random subsample of {@code n} PDB codes for which all three file format
	 * representations exists. Also downloads the respective files.
	 */
	public void generateSample(int n) throws IOException {
		DatasetGenerator d = new DatasetGenerator(dirs);
		dirs.getSample1000().delete();
		LineFile lf = new LineFile(dirs.getSample1000());
		String[] sample = new String[n];
		int index = 0;
		Random random = new Random(1);
		List<String> fails = new ArrayList<>();
		Counter counter = new Counter("all formats sample download", 10, codes.size());
		while (index < n) {
			int r = random.nextInt(codes.size());
			String code = codes.get(r);
			if (d.downloadAllFormats(code)) {
				sample[index++] = code;
				codes.remove(r);
			}
			counter.next();
		}
		if (fails.size() > 0) {
			for (String s : fails) {
				System.out.println("File " + s + "not available in all formats,"
					+ " skipping.");
			}
		}
		for (int i = 0; i < sample.length; i++) {
			lf.println(sample[i]);
		}
	}

	public void downloadSelected(String[] selectedCodes) throws IOException {
		for (String code : selectedCodes) {
			MmtfFileUtils.downloadMmtf(code, dirs.getMmtfPath(code));
			try {
				MmtfFileUtils.downloadPdb(code, dirs.getPdbPath(code));
			} catch (Exception ex) {
				System.out.println("PDB file " + code + " does not exist (this is correct if the "
					+ "file is large).");
			}
			MmtfFileUtils.downloadCif(code, dirs.getCifPath(code));
			MmtfFileUtils.downloadMmtfReduced(code, dirs.getMmtfReducedPath(code));
		}
	}

	public void downloadAllFormats(String[] codes) {
		for (String code : codes) {
			downloadAllFormats(code);
		}
	}

	/**
	 * @param code PDB code
	 * @return true if the PDB entry exists in all three formats and their download was successful.
	 */
	public boolean downloadAllFormats(String code) {
		boolean available = true;
		try {
			MmtfFileUtils.downloadMmtf(code, dirs.getMmtfPath(code));
		} catch (Exception ex) {
			available = false;
		}
		try {
			MmtfFileUtils.downloadPdb(code, dirs.getPdbPath(code));
		} catch (Exception ex) {
			available = false;
		}
		try {
			MmtfFileUtils.downloadCif(code, dirs.getCifPath(code));
		} catch (Exception ex) {
			available = false;
		}
		return available;
	}

}
