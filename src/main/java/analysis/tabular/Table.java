package analysis.tabular;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.DoublePredicate;

import io.Directories;

public class Table {
	private static String SEP = ",";
	private Map<String, Integer> namesToCols = new LinkedHashMap<>();
	private List<Row> rows = new ArrayList<>();
	private List<DoublePredicate> predicates = new ArrayList<>();
	private List<Integer> predicateCols = new ArrayList<>();
	private Set<Integer> colFilter = new HashSet<>();

	public Table(File csv, boolean header) {
		try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
			String line = br.readLine();
			if (header) {
				List<String> l = readLine(line);
				for (int i = 0; i < l.size(); i++) {
					namesToCols.put(l.get(i), i);
				}
			}
			while ((line = br.readLine()) != null) {
				Row r = new Row(readLine(line));
				rows.add(r);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void filterCols(String... names) {
		colFilter.clear();
		for (String n : names) {
			colFilter.add(namesToCols.get(n));
		}
	}

	public void print() {
		int width = 18;
		List<String> nl = new ArrayList<>();
		nl.addAll(namesToCols.keySet());
		for (int i = 0; i < nl.size(); i++) {
			if (colFilter.isEmpty() || colFilter.contains(i)) {
				System.out.printf("%" + width + "s", nl.get(i));
			}
		}
		System.out.println();
		for (Row r : eval()) {
			for (int i = 0; i < r.size(); i++) {
				if (colFilter.isEmpty() || colFilter.contains(i)) {
					if (r.get(i).isDouble()) {
						System.out.printf("%" + width + ".3f", r.get(i).getDouble());
					} else {
						System.out.printf("%" + width + "s", r.get(i));
					}
				}
			}
			System.out.println();
		}
	}

	/* TODO deal with empty fields */
	private List<String> readLine(String line) {
		List<String> list = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(line, SEP);
		while (st.hasMoreTokens()) {
			list.add(st.nextToken());
		}
		return list;
	}

	public void addPredicate(String col, DoublePredicate f) {
		predicateCols.add(namesToCols.get(col));
		predicates.add(f);
	}

	public List<Row> eval() {
		List<Row> result = new ArrayList<>();
		for (Row r : rows) {
			boolean satistfies = true;
			for (int p = 0; p < predicates.size(); p++) {
				for (int c : predicateCols) {
					satistfies &= r.get(c).satisfies(predicates.get(p));
				}
			}
			if (satistfies)
				result.add(r);
		}
		return result;
	}

	public static void main(String[] args) {
		boolean print = true;
		Directories dir = Directories.createDefault();
		Table t = new Table(dir.getAlignmentCsv(), true);
		// t.filterCols("probability", "tmScore");
		//t.addPredicate("probability", d -> d != 0 && d < 0.05);
		//t.addPredicate("norm_align_score", d -> d > 0);
		//t.addPredicate("hsp", d -> d < 10);
		// t.addPredicate("tmScore", d -> d > 0.5);
		// t.addPredicate("tmScore", d -> d > 0.4);
		// List<Row> rs = t.eval();
		// System.out.println(rs.size());
		// System.out.println(t.getHeader());
		if (print) {
			t.print();
		}
	}

}
