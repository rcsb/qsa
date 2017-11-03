package analysis;

import global.io.LineFile;
import java.io.File;
import java.util.StringTokenizer;
import util.Pair;

public class PairLoader {

	private final String[] items;
	private int index;
	private boolean labels;
	private boolean noDomain;

	public PairLoader(File f, boolean labels) {
		LineFile lf = new LineFile(f);
		items = lf.asArray();
		this.labels = labels;
	}

	public void setNoDomain(boolean noDomain) {
		this.noDomain = noDomain;
	}

	private String prepare(String s) {
		if (noDomain) {
			return s.substring(0, Math.min(5, s.length()));
		} else {
			return s;
		}
	}
	public Pair<String> getNext() {
		if (labels && index == 0) {
			index = 1;
		}
		StringTokenizer st = new StringTokenizer(items[index++], " \t");
		if (labels) {
			st.nextToken();
		}
		return new Pair(prepare(st.nextToken()), prepare(st.nextToken()));
	}

	public int size() {
		if (labels) {
			return items.length - 1;
		} else {
			return items.length;
		}
	}

}
