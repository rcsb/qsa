package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class Table {

	private List<List<Double>> table;
	private Map<Integer, Double> hi_ = new HashMap<>();
	private Map<Integer, Double> lo_ = new HashMap<>();
	// private List<String> lines = new ArrayList<>();
	private List<Double> activeLine = new ArrayList<>();

	public Table() {
		table = new ArrayList<>();
	}

	public Table(List<List<Double>> table) {
		this.table = table;
	}

	public Table(File f) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line;
			while ((line = br.readLine()) != null) {
				add(line);
			}
			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setFilter(int high, int row, double threshold) {
		if (high == 1) {
			hi_.put(row, threshold);
		} else {
			lo_.put(row, threshold);
		}
	}

	public void resetFilters() {
		hi_.clear();
		lo_.clear();
	}

	public Table getFiltered() {
		List<List<Double>> filtered = new ArrayList<>();
		for (int i = 0; i < table.size(); i++) {
			List<Double> line = table.get(i);
			boolean include = true;
			for (int row = 0; row < line.size(); row++) {
				double l = Double.NEGATIVE_INFINITY;
				double h = Double.POSITIVE_INFINITY;
				if (lo_.containsKey(row)) {
					l = lo_.get(row);
				}
				if (hi_.containsKey(row)) {
					h = hi_.get(row);
				}
				double value = line.get(row);
				include &= l <= value && value <= h;
			}
			if (include) {
				filtered.add(table.get(i));
			}
		}
		return new Table(filtered);
	}

	public Table getFirst(int n) {
		return new Table(table.subList(0, n));
	}

	public void add(String sLine) {
		StringTokenizer st = new StringTokenizer(sLine, " \t;,");
		List<Double> line = new ArrayList<>();
		// st.nextToken();
		// st.nextToken();
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			double d = 8888;
			try {
				d = Double.parseDouble(s);
			} catch (Exception ex) {
			}
			line.add(d);
		}
		table.add(line);
		// lines.add(sLine);
	}

	public Table add(double value) {
		activeLine.add(value);
		return this;
	}

	public void line() {
		table.add(activeLine);
		activeLine = new ArrayList<>();
	}

	public Table sortAscending(int i) {
		Collections.sort(table, new RowComparator(i));
		return this;
	}

	public Table sortDescending(int i) {
		Collections.sort(table, Collections.reverseOrder(new RowComparator(i)));
		return this;
	}

	public List<List<Double>> get() {
		return table;
	}

	public void print() {
		for (List<Double> line : table) {
			for (Double d : line) {
				String s = String.format("%8.3f ", d);
				System.out.print(s);
				//System.out.format(;
			}
			System.out.println();
		}
	}
}

class RowComparator implements Comparator<List<Double>> {

	int col;

	public RowComparator(int c) {
		col = c;
	}

	public int compare(List<Double> a, List<Double> b) {
		return a.get(col).compareTo(b.get(col));
	}
}
