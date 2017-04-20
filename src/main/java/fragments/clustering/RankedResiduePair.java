package fragments.clustering;

import pdb.Residue;
import pdb.ResidueId;

public class RankedResiduePair implements Comparable<RankedResiduePair> {
	private int count; // in how many AFP does this pair occur
	private double bestRmsd = Double.MAX_VALUE; // of fragments
	private Residue x, y;

	public RankedResiduePair(Residue x, Residue y) {
		this.x = x;
		this.y = y;
	}

	public Residue getX() {
		return x;
	}

	public Residue getY() {
		return y;
	}

	public void add(double rmsd) {
		if (rmsd < bestRmsd) {
			bestRmsd = rmsd;
		}
		count++;
	}

	public int compareTo(RankedResiduePair other) {
		int c = Integer.compare(other.count, count);
		if (c == 0) {
			c = Double.compare(bestRmsd, other.bestRmsd);
		}
		return c;
	}

	public double getBestRmsd() {
		return bestRmsd;
	}

	public int getCount() {
		return count;
	}

}
