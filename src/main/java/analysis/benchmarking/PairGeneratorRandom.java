package analysis.benchmarking;

import global.io.LineFile;
import java.io.File;
import java.util.Random;
import java.util.StringTokenizer;
import util.Pair;

public class PairGeneratorRandom {

	private final String[] items;
	private final Random random = new Random(3);

	public PairGeneratorRandom(File f) {
		LineFile lf = new LineFile(f);
		String[] a = lf.asArray();
		items = new String[a.length];
		for (int i = 0; i < a.length; i++) {
			StringTokenizer st = new StringTokenizer(a[i], " \t");
			items[i] = st.nextToken();
		}
	}

	public Pair<String> getNext() {
		int x = 0;
		int y = 0;
		while (x == y) {
			x = random.nextInt(items.length);
			y = random.nextInt(items.length);
		}
		return new Pair(items[x], items[y]);
	}

	public String[] getAllItems(int max) {
		if (items.length <= max) {
			return items;
		} else {
			String[] sample = new String[max];
			for (int i = 0; i < max; i++) {
				sample[i] = items[random.nextInt(max)];
			}
			return sample;
		}
	}

	public String getRandomItem() {
		return items[random.nextInt(items.length)];
	}

	public int size() {
		return Integer.MAX_VALUE;
	}
}
