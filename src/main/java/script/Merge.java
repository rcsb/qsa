package script;

import io.LineFile;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Merge {

	public static Map<String, String> map(String fn) {
		HashMap<String, String> map = new HashMap<>();
		LineFile lf = new LineFile(new File(fn));
		for (String line : lf.readLines()) {
			StringTokenizer st = new StringTokenizer(line, ", \t");
			String key = st.nextToken() + "_" + st.nextToken();
			StringBuilder value = new StringBuilder();
			while (st.hasMoreTokens()) {
				value.append(st.nextToken()).append(",");
			}
			map.put(key, value.toString());
		}
		return map;
	}

	public static void main(String[] args) throws IOException {
		String dir = "c:/kepler/data/qsa/analysis/";
		Map<String, String> a = map(dir + "a.csv");
		Map<String, String> b = map(dir + "b.csv");
		BufferedWriter bw = new BufferedWriter(new FileWriter(dir + "/c.csv"));
		for (String ak : a.keySet()) {
			if (b.containsKey(ak)) {
				bw.write(ak + "," + a.get(ak) + "," + b.get(ak) + "\n");
				b.remove(ak);
			} else {
				bw.write(ak + "," + a.get(ak) + ",0,0,0,\n");
			}

		}
		for (String bk : b.keySet()) {
			bw.write(bk + ",-,-,-," + b.get(bk) + "\n");
		}
		bw.close();
	}
}
