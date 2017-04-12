package script;

import io.LineFile;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import util.Pair;

public class PairGenerator {

	private final String[] items;
	private final Random random = new Random();

	public PairGenerator(File f) {
		LineFile lf = new LineFile(f);
		items = lf.asArray();
	}

	public Pair<String> getRandom() {
		int x = 0;
		int y = 0;
		while (x == y) {
			x = random.nextInt(items.length);
			y = random.nextInt(items.length);
		}
		return new Pair(items[x], items[y]);
	}
}
