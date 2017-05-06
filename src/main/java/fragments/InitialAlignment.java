package fragments;

import fragments.clustering.ResiduePair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pdb.Residue;

/**
 *
 * @author Antonin Pavelka
 */
public class InitialAlignment {

	private Residue[][] pairing;

	public InitialAlignment(Collection<AwpNode> nodes) {
		computeAlignment(nodes);
	}

	public Residue[][] getPairing() {
		return pairing;
	}

	private void computeAlignment(Collection<AwpNode> nodes) {
		Set<ResiduePair> a = new HashSet<>();
		for (AwpNode awp : nodes) {
			Residue[] x = awp.getWords()[0].getResidues();
			Residue[] y = awp.getWords()[1].getResidues();
			for (int i = 0; i < x.length; i++) {
				Residue xi = x[i];
				Residue yi = y[i];
				a.add(new ResiduePair(xi, yi));
			}
		}
		Set<Residue> usedX = new HashSet<>();
		Set<Residue> usedY = new HashSet<>();
		List<Residue[]> aln = new ArrayList<>();
		for (ResiduePair rp : a) {
			Residue x = rp.x;
			Residue y = rp.y;
			if (!usedX.contains(x) && !usedY.contains(y)) {
				usedX.add(x);
				usedY.add(y);
				Residue[] p = {x, y};
				aln.add(p);
			}
		}
		pairing = new Residue[2][aln.size()];
		for (int i = 0; i < aln.size(); i++) {
			pairing[0][i] = aln.get(i)[0];
			pairing[1][i] = aln.get(i)[1];
		}
	}
}
