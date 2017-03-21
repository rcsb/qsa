package org.rcsb.mmtf.benchmark;

import io.Directories;
import io.PdbCodeDates;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Random;
import profiling.ProfilingFileUtils;

public class Downloader {

	private final Directories dirs;
	private final List<String> codes;

	public Downloader(Directories dirs, String beforeDate) {
		this.dirs = dirs;
		try {
			codes = PdbCodeDates.getCodesBefore(beforeDate);
		} catch (IOException | ParseException ex) {
			throw new RuntimeException(ex);
		}
	}

	public List<String> getCodes() {
		return codes;
	}

	public void downloadMmtf() {
		Counter c = new Counter();
		for (String code : codes) {
			try {
				Path p = dirs.getMmtfPath(code);
				ProfilingFileUtils.downloadMmtf(code, p);
				c.next();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void downloadPdb() {
		Counter c = new Counter();
		for (String code : codes) {
			try {
				Path p = dirs.getPdbPath(code);
				if (Files.notExists(p)) {
					ProfilingFileUtils.downloadPdb(code, p);
				}
				c.next();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void downloadCif() {
		Counter c = new Counter();
		for (String code : codes) {
			try {
				Path p = dirs.getCifPath(code);
				if (Files.notExists(p)) {
					ProfilingFileUtils.downloadCif(code, p);
				}
				c.next();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void downloadHadoopSequenceFile() {
		try {
			Path p = dirs.getSub("hsf");
			if (Files.notExists(p)) {
				ProfilingFileUtils.download(
					"http://mmtf.rcsb.org/v1.0/hadoopfiles/full.tar", p);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void downloadSample(int n) {
		String[] sample = new String[n];
		int index = 0;
		Random random = new Random(1);
		List<String> fails = new ArrayList<>();
		while (index < n) {
			int r = random.nextInt(codes.size());
			String code = codes.get(r);
			boolean fail = false;

			try {
				ProfilingFileUtils.downloadMmtf(code, dirs.getMmtfPath(code));
			} catch (Exception ex) {
				fail = true;
				fails.add(code + " cif");
			}
			try {
				ProfilingFileUtils.downloadPdb(code, dirs.getPdbPath(code));
			} catch (Exception ex) {
				fail = true;
				fails.add(code + " cif");
			}
			try {
				ProfilingFileUtils.downloadCif(code, dirs.getCifPath(code));
			} catch (Exception ex) {
				fail = true;
				fails.add(code + " cif");
			}

			if (!fail) {
				sample[index++] = code;
				codes.remove(r);
			}
		}
		System.out.println("SAMPLE START");
		for (int i = 0; i < sample.length; i++) {
			System.out.println(sample[i]);
		}
		System.out.println("SAMPLE END");
		
		if (fails.size() > 0) {
			for (String s : fails) {
				System.out.println("FAIL: " + s);
			}
		}
	}

	public Path getResource(String p) throws IOException {
		File f = new File(getClass().getResource(p).getFile());
		return Paths.get(f.getAbsolutePath());
	}

}
