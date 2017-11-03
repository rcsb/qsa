package analysis.benchmarking;

import pdb.ChainId;

public class Substructure {

	public final String code;
	public final ChainId cid;

	public Substructure(String code) {
		this.code = code;
		cid = null;
	}

	public Substructure(String code, ChainId cid) {
		this.code = code;
		this.cid = cid;
	}

}
