package analysis.tabular;

import java.util.function.DoublePredicate;

public class Value {
	private String s;
	private Double d;

	public Value(String s) {
		try {
			d = Double.parseDouble(s);
		} catch (NumberFormatException e) {
			this.s = s;
		}
	}

	public boolean satisfies(DoublePredicate p) {
		if (d == null)
			return false;
		else {
			return p.test(d);
		}
	}

	public String toString() {
		if (d == null) {
			return s;
		} else {
			return Double.toString(d);
		}
	}
}
