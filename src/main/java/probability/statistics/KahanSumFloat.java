package probability.statistics;

/**
 * https://rosettacode.org/wiki/Kahan_summation
 */
@Deprecated
public class KahanSumFloat {

	private float sum = 0.0f;
	private float c = 0.0f;

	public void add(float f) {
		float y = f - c;
		float t = sum + y;
		c = (t - sum) - y;
		sum = t;
	}

	public float value() {
		return sum;
	}

}
