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

	public PythonPath(File home, File file) {
		this.file = file;
		String prefix = home.getAbsolutePath();
		String all = file.getAbsolutePath();
		if (!all.startsWith(prefix)) {
			throw new RuntimeException();
		}
		String relative = all.substring(prefix.length());
		this.path = "." + relative.replace("\\", "/");
	}

	public File getFile() {
		return file;
	}

	public String getPath() {
		return path;
	}

	public static void main(String[] args) {
		PythonPath pp = new PythonPath(new File("c:/kepler/"), new File("c:\\kepler\\rozbal"));
		System.out.println(pp.getPath());

	}
}
