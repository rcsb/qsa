package spark.interfaces;

import spark.Alignable;

public class AlignablePair {
	private Alignable a, b;

	public AlignablePair(Alignable  a, Alignable  b) {
		this.a = a;
		this.b = b;
	}

	public Alignable getA() {
		return a;
	}

	public Alignable getB() {
		return b;
	}

}
