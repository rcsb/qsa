package fragments;

import alignment.score.Equivalence;
import alignment.score.EquivalenceFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import pdb.Residue;

import pdb.SimpleStructure;
import superposition.SuperPositionQCP;

public class AlignmentCore implements Comparable<AlignmentCore> {

	private final SimpleStructure a;
	private final SimpleStructure b;
	private final Residue[][] superpositionAlignment;
	private final Equivalence equivalence;
	private double rmsd;
	private final double score;
	private final Debugger debug;

	@Deprecated // move to EquivalenceFactory
	public AlignmentCore(SimpleStructure a, SimpleStructure b, Residue[][] aln,
		Debugger debug) {
		this.a = a;
		this.b = b;
		this.superpositionAlignment = aln;
		this.equivalence = align();
		this.score = equivalence.tmScore();
		this.debug = debug;
	}

	public Debugger getDebugger() {
		return debug;
	}

	public Equivalence getEquivalence() {
		return equivalence;
	}

	public int getLength() {
		return superpositionAlignment[0].length;
	}

	public double getRmsd() {
		return rmsd;
	}

	public int compareTo(AlignmentCore other) {
		return Double.compare(other.score, score);
	}

	public Residue[][] getSuperpositionAlignment() {
		return superpositionAlignment;
	}

	private Matrix4d computeMatrix(SimpleStructure sa, SimpleStructure sb, Residue[][] rs) {
		SuperPositionQCP qcp = new SuperPositionQCP();
		Point3d[] x = sa.getPoints(rs[0]);
		Point3d[] y = sb.getPoints(rs[1]);
		//System.out.println(x.length);
		qcp.set(x, y);
		Matrix4d m = qcp.getTransformationMatrix();
		rmsd = qcp.getRmsd();
		//System.out.println("rmsd = " + rmsd);
		return m;
	}

	private Equivalence align() {
		//System.out.println("+++");
		Matrix4d m = computeMatrix(a, b, superpositionAlignment);

		SimpleStructure tb = new SimpleStructure(b);
		tb.transform(m);

		Equivalence eq = EquivalenceFactory.create(a, tb);
		//System.out.println("tm="+eq.tmScore());

		if (eq.getResidueParing()[0].length >= superpositionAlignment.length / 2 + 1) {
			
			m = computeMatrix(a, tb, eq.getResidueParing());
			tb.transform(m);
			Equivalence eq2 = EquivalenceFactory.create(a, tb);
			if (eq2.tmScore() > eq.tmScore()) {
				return eq2;
			}
		}
		//System.out.println("tm="+eq.tmScore());

		//System.out.println("---");
		return eq;
	}

}
