package algorithm;

import embedding.lipschitz.object.AlternativeMode;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordAlternativeMode implements AlternativeMode {

	public boolean interchangeable;
	public boolean invertible;

	public BiwordAlternativeMode() {
	}

	public BiwordAlternativeMode(boolean interchangeable, boolean invertible) {
		this.interchangeable = interchangeable;
		this.invertible = invertible;
	}

	@Override
	public int numberOfPointTuples() {
		int n = 1;
		if (interchangeable) {
			n *= 2;
		}
		if (invertible) {
			n *= 2;
		}
		return n;
	}
}
