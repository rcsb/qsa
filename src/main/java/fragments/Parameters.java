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

	/*
	 * public double getSeqSep() { return 0;// getWordLength(); }
	 */

	public double getResidueContactDistance() {
		return 20;
	}

	public double getMaxRotation() {
		return 0.7;
	}

	public double getMaxFragmentSimilarity() {
		return 1.9;
	}

	public boolean findAfpBySuperposing() {
		return true;
	}

	// -----------------------------------------------------------------------
	public int skip() {
		return 10;
	}

	public double getMaxFragmentRmsd() {
		return 3;
	}

	public double getMaxTranslationDifference() {
		return 4;
		// return 4;
	}

	public double getMaxCompatibilityDistance() {
		return 3;
	}

	public double getMaxRotationCompatibilityAngle() {
		// return Math.PI / 4;
		return Math.PI / 4;
	}

}
