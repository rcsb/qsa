package analysis.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Antonin Pavelka
 */
public class Csv {

	private final File file;
	private final char SEPARATOR = ',';

	public Csv(File file) {
		this.file = file;
	}

	public void save2d(Distribution2d... distributions) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			int distributionSize = distributions[0].size();
			for (int i = 0; i < distributionSize; i++) {
				for (Distribution2d d : distributions) {
					bw.write(Double.toString(d.getX(i)));
					bw.write(SEPARATOR);
					bw.write(Double.toString(d.getY(i)));
					bw.write(SEPARATOR);
				}
				bw.write("\n");
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
