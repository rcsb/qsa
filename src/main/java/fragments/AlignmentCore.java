package fragments;

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
	private double score;
	private File fileA;
	private File fileB;
	private int clusterNumber;
	private double rmsd;

	public AlignmentCore(SimpleStructure a, SimpleStructure b, ResidueId[][] aln, int clusterNumber) {
		this.a = a;
		this.b = b;
		this.aln = aln;		
		this.clusterNumber = clusterNumber;
		this.score = computeScore();
	}

	public double getScore() {
		return score;
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

	public String getLoadA() {
		return "load " + fileA;

	}

	public String getLoadB() {
		return "load " + fileB;
	}

	private double computeScore() {
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
		double score = 0;
		for (int i = 0; i < aln[0].length; i++) {
			Residue r = a.getResidue(aln[0][i]);
			Residue q = tb.getResidue(aln[1][i]);
			double d = r.getPosition().distance(q.getPosition());
			double dd = (d / 10);
			score += 1 / (1 + dd * dd);
		}
		return score;
	}

}
