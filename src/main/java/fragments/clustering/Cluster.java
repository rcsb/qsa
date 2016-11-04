package fragments.clustering;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import org.biojava.nbio.structure.io.LocalPDBDirectory;

import fragments.FragmentPair;
import geometry.Transformation;
import io.Directories;
import pdb.Residue;
import pdb.ResidueId;
import pdb.SimpleStructure;
import superposition.SuperPositionQCP;
import util.pymol.PymolVisualizer;

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
	private int id;
	private static int idG;
	private String loadA;
	private String loadB;

	public Cluster(FragmentPair p) {
		list.add(p);
		core = p;
		id = idG++;
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

	public String getLoadA() {
		return loadA;
	}

	public String getLoadB() {
		return loadB;
	}

	private ResidueId[][] computeAlignment() {
		System.out.print("aln list " + list.size());
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
		System.out.println(" aln size " + alignment[0].length);
		return alignment;
	}

	public void computeScore(SimpleStructure a, SimpleStructure b) {
		aln = computeAlignment();

		SuperPositionQCP qcp = new SuperPositionQCP();
		Point3d[] x = a.getPoints(aln[0]);
		Point3d[] y = b.getPoints(aln[1]);
		qcp.set(x, y);
		Matrix4d m = qcp.getTransformationMatrix();
		setMatrix(m);
		double rmsd = qcp.getRmsd();
		setRmsd(rmsd);

		SimpleStructure tb = new SimpleStructure(b);
		tb.transform(getMatrix());
		String name = a.getPdbCode() + "_" + b.getPdbCode() + "_" + id;
		File fa = Directories.createDefault().getAlignedA(name);
		File fb = Directories.createDefault().getAlignedB(name);
		PymolVisualizer.save(a, fa);
		PymolVisualizer.save(tb, fb);
		loadA = "load " + fa;
		loadB = "load " + fb;

		for (int i = 0; i < aln[0].length; i++) {
			Residue r = a.getResidue(aln[0][i]);
			Residue q = tb.getResidue(aln[1][i]);
			double d = r.getPosition().distance(q.getPosition());
			// System.out.println(d + " ");
			double dd = (d);
			score += 1 / (1 + dd * dd);
		}
		// System.out.println();

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
