package fragments;

import alignment.score.Equivalence;
import alignment.score.EquivalenceFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import pdb.Residue;

import pdb.SimpleStructure;
import superposition.SuperPositionQCP;

public class ResidueAlignment implements Comparable<ResidueAlignment> {

	private final SimpleStructure a;
	private final SimpleStructure b;
	private final Residue[][] biwordAlignment;
	private Equivalence equivalence;
	private double rmsd;
	private Matrix4d matrix;
	private double tmScore;
	private final Debugger debug;
	private final double initialScore; // seems not to be accurate enough for anything
	private Point3d[][] points;

	@Deprecated // move to EquivalenceFactory
	public ResidueAlignment(SimpleStructure a, SimpleStructure b, Residue[][] aln, double initialScore,
		Debugger debug) {
		this.a = a;
		this.b = b;
		this.biwordAlignment = aln;
		this.initialScore = initialScore;
		this.debug = debug;
	}

	public Debugger getDebugger() {
		return debug;
	}

	public Equivalence getEquivalence() {
		return equivalence;
	}

	public int getLength() {
		return biwordAlignment[0].length;
	}

	public double getRmsd() {
		return rmsd;
	}

	public int compareTo(ResidueAlignment other) {
		return Double.compare(other.tmScore, tmScore);
	}

	public Residue[][] getSuperpositionAlignment() {
		return biwordAlignment;
	}

	private Matrix4d computeMatrix(SimpleStructure sa, SimpleStructure sb, Residue[][] rs) {
		SuperPositionQCP qcp = new SuperPositionQCP();
		Point3d[][] newPoints = {sa.getPoints(rs[0]), sa.getPoints(rs[1])};
		points = newPoints;
		qcp.set(points[0], points[1]);
		Matrix4d m = qcp.getTransformationMatrix();
		rmsd = qcp.getRmsd();
		//System.out.println("rmsd = " + rmsd);
		return m;
	}

	// 1st step
	public void alignBiwords() {
		matrix = computeMatrix(a, b, biwordAlignment);
		Point3d[] xs = points[0];
		Point3d[] ys = points[1];
		for (int i = 0; i < ys.length; i++) {
			Point3d y = ys[i];
			matrix.transform(y);
		}
		tmScore = Equivalence.tmScore(xs, ys, Math.min(a.size(), b.size()));
		//System.out.println("init rmsd " + rmsd);
		//System.out.println("init tm " + tmScore);

	}

	// 2nd step
	public void refine() {
		//System.out.println("");
		//alignBiwords();

		//System.out.println("+++");
		//Matrix4d m = computeMatrix(a, b, biwordAlignment);
		SimpleStructure tb = new SimpleStructure(b);
		tb.transform(matrix);

		equivalence = EquivalenceFactory.create(a, tb);
		//System.out.println("refined tm " + equivalence.tmScore());

		if (equivalence.getResidueParing()[0].length >= biwordAlignment.length / 2 + 1) {
			matrix = computeMatrix(a, tb, equivalence.getResidueParing());
			tb.transform(matrix);
			Equivalence eq2 = EquivalenceFactory.create(a, tb);
			if (eq2.tmScore() > equivalence.tmScore()) {
				//System.out.println("refined2 tm " + eq2.tmScore());
				equivalence = eq2;
			}
		}
		tmScore = equivalence.tmScore();
	}

	public double getTmScore() {
		return tmScore;
	}

}
