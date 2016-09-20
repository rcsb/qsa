package analysis.tabular;

import java.util.ArrayList;
import java.util.List;

public class Row {
	private List<Value> values = new ArrayList<>();

	public Row(List<String> ss) {
		for (String s : ss) {
			values.add(new Value(s));
		}
	}

	public Value get(int i) {
		return values.get(i);
	}

	public int size() {
		return values.size();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Value v : values) {
			sb.append(v).append(" ");
		}
		return sb.toString();
	}

	/*
	 * public String toString(String... cols) { for (String c : cols) {
	 * 
	 * } }
	 */
}
