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

public class AlignmentCore {

	private SimpleStructure a;
	private SimpleStructure b;
	private ResidueId[][] aln;

	public AlignmentCore(SimpleStructure a, SimpleStructure b, ResidueId[][] aln) {
		this.a = a;
		this.b = b;
		this.aln = aln;
	}

	public double computeScore() {
		SuperPositionQCP qcp = new SuperPositionQCP();
		Point3d[] x = a.getPoints(aln[0]);
		Point3d[] y = b.getPoints(aln[1]);
		qcp.set(x, y);
		Matrix4d m = qcp.getTransformationMatrix();
		double rmsd = qcp.getRmsd();
		SimpleStructure tb = new SimpleStructure(b);
		tb.transform(m);
		String name = a.getPdbCode() + "_" + b.getPdbCode();
		File fa = Directories.createDefault().getAlignedA(name);
		File fb = Directories.createDefault().getAlignedB(name);
		PymolVisualizer.save(a, fa);
		PymolVisualizer.save(tb, fb);
		String loadA = "load " + fa;
		String loadB = "load " + fb;
		File fileA = fa;
		File fileB = fb;
		double score = 0;
		for (int i = 0; i < aln[0].length; i++) {
			Residue r = a.getResidue(aln[0][i]);
			Residue q = tb.getResidue(aln[1][i]);
			double d = r.getPosition().distance(q.getPosition());
			double dd = (d);
			score += 1 / (1 + dd * dd);
		}
		return score;
	}

}
