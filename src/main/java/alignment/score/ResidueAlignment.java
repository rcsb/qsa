package alignment.score;

import geometry.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.vecmath.Point3d;
import pdb.PdbLine;
import pdb.Residue;
import pdb.SimpleStructure;

/**
 * Residue equivalences for final scoring.
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

	public void save(Point shift, File f) {
		save(rr, shift, f);
	}

	public Residue[][] orient(Residue[][] in) {
		Residue[][] out = new Residue[in.length][in[0].length];
		for (int x = 0; x < in.length; x++) {
			for (int y = 0; y < in[0].length; y++) {
				out[x][y] = s[x].getResidue(in[x][y].getId());
			}
		}
		return out;
	}

	public void save(Residue[][] pairs, Point shift, File f) {
		try {
			int serial = 1;
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
				for (int i = 0; i < pairs[0].length; i++) {
					for (int k = 0; k < 2; k++) {
						Point p = pairs[k][i].getPosition();
						if (shift != null) {
							p = p.plus(shift);
						}
						PdbLine pl = new PdbLine(serial + k, "CA", "C", "GLY",
							Integer.toString(serial + k), 'A', p.x, p.y, p.z);
						bw.write(pl.toString());
						bw.newLine();
					}
					bw.write(PdbLine.getConnectString(serial, serial + 1));
					bw.newLine();
					serial += 2;
				}
			}

		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Deprecated
	public static void saveSelections(Residue[][] pairs, String name, File f) {
		try {
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
				for (int i = 0; i < pairs[0].length; i++) {
					bw.write("cmd.bond('" + name + "A and resi " + pairs[0][i].getId().getSequenceNumber() + "','"
						+ name + "B and resi " + pairs[1][i].getId().getSequenceNumber() + "')");
					bw.newLine();
				}
			}

		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
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
