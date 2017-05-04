package fragments;

import fragments.clustering.RankedResiduePair;
import fragments.clustering.ResiduePairs;
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
	private double score;

	public InitialAlignment(Collection<AwpNode> nodes) {
		computeAlignment(nodes);
	}

	public double getScore() {
		return score;
	}

	public Residue[][] getPairing() {
		return pairing;
	}

	private void addTmLikeScore(double rmsd) {
		score += 1.0 / (1.0 + rmsd * rmsd);
	}

	private final void computeAlignment(Collection<AwpNode> nodes) {
		ResiduePairs a = new ResiduePairs();
		for (AwpNode awp : nodes) {
			Residue[] x = awp.getWords()[0].getResidues();
			Residue[] y = awp.getWords()[1].getResidues();
			for (int i = 0; i < x.length; i++) {
				Residue xi = x[i];
				Residue yi = y[i];
				a.add(xi, yi, awp.getRmsd());
			}
		}
		Set<Residue> usedX = new HashSet<>();
		Set<Residue> usedY = new HashSet<>();
		List<Residue[]> aln = new ArrayList<>();
		for (RankedResiduePair rrp : a.values()) {
			Residue x = rrp.getX();
			Residue y = rrp.getY();
			if (!usedX.contains(x) && !usedY.contains(y)) {
				usedX.add(x);
				usedY.add(y);
				Residue[] p = {x, y};
				aln.add(p);
				addTmLikeScore(rrp.getBestRmsd());
			}
		}
		pairing = new Residue[2][aln.size()];
		for (int i = 0; i < aln.size(); i++) {
			pairing[0][i] = aln.get(i)[0];
			pairing[1][i] = aln.get(i)[1];
		}
	}
}
