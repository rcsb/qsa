package algorithm.scoring;

import geometry.primitives.Point;
import javax.vecmath.Point3d;
import structure.Residue;
import structure.SimpleStructure;

/**
 * A pairing 1 : 1 of residues between two structures.
 */
public class ResidueAlignment {

	private final SimpleStructure[] structures = new SimpleStructure[2];
	private final Residue[][] rr;
	private final int normalizationLength;

	public ResidueAlignment(SimpleStructure sa, SimpleStructure sb, int normalizationLength, Residue[][] mapping) {
		this.structures[0] = sa;
		this.structures[1] = sb;
		// query size
		this.normalizationLength = normalizationLength;
		this.rr = mapping;
	}

	public SimpleStructure getA() {
		return structures[0];
	}

	public SimpleStructure getB() {
		return structures[1];
	}

	public SimpleStructure[] getStructures() {
		return structures;
	}

	public Residue[][] getResidueParing() {
		return rr;
	}

	private int getResidueCount(int i) {
		return structures[i].size();
	}

	public boolean empty() {
		return getResidueCount(0) == 0 || getResidueCount(1) == 0;
	}

	public SimpleStructure get(int i) {
		return structures[i];
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

	public int getMatchingResiduesAbsolute() {
		return rr[0].length;
	}

	public double getIdentity() {
		if (rr[0].length == 0) {
			return 0;
		}
		int match = 0;
		int total = 0;
		for (int i = 0; i < rr[0].length; i++) {
			assert rr[0] != null;
			assert rr[0][i] != null;
			assert rr[0][i].getName() != null;
			assert rr[1][i].getName() != null;
			String x = rr[0][i].getName().toUpperCase();
			String y = rr[1][i].getName().toUpperCase();
			if (x.equals(y)) {
				match++;
			}
			total++;
		}
		double identity = (double) match / total;
		return identity;
	}

	public double getRmsd() {
		if (rr[0].length == 0) {
			return 0;
		}
		double sum = 0;
		for (int i = 0; i < rr[0].length; i++) {
			Residue r = rr[0][i];
			Residue q = rr[1][i];
			double d = r.getPosition().squaredDistance(q.getPosition());
			sum += d;
		}
		double avg = sum / rr[0].length;
		double rmsd = Math.sqrt(avg);
		return rmsd;
	}

	public int getMinStrLength() {
		return normalizationLength;
	}

	public double getMatchingResidues() {
		if (normalizationLength == 0) {
			return 0;
		} else {
			return (double) getMatchingResiduesAbsolute() / normalizationLength;
		}
	}

	/**
	 * https://en.wikipedia.org/wiki/Template_modeling_score
	 */
	public double getTmScore() {
		if (rr[0].length == 0) {
			return 0;
		}
		int length = normalizationLength;
		if (length <= 20) {
			return 0;
		}
		double d0 = 1.24 * Math.pow(length - 15, 1.0 / 3) - 1.8;
		double score = 0;
		for (int i = 0; i < rr[0].length; i++) {
			Residue r = rr[0][i];
			Residue q = rr[1][i];
			double d = r.getPosition().distance(q.getPosition());
			double dd = (d / d0);
			score += 1 / (1 + dd * dd);
		}
		return score / length;
	}

	public static double getTmScore(Point3d[] x, Point3d[] y, double normLength) {
		if (normLength <= 20) {
			return 0;
		}
		double d0 = 1.24 * Math.pow(normLength - 15, 1.0 / 3) - 1.8;
		double score = 0;
		for (int i = 0; i < x.length; i++) {
			double d = x[i].distance(y[i]);
			double dd = (d / d0);
			score += 1 / (1 + dd * dd);
		}
		return score / normLength;
	}
}
