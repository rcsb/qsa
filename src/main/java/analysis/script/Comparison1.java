package analysis.script;

import global.io.LineFile;
import java.io.File;

public class Comparison1 {

	private static double threshold;

	private static boolean hit(double p) {
		return p >= threshold;
	}

	public static void main(String[] args) {
		boolean superior = true;
		String dir = "c:/kepler/data/qsa/analysis/";
		LineFile lf = new LineFile(new File(dir + "/c.csv"));
		String[] lines = lf.asArray();
		for (int i = 0; i < 1000; i++) {
			threshold = i / 1000.0;
			int none = 0;
			int xSupY = 0;
			int ySupX = 0;
			int both = 0;
			for (String line : lines) {
				String[] ts = line.split(",");
				double frag = Double.parseDouble(ts[4]);
				double fatcat = Double.parseDouble(ts[5]);
				double click = Double.parseDouble(ts[6]);
				double x = frag;
				double y = click;

				if (hit(x) && hit(y)) {
					both++;
				} else if (hit(x) && (!hit(y))) {
					xSupY++;
				} else if (hit(y) && (!hit(x))) {
					ySupX++;
				} else {
					none++;
				}

			}
			if (xSupY < ySupX && xSupY > 0) {
				superior = false;
				System.out.println("xxxxxxxxxxxxxx");
				System.out.println(threshold + ": " + xSupY + " " + ySupX + " "
					+ both + " " + none + " "
					+ (xSupY + ySupX + both + none));
			}
			if ((both + xSupY) > 10 && (both + ySupX) > 10) {
				double factor = (double) (both + xSupY) / (both + ySupX);
				System.out.println(threshold + ": " + factor + " | " + xSupY + " " + ySupX + " "
					+ both + " " + none + " "
					+ (xSupY + ySupX + both + none));
			}
		}
		System.out.println("superior: " + superior);
	}

}
