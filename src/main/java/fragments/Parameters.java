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
		return 3.5; // 1:3
	}

	public double getMaxWordDistDiff() {
		return 3;
	}

	public double getMaxWordRmsd() { // or is it fragment?
		return 3.5; // 1:2, 2:3.5, 3:4
	}

	// -----------------------------------------------
	public double getMaxTranslationDifference() {
		return 4;
	}

	public double getMaxCompatibilityDistance() {
		return 4;
	}

	public double getMaxRotationCompatibilityAngle() {
		return Math.PI / 4;
	}

	public double getMaxEulerDistance() {
		return Math.PI / 4;
	}

	public double getMaxBiwordGridDistance() {
		return 6;
	}

	public double getMergeRmsd() {
		return 2.5; // 1:2.5
	}
	
	
	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	public double tmFilter() {
		return -0.4;
	}

	// ----
	public boolean doClustering() {
		return false;
	}

	public boolean visualize() {
		return true;
	}
}
