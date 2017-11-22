package global;

import global.io.Directories;
import java.io.File;

/**
 *
 * @author kepler
 */
public class TestVariables {

	private final Directories directories;
	private final Parameters parameters;

	public TestVariables() {
		File file = new File("e:/data/qsa");
		if (!file.exists()) {
			file = new File("data");
		}
		System.out.println("Using test directory " + file.getAbsolutePath());
		directories = new Directories(file);
		parameters = Parameters.create(directories.getParameters());
	}

	public Directories getDirectoris() {
		return directories;
	}

	public Parameters getParameters() {
		return parameters;
	}
}
