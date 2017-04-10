package alignment.score;

import geometry.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import pdb.PdbLine;
import pdb.Residue;
import pdb.SimpleStructure;

public class Equivalence {

	private final SimpleStructure sa, sb;
	private final Residue[][] rr;

	public Equivalence(SimpleStructure sa, SimpleStructure sb, Residue[][] mapping) {
		this.sa = sa;
		this.sb = sb;
		this.rr = mapping;
	}

	public void save(File f) throws IOException {
		int serial = 1;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for (int i = 0; i < rr.length; i++) {
				for (int k = 0; k < 2; k++) {
					Point p = rr[k][i].getPosition();
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
	}

	public int size() {
		return rr[0].length;
	}

	public int matchingResidues() {
		return rr[0].length;
	}

	public double matchingResiduesRelative() {
		return (double) matchingResidues() / Math.min(sa.size(), sb.size());
	}

	public double tmScore() {
		double score = 0;
		for (int i = 0; i < rr[0].length; i++) {
			Residue r = rr[0][i];
			Residue q = rr[1][i];
			double d = r.getPosition().distance(q.getPosition());
			double dd = (d / 10);
			score += 1 / (1 + dd * dd);
		}
		return score;
	}
}
