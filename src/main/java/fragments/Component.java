package fragments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pdb.Residue;

/**
 *
 * @author Antonin Pavelka
 */
public class Component {

	private List<AwpNode> nodes = new ArrayList<>();
	private Set<Residue> as = new HashSet<>();
	private Set<Residue> bs = new HashSet<>();

	public void add(AwpNode n) {
		nodes.add(n);
		Word x = n.getWords()[0];
		Word y = n.getWords()[1];
		for (Residue r : x.getResidues()) {
			as.add(r);
		}
		for (Residue r : y.getResidues()) {
			bs.add(r);
		}
	}

	public int sizeInResidues() {
		return Math.min(as.size(), bs.size());
	}
}
