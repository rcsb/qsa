package script;

import io.Directories;
import io.LineFile;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import util.Pair;
import util.Timer;

public class PairGenerator {

	String[] items;
	int n;
	private Random random = new Random(1);

	public PairGenerator(File f) throws IOException {
		load(f);
		n = items.length;
	}

	public Pair<String> getRandom(int i) {
		int x = 0;
		int y = 0;
		while (x == y) {
			x = random.nextInt(items.length);
			y = random.nextInt(items.length);
		}
		return new Pair(items[x], items[y]);
	}

	public void load(File f) throws IOException {
		Directories dirs = Directories.createDefault();
		LineFile lf = new LineFile(dirs.getCathS20());
		String[] items = lf.asArray();
		Timer.stop();
		System.out.println("reading finished " + Timer.get());
		Timer.start();
		List<Pair<String>> pairs = new ArrayList<>();
		for (int x = 0; x < items.length; x++) {
			for (int y = 0; y < x; y++) {

			}
		}
		Timer.stop();
		System.out.println(pairs.size() + " pairs");
		System.out.println("pairing finished " + Timer.get());
		Timer.start();
		Random random = new Random(1);
		Collections.shuffle(pairs, random);
		Timer.stop();
		System.out.println("shuffling finished " + Timer.get());
		Timer.start();
		try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(dirs.getPairs()))) {
			pairs.stream().map((Pair p) -> p.x + "," + p.y).forEach(pw::println);
		}
		Timer.stop();
		System.out.println("transformation finished " + Timer.get());

	}

	public static void main(String[] args) throws IOException {
	}
}
