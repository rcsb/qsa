package statistics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Antonin Pavelka
 */
public class ReservoirSample<T> implements Iterable<T> {

	private final List<T> sample = new ArrayList<>();
	private final Random random;
	private final int howMany;
	private int index;

	public ReservoirSample(int howMany, Random random) {
		this.random = random;
		this.howMany = howMany;
	}

	public void add(T t) {
		if (index < howMany) {
			sample.add(t);
			index++;
		} else {
			int r = random.nextInt(1 + index++);
			if (r < howMany) {
				sample.set(r, t);
			}
		}
	}

	@Override
	public Iterator<T> iterator() {
		return sample.iterator();
	}

	public static void main(String[] args) {
		int howMany = 10;
		int outOf = 100;
		int cycles = 10000000;
		long[] counts = new long[outOf];

		for (int cycle = 0; cycle < cycles; cycle++) {
			ReservoirSample<Integer> s = new ReservoirSample<>(howMany, new Random());
			for (int i = 0; i < outOf; i++) {
				s.add(i);
			}
			for (int i : s) {
				counts[i]++;
			}
		}
		for (long c : counts) {
			System.out.println(c);
		}
	}
}
