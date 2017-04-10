package fragments;

import alignment.score.Equivalence;
import alignment.score.EquivalenceFactory;
import java.io.File;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import io.Directories;
import pdb.Residue;
import pdb.ResidueId;
import pdb.SimpleStructure;
import superposition.SuperPositionQCP;
import util.pymol.PymolVisualizer;

public class AlignmentCore implements Comparable<AlignmentCore> {

	private SimpleStructure a;
	private SimpleStructure b;
	private ResidueId[][] aln;
	private Equivalence equivalence;
	private File fileA;
	private File fileB;
	private int clusterNumber;
	private double rmsd;
	private double score;

	public AlignmentCore(SimpleStructure a, SimpleStructure b, ResidueId[][] aln, int clusterNumber) {
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

	public String getA() {
		return fileA.getAbsolutePath().replace("\\", "/");

	}

	public String getB() {
		return fileB.getAbsolutePath().replace("\\", "/");
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
		String name = a.getPdbCode() + "_" + b.getPdbCode() + "_" + clusterNumber;
		fileA = Directories.createDefault().getAlignedA(name);
		fileB = Directories.createDefault().getAlignedB(name);
		PymolVisualizer.save(a, fileA);
		PymolVisualizer.save(tb, fileB);
		Equivalence eq = EquivalenceFactory.create(a, tb);
		return eq;
	}

}
