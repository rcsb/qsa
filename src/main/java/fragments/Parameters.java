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

	public double getSeqSep() {
		return getWordLength();
	}

	public double getResidueContactDistance() {
		return 20;
	}

	public double getMaxTranslation() {
		return 10;
	}

	public double getMaxRotation() {
		return 0.7;
	}
	
	public double getMaxFragmentSimilarity() {
		return 1.9;
	}
	
	public double getMaxFragmentDist() {
		return 4;
	}
}
