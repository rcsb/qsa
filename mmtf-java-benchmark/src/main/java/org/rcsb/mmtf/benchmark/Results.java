package org.rcsb.mmtf.benchmark;

import org.rcsb.mmtf.benchmark.io.Directories;
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

	public void end() throws IOException {
		try (BufferedWriter bw = new BufferedWriter(
			new FileWriter(dirs.getResults(), true))) {
			for (int i = 0; i < 80; i++) {
				bw.write("-");
			}
			bw.write("\n");
		}
	}
}
