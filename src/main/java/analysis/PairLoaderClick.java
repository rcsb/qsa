package analysis;

import java.io.File;
import java.nio.file.Path;
import java.util.StringTokenizer;
import language.Pair;

public class PairLoaderClick {

	private final String[][] items;
	private int index;

	public PairLoaderClick(Path dir) {
		File[] files = dir.toFile().listFiles();
		items = new String[files.length][2];
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory() && f.getName().contains("-")) {
				StringTokenizer st = new StringTokenizer(f.getName(), "-");
				String a = st.nextToken();
				String b = st.nextToken();
				String[] p = {a, b};
				items[i] = p;
			}
		}
	}

	public Pair<String> getNext() {
		String[] p = items[index++];
		return new Pair(p[0], p[1]);
	}

	public int size() {
		return items.length;
	}

}
