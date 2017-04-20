package fragments;

import alignment.score.Equivalence;
import alignment.score.EquivalenceFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import pdb.Residue;

import pdb.ResidueId;
import pdb.SimpleStructure;
import superposition.SuperPositionQCP;

public class AlignmentCore implements Comparable<AlignmentCore> {

	private SimpleStructure a;
	private SimpleStructure b;
	private Residue[][] aln;
	private Equivalence equivalence;
	private int clusterNumber;
	private double rmsd;
	private double score;

	public AlignmentCore(SimpleStructure a, SimpleStructure b, Residue[][] aln, int clusterNumber) {
		this.a = a;
		this.b = b;
		this.aln = aln;
		this.clusterNumber = clusterNumber;
		this.equivalence = align();
		this.score = equivalence.tmScore();
	}

	public Equivalence getEquivalence() {
		return equivalence;
	}

	public int getLength() {
		return aln[0].length;
	}

	public double getRmsd() {
		return rmsd;
	}

	public int compareTo(AlignmentCore other) {
		return Double.compare(other.score, score);
	}
	
	public Residue[][] getAln() {
		return aln;
	}

	private Equivalence align() {
		SuperPositionQCP qcp = new SuperPositionQCP();
		Point3d[] x = a.getPoints(aln[0]);
		Point3d[] y = b.getPoints(aln[1]);
		qcp.set(x, y);
		Matrix4d m = qcp.getTransformationMatrix();
		rmsd = qcp.getRmsd();
		SimpleStructure tb = new SimpleStructure(b);
		tb.transform(m);
		Equivalence eq = EquivalenceFactory.create(a, tb);
		return eq;
	}

}
