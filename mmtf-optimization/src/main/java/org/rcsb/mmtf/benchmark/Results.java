package org.rcsb.mmtf.benchmark;

import io.LineFile;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Antonin Pavelka
 *
 * Table with named columns, allows variable height of columns, empty values are
 * on their ends, absolute position of value in the column has no meaning.
 *
 */
public class Results {

	private File f;
	private Map<String, List<Long>> times = new HashMap<>();
	private int height; // max height of column

	public Results(File f) {
		this.f = f;
		if (f.exists()) {
			LineFile lf = new LineFile(f);
			List<String> lines = lf.readLines();
			String[] names = lines.get(0).split(",");
			for (int i = 1; i < lines.size(); i++) {
				String[] ts = lines.get(i).split(",");
				Long[] tl = new Long[ts.length];
				for (int j = 0; j < ts.length; j++) {
					if (ts[j].trim().length() > 0) {
						tl[j] = Long.parseLong(ts[j]);
					}
				}
				for (int j = 0; j < names.length; j++) {
					String n = names[j];
					if (!times.containsKey(n)) {
						times.put(n, new ArrayList<>());
					}
					List<Long> l = times.get(n);
					if (l.size() > height) {
						height = l.size();
					}
					if (tl[j] != null) {
						l.add(tl[j]);
					}
				}
			}
		}
	}

	public void addStructure(String code, long time) {
		if (!times.containsKey(code)) {
			times.put(code, new ArrayList<>());
		}
		List<Long> l = times.get(code);
		l.add(time);
		if (l.size() > height) {
			height = l.size();
		}
	}

	public void save() {
		f.delete();
		LineFile lf = new LineFile(f);
		lf.writeLine(String.join(",", times.keySet()));
		System.out.println(height);
		for (int i = 0; i < height; i++) {
			StringBuilder sb = new StringBuilder();
			for (List<Long> l : times.values()) {
				if (i < l.size()) {
					sb.append(l.get(i)).append(",");
				} else {
					sb.append(" ,");
				}
			}
			sb.deleteCharAt(sb.length() - 1);
			lf.writeLine(sb.toString());
		}
	}

	public static void main(String[] args) {
		Results r = new Results(new File("c:/kepler/rozbal/table.csv"));
		r.addStructure("a", 1);
		r.addStructure("a", 2);
		r.addStructure("b", 1);
		r.addStructure("c", 7);
		r.addStructure("c", 8);
		r.addStructure("c", 9);
		r.save();
	}
}
