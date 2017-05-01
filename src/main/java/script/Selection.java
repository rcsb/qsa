package script;

import io.LineFile;
import java.io.File;

public class Selection {

	private static double threshold;

	private static boolean hit(int l, double p) {
		return /*l >= 50 && */ p >= threshold;
	}

	public static void main(String[] args) {
		String dir = "c:/kepler/data/qsa/analysis/";
		LineFile lf = new LineFile(new File(dir + "/expand_fatcat_click_selection.csv"));
		String[] lines = lf.asArray();
		int counter = 0;
		for (String line : lines) {
			String[] ts = line.split(",");
			if (ts.length > 6) {
				double fr = Double.parseDouble(ts[4]);
				double fc = Double.parseDouble(ts[5]);
				double cl = Double.parseDouble(ts[6]);
				counter++;
				//System.out.println(fr + " " + fc + " " + cl);
				//if (fr >= 0.5 && fc < 0.2 && cl < 0.3) {
				if (fr >= 0.4 && cl < 0.1) {
					System.out.println(ts[0] + "_" + ts[1]);
				}
			}
		}
		System.out.println("counter " + counter);
	}
}
