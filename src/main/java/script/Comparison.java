package script;

import io.LineFile;
import java.io.File;
import java.util.StringTokenizer;

public class Comparison {

	private static boolean hit(int l, double p) {
		return l >= 50 && p >= 0.05;
	}

	public static void main(String[] args) {
		String dir = "c:/kepler/data/qsa/analysis/";
		LineFile lf = new LineFile(new File(dir + "/c.csv"));
		int ab = 0;
		int bb = 0;
		int nn = 0;
		int both = 0;
		for (String line : lf.readLines()) {
			String[] ts = line.split(",");
			int al = Integer.parseInt(ts[1]);
			double ap = Double.parseDouble(ts[2 + 1]);
			int bl = Integer.parseInt(ts[5]);
			double bp = Double.parseDouble(ts[6 + 1]);
			if (hit(al, ap) || hit(bl, bp)) {
				if (hit(al, ap) && !hit(bl, bp)) {
					ab++;
				}
				if (!hit(al, ap) && hit(bl, bp)) {
					bb++;
				}
				if (hit(al, ap) && hit(bl, bp)) {
					both++;
				}
			} else {
				nn++;
			}
			//System.out.println(al + " " + ap + " " + bl + " " + bp);
		}

		System.out.println(
			"---");
		System.out.println(ab);

		System.out.println(bb);

		System.out.println(both);

		System.out.println(nn);
	}
}
