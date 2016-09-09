package spark.interfaces;

import pdb.SimpleStructure;

public class AlignablePair {
	private SimpleStructure a, b;

	public AlignablePair(SimpleStructure a, SimpleStructure b) {
		this.a = a;
		this.b = b;
	}

	public SimpleStructure getA() {
		return a;
	}

	public SimpleStructure getB() {
		return b;
	}

}
