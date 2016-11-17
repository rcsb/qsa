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
		return 2;
	}

	public double getMaxWordDistDiff() {
		return 3;
	}

	public double getMaxWordRmsd() {
		return 2.5;
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

}
