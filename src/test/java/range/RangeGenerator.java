package range;

import java.util.Random;

/**
 *
 * @author Antonin Pavelka
 */
public class RangeGenerator {

	private Random random = new Random(1);
	private int bins;

	public RangeGenerator(int bins) {
		this.bins = bins;
	}

	public int[] reasonable() {
		int[] range = new int[2];
		range[0] = random.nextInt(2 * bins) - bins / 2;
		range[1] = range[0] + random.nextInt(bins);
		return range;
	}

	public int[] crazy() {
		int[] range = new int[2];
		range[0] = random.nextInt(10 * bins) - 5 * bins;
		range[1] = random.nextInt(10 * bins) - 5 * bins;
		if (range[0] > range[1]) {
			int swap = range[0];
			range[0] = range[1];
			range[1] = swap;
		}
		return range;
	}

}
