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
		return 5;
	}

	public int getWordLength() {
		return 8;
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
		return 3.5; // 1:3
	}

	public double getMaxWordDistDiff() {
		return 3;
	}

	public double getMaxWordRmsd() { // or is it fragment?
		return 3.5; // 1:2, 2:3.5, 3:4
	}

	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	public double tmFilter() {
		return -0.4;
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
}
