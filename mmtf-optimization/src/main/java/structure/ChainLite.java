package structure;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Antonin Pavelka
 */
public class ChainLite {

	private final ChainId id;
	private final List<ResidueLite> residues = new ArrayList<>();

	public void add(ResidueLite r) {
		residues.add(r);
	}

	public List<ResidueLite> getResidues() {
		return residues;
	}

	public ChainLite(ChainId id) {
		this.id = id;
	}

	public ChainId getId() {
		return id;
	}

	public ResidueLite getResidue(ResidueId rid) {
		for (ResidueLite r : residues) {
			if (rid.equals(r.getId())) {
				return r;
			}
		}
		return null;
	}

}
