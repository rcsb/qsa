package org.rcsb.mmtf.benchmark;

import io.Directories;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import io.PdbEntries;
import java.text.ParseException;
import profiling.ProfilingFileUtils;

public class Downloader {

	private Directories dirs;
	private PdbEntries entries;

	public Downloader(Directories dirs, String beforeDate) {
		try {
			this.dirs = dirs;
			Path p = getResource("entries.txt.zip");
			entries = new PdbEntries(p.toFile(), beforeDate);
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public void downloadMmtf() {
		Counter c = new Counter();
		for (String code : getCodes()) {
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
		for (String code : getCodes()) {
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
		for (String code : getCodes()) {
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
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Path getResource(String p) throws IOException {
		File f = new File(getClass().getResource(p).getFile());
		return Paths.get(f.getAbsolutePath());
	}

	public List<String> getCodes() {
		return entries.getCodes();
	}

}
