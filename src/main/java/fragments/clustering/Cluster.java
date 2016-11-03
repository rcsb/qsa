package fragments.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fragments.FragmentPair;
import fragments.Parameters;
import geometry.Transformation;
import pdb.Residue;
import pdb.ResidueId;
import pdb.SimpleStructure;

/**
 *
 * @author Antonin Pavelka
 */
public class Cluster implements Comparable<Cluster> {

	private List<FragmentPair> list = new ArrayList<>();
	private FragmentPair core;
	private int coverage = -1;

	public Cluster(FragmentPair p) {
		list.add(p);
		core = p;
	}

	public int getCoverage() {
		return getAlignment()[0].length;
	}

	public ResidueId[][] getAlignment() {
		ResiduePairs a = new ResiduePairs();
		for (FragmentPair fp : list) {
			List<Residue> x = fp.get()[0].getResidues();
			List<Residue> y = fp.get()[1].getResidues();
			for (int i = 0; i < x.size(); i++) {
				ResidueId xi = x.get(i).getId();
				ResidueId yi = y.get(i).getId();
				a.add(xi, yi, fp.getRmsd());
			}
		}
		Set<ResidueId> usedX = new HashSet<>();
		Set<ResidueId> usedY = new HashSet<>();
		List<ResidueId[]> aln = new ArrayList<>();
		for (RankedResiduePair rrp : a.values()) {
			ResidueId x = rrp.getX();
			ResidueId y = rrp.getY();
			if (!usedX.contains(x) && !usedY.contains(y)) {
				usedX.add(x);
				usedY.add(y);
				ResidueId[] p = { x, y };
				aln.add(p);
			}
		}
		ResidueId[][] alignment = new ResidueId[2][aln.size()];
		for (int i = 0; i < aln.size(); i++) {
			alignment[0][i] = aln.get(i)[0];
			alignment[1][i] = aln.get(i)[1];
		}
		return alignment;
	}

	public double getScore(SimpleStructure a, SimpleStructure b) {
		double score = 0;
		ResidueId[][] aln = getAlignment();
		for (int i = 0; i < aln[0].length; i++) {
			Residue r = a.getResidue(aln[0][i]);
			Residue q = b.getResidue(aln[1][i]);
			double d = r.getPosition().distance(q.getPosition());
			double dd = (d);
			score += 1 / (1 + dd * dd);
		}
		return score;
	}

	public FragmentPair getCore() {
		return core;
	}

	public List<FragmentPair> getFragmentPairs() {
		return list;
	}

	@Deprecated
	public Transformation getTransformation() {
		return core.getTransformation();
	}

	public void add(FragmentPair p) {
		list.add(p);
		p.capture();
	}

	public void add(FragmentPair p, double rmsd) {
		list.add(p);

	}

	public int size() {
		return list.size();
	}

	@Override
	public int compareTo(Cluster other) {
		return Integer.compare(other.size(), size());
	}
}
