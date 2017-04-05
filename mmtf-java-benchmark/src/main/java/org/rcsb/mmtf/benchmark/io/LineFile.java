package org.rcsb.mmtf.benchmark.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LineFile {

	private final File file;

	public LineFile(File f) {
		this.file = f;
	}

	public List<String> readLines() throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			List<String> lines = new ArrayList<>();
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			return lines;
		}
	}

	public void println(String line) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(
			new FileWriter(file, true))) {
			bw.write(line + "\n");
		}
	}

}
