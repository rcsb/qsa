package algorithm.scoring;

import geometry.Point;
import javax.vecmath.Point3d;
import pdb.Residue;
import pdb.SimpleStructure;

/**
 * A pairing 1 : 1 of residues between two structures.
 */
public class ResidueAlignment {

	private final SimpleStructure[] s = new SimpleStructure[2];
	private final Residue[][] rr;
	private final int minStrLength;

	public ResidueAlignment(SimpleStructure sa, SimpleStructure sb, Residue[][] mapping) {
		this.s[0] = sa;
		this.s[1] = sb;
		this.minStrLength = Math.min(sa.size(), sb.size());
		this.rr = mapping;
	}

	public SimpleStructure getA() {
		return s[0];
	}
	
	public SimpleStructure getB() {
		return s[1];
	}
	
	public Residue[][] getResidueParing() {
		return rr;
	}

	private int getResidueCount(int i) {
		return s[i].size();
	}

	public boolean empty() {
		return getResidueCount(0) == 0 || getResidueCount(1) == 0;
	}

	public SimpleStructure get(int i) {
		return s[i];
	}

	public Point center() {
		Point[] all = new Point[2 * size()];
		int index = 0;
		for (int k = 0; k < 2; k++) {
			for (int i = 0; i < size(); i++) {
				all[index++] = rr[k][i].getPosition();
			}
		}
		return Point.center(all);
	}

	public int size() {
		return rr[0].length;
	}

	public int matchingResidues() {
		return rr[0].length;
	}

	public int getMinStrLength() {
		return minStrLength;
	}

	public double matchingResiduesRelative() {
		if (minStrLength == 0) {
			return 0;
		} else {
			return (double) matchingResidues() / minStrLength;
		}
	}

	/**
	 * https://en.wikipedia.org/wiki/Template_modeling_score
	 */
	public double tmScore() {
		if (rr[0].length == 0) {
			return 0;
		}
		int lengthTarget = minStrLength;
		if (lengthTarget <= 20) {
			return 0;
		}
		double d0 = 1.24 * Math.pow(lengthTarget - 15, 1.0 / 3) - 1.8;
		double score = 0;
		for (int i = 0; i < rr[0].length; i++) {
			Residue r = rr[0][i];
			Residue q = rr[1][i];
			double d = r.getPosition().distance(q.getPosition());
			double dd = (d / d0);
			score += 1 / (1 + dd * dd);
		}
		return score / lengthTarget;
	}

	public static double tmScore(Point3d[] x, Point3d[] y, double lengthTarget) {
		if (lengthTarget <= 20) {
			return 0;
		}
		double d0 = 1.24 * Math.pow(lengthTarget - 15, 1.0 / 3) - 1.8;
		double score = 0;
		for (int i = 0; i < x.length; i++) {
			double d = x[i].distance(y[i]);
			double dd = (d / d0);
			score += 1 / (1 + dd * dd);
		}
		return score / lengthTarget;
	}

	public static void main(String[] args) {
		int lengthTarget = 50;
		double d0 = 1.24 * Math.pow(lengthTarget - 15, 1.0 / 3) - 1.8;
		double score = 0;
		double d = 2;
		double dd = (d / d0);
		score = 1 / (1 + dd * dd);
		System.out.println(score);
		System.out.println(score / lengthTarget);
	}
}
