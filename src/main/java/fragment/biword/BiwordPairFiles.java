package fragment.biword;

import global.io.Directories;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import util.FlexibleLogger;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordPairFiles {

	private List<BiwordPairReader> readers = new ArrayList<>();

	public BiwordPairFiles(Directories dirs) {
		for (File f : dirs.getBiwordHitsDir().toFile().listFiles()) {
			try {
				readers.add(new BiwordPairReader(f));
			} catch (IOException ex) {
				FlexibleLogger.error(ex);
			}
		}
	}

	public List<BiwordPairReader> getReaders() {
		return readers;
	}
}
