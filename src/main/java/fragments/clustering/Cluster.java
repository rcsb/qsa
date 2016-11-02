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

/**
 *
 * @author Antonin Pavelka
 */
public class Cluster implements Comparable<Cluster> {

	private List<FragmentPair> list = new ArrayList<>();
	private FragmentPair core;

	public Cluster(FragmentPair p) {
		list.add(p);
		core = p;
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

	@Deprecated
	public void tryToAdd(FragmentPair p) {
		if (Math.abs(core.getFragmentDistance() - p.getFragmentDistance()) <= Parameters.create()
				.getMaxFragmentDist()) {
			if (core.isTranformationSimilar(p)) {
				add(p);
			}
		}
	}

	public int size() {
		return list.size();
	}

	@Override
	public int compareTo(Cluster other) {
		return Integer.compare(other.size(), size());
	}
}
