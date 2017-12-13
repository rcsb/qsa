package analysis.benchmarking;

import pdb.StructureSource;

public class StructurePair {

	public final StructureSource a, b;

	public StructurePair(StructureSource a, StructureSource b) {
		this.a = a;
		this.b = b;
	}
	
	public String toString() {
		return a.toString() + "_" + b.toString();
	}

}
