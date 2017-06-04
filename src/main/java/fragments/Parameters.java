package fragments;

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

	public double sequenceNeighborLimit() {
		return 500;
	}

	public int getWordLength() {
		return 10;
	}

	public double getResidueContactDistance() {
		return 8;
	}

	public int skipX() {
		return 1;
	}

	public int skipY() {
		return 1;
	}

	public double getMaxFragmentRmsd() {
		return 3.5; // 1:3, 4:3.5
	}

	public double getMaxWordDistDiff() {
		return 3;
	}

	public double getMaxWordRmsd() { // or is it fragment?
		return 3.5; // 1:2, 2:3.5, 3:4, 4:3.5
	}

	public double newMaxDeviation() {
		return 7;
	}

	public double newAvgDeviation() {
		return 5;
	}

	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	public double tmFilter() {
		return -0.3;
	}

	public double rigid() {
		return 3;
	}

	// ----
	public boolean visualize() {
		return true;
	}

	public boolean debug() {
		return true;
	}

	public boolean displayFirstOnly() {
		return true;
	}

	public double[] getRanges() {
		double[] ranges = {2, 2, 2, 2, 2, 2};
		return ranges;
	}
}
