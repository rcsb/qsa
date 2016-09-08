package spark.interfaces;

import java.io.Serializable;

public class MassAligner implements Serializable {

	private StructureAlignmentAlgorithm saa;

	public MassAligner(StructureAlignmentAlgorithm saa) {
		this.saa = saa;
	}

}
