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
		return 6;
	}

	public double getResidueContactDistance() {
		return 6;
	}

	public int skip() {
		return 10;
	}

	public double getMaxFragmentRmsd() {
		return 4;
	}

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
