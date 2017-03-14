package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ZippedTextFile {

	public static List<String> readLines(File f) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
			new GZIPInputStream(new FileInputStream(f))))) {
			List<String> lines = new ArrayList<>();
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			return lines;
		}
	}

	public static void writeLines(List<String> lines, File f)
		throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
			new GZIPOutputStream(new FileOutputStream(f))))) {
			for (String s : lines) {
				bw.write(s + "\n");
			}
		}
	}
}
