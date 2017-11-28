package global.io;

import java.io.File;

/**
 *
 * Allows access to both File and a path usable in Python scripts.
 *
 * @author Antonin Pavelka
 */
public class PythonPath {

	private final File file;
	private final String path;

	public PythonPath(File file) {
		this.file = file;
		this.path = file.getAbsolutePath().replace("\\", "/");
	}

	public File getFile() {
		return file;
	}

	public String getPath() {
		return path;
	}
}
