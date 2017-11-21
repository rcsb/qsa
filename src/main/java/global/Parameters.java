package global;

import java.io.Serializable;

/**
 *
 * @author Antonin Pavelka
 */
public class Parameters implements Serializable {

	private static final long serialVersionUID = 1L;

	private Parameters() {
	}

	public static Parameters create() {
		return new Parameters();
	}

	/**
	 *
	 * @return Maximum distance between C-alpha atoms of consecutive residues in sequence in Angstroms.
	 */
	public double sequenceNeighborLimit() {
		return 5;
	}

	public int getWordLength() {
		return 3;
	}

	public double getResidueContactDistance() {
		return 8;
	}

	public double getAtomContactDistance() {
		return 7;
	}

	public int skipX() {
		return 1;
	}

	public int skipY() {
		return 1;
	}

	public double getMaxFragmentRmsd() {
		return 2; // 1:3, 4:3.5
	}

	public double newMaxDeviation() {
		return 7;
	}

	public double newAvgDeviation() {
		return 5;
	}

	public double tmFilter() {
		return 0.3;
	}

	public boolean visualize() {
		return true;
	}

	public boolean visualizeBiwords() {
		return true;
	}

	public boolean debug() {
		return true;
	}

	public boolean displayFirstOnly() {
		return true;
	}

	public byte getIndexBrackets() {
		return 10;
	}

	public double[] getRanges() {
		double a = 2;
		double b = 0.5;
		double[] ranges = {a, a, a, a, b, b};
		return ranges;
	}

	public int[] getBins() {
		int a = 40;
		int b = 60;
		int[] bins = {a, a, a, a, b, b};
		return bins;
	}

	public boolean isParallel() {
		return true;
	}
}
