package fragments;

import alignment.score.WordAlignmentFactory;
import alignment.score.ResidueAlignment;
import fragments.clustering.ResiduePair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import pdb.Residue;

import pdb.SimpleStructure;
import superposition.SuperPositionQCP;

public class ResidueAlignmentFactory implements Comparable<ResidueAlignmentFactory> {

	private final SimpleStructure a;
	private final SimpleStructure b;
	private final Residue[][] biwordAlignment;
	private ResidueAlignment residueAlignment;
	private double rmsd;
	private Matrix4d matrix;
	private double tmScore;
	private final Debugger debug;
	private Point3d[][] points;

	public ResidueAlignmentFactory(SimpleStructure a, SimpleStructure b, Residue[][] pairing,
		Debugger debug) {
		this.a = a;
		this.b = b;
		this.biwordAlignment = pairing;
		this.debug = debug;
	}

	public Debugger getDebugger() {
		return debug;
	}

	public ResidueAlignment getEquivalence() {
		return residueAlignment;
	}

	public int getLength() {
		return biwordAlignment[0].length;
	}

	public double getRmsd() {
		return rmsd;
	}

	public int compareTo(ResidueAlignmentFactory other) {
		return Double.compare(other.tmScore, tmScore);
	}

	public Residue[][] getSuperpositionAlignment() {
		return biwordAlignment;
	}

	// reason: is done on the fly in expansionAlignemnt
	/*@Deprecated
	private Residue[][] computeBiwordAlignment(Collection<AwpNode> nodes) {
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
		Residue[][] pairing = new Residue[2][aln.size()];
		for (int i = 0; i < aln.size(); i++) {
			pairing[0][i] = aln.get(i)[0];
			pairing[1][i] = aln.get(i)[1];
		}
		return pairing;
	}*/
	private Matrix4d computeMatrix(Residue[][] rs) {
		SuperPositionQCP qcp = new SuperPositionQCP();
		Point3d[][] newPoints = {getPoints(rs[0]), getPoints(rs[1])};
		points = newPoints;
		qcp.set(points[0], points[1]);
		Matrix4d m = qcp.getTransformationMatrix();
		rmsd = qcp.getRmsd();
		return m;
	}

	private Point3d[] getPoints(Residue[] rs) {
		Point3d[] ps = new Point3d[rs.length];
		for (int i = 0; i < rs.length; i++) {
			ps[i] = rs[i].getPosition3d();
		}
		return ps;
	}

	// 1st step
	public void alignBiwords() {
		matrix = computeMatrix(biwordAlignment);
		Point3d[] xs = points[0];
		Point3d[] ys = points[1];
		for (int i = 0; i < ys.length; i++) {
			Point3d y = ys[i];
			matrix.transform(y);
		}
		tmScore = ResidueAlignment.tmScore(xs, ys, Math.min(a.size(), b.size()));
	}

	// 2nd step
	// TODO refactor, repeat one more time, rewrite with cycle
	// profile slow cases separatelly?
	// establish largest connected component, possibly with cross-checks and reject FP based on its size not allowing tm score above 0.3
	// ... but only if assembly is problem and it would differ in complexity, probably would, what is alg.
	// ... revive checks, for assembly and components? for twists along hinge helix... how?
	// filter alignments the same way, by number of matched residues if too low, even for initial
	// grid with buffer, is it in sep. proj.?
	public void refine() {
		SimpleStructure tb = new SimpleStructure(b);
		tb.transform(matrix);
		residueAlignment = WordAlignmentFactory.create(a, tb);
		if (residueAlignment.getResidueParing()[0].length >= biwordAlignment.length / 2 + 1) { // TODO to params
			matrix = computeMatrix(residueAlignment.getResidueParing());
			tb.transform(matrix);
			ResidueAlignment eq2 = WordAlignmentFactory.create(a, tb);
			if (eq2.tmScore() > residueAlignment.tmScore()) {
				residueAlignment = eq2;
			}
		}
		tmScore = residueAlignment.tmScore();
	}

	public double getTmScore() {
		return tmScore;
	}

}
