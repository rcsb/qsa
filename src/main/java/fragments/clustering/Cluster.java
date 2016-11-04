package fragments.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Matrix4d;

import fragments.FragmentPair;
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
	private double score;
	private Matrix4d matrix;
	private ResidueId[][] aln;
	private double rmsd;

	public Cluster(FragmentPair p) {
		list.add(p);
		core = p;
	}

	public void setMatrix(Matrix4d m) {
		this.matrix = new Matrix4d(m);
	}

	public Matrix4d getMatrix() {
		return matrix;
	}

	public int getCoverage() {
		return getAlignment()[0].length;
	}

	public ResidueId[][] getAlignment() {
		return aln;
	}

	public void setRmsd(double d) {
		rmsd = d;

	}

	public double getRmsd() {
		return rmsd;
	}

	private ResidueId[][] computeAlignment() {
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

	public void computeScore(SimpleStructure a, SimpleStructure b) {
		aln = computeAlignment();
		for (int i = 0; i < aln[0].length; i++) {
			Residue r = a.getResidue(aln[0][i]);
			Residue q = b.getResidue(aln[1][i]);
			double d = r.getPosition().distance(q.getPosition());
			double dd = (d / 3.5);
			score += 1 / (1 + dd * dd);
		}
	}

	public double getScore() {
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
		return Double.compare(score, other.score);
	}
}
