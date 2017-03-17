package org.rcsb.mmtf.benchmark;

import io.Directories;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Antonin Pavelka
 */
public class Results {

	Directories dirs;

	public Results(Directories dirs) {
		this.dirs = dirs;
	}

	public void add(String name, long time, String unit) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(
			new FileWriter(dirs.getResults(), true))) {
			bw.write(name + " " + time + " " + unit + "\n");
		}
	}
}
