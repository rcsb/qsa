package analysis;

import global.io.Directories;
import global.io.LineFile;
import java.io.File;
import java.util.StringTokenizer;

/**
 *
 * @author Antonin Pavelka
 *
 */
public class CsvMerger {

	Directories dirs;

	public CsvMerger(Directories dirs) {
		this.dirs = dirs;
	}

	public void print() {
		double avg = 0;
		int counter = 0;
		LineFile summary = new LineFile(dirs.getSummaryTable());
		for (File task : dirs.getJob().listFiles()) {
			if (task.isDirectory()) {
				File t = task.toPath().resolve("table.csv").toFile();
				LineFile table = new LineFile(t);
				for (String line : table.readLines()) {
					summary.writeLine(line);
					StringTokenizer st = new StringTokenizer(line, ",");
					st.nextToken();
					st.nextToken();
					String a = st.nextToken();
					String b = st.nextToken();
					double tm = Double.parseDouble(st.nextToken());
					System.out.println(a + " " + b + " tm = " + tm + (tm < 0.5 ? "*" : " "));
					avg += tm;
					counter++;
				}
			}
		}
		avg /= counter;
		summary.writeLine(avg + "");
		System.out.println("avergage tm " + avg);
	}
}
