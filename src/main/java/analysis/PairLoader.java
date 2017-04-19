package analysis;

import io.LineFile;
import java.io.File;
import java.util.StringTokenizer;
import util.Pair;

public class PairLoader {

	private final String[] items;
	private int index;

	public PairLoader(File f) {
		LineFile lf = new LineFile(f);
		items = lf.asArray();
	}

	public Pair<String> getNext() {
		StringTokenizer st = new StringTokenizer(items[index++], " \t");
		return new Pair(st.nextToken(), st.nextToken());
	}

	public int size() {
		return items.length;
	}
}
