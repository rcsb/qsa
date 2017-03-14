package org.rcsb.mmtf.benchmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Antonin Pavelka
 *
 */
public class ResultWriter {

	private File f;
	private int height; // max height of column
	private Map<String, Long> results = new HashMap<>();

	public ResultWriter(File f) {
		this.f = f;
	}

	public void addStructure(String code, long time) {
		results.put(code, time);
	}

	public void save() throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for (String code : results.keySet()) {
				bw.write(code + " " + results.get(code) + "\n");
			}
		}
	}

}
