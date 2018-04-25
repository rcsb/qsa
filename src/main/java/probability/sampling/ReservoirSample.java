package probability.sampling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import probability.random.RandomLong;

/**
 *
 * @author Antonin Pavelka
 */
public class ReservoirSample<T> implements Iterable<T> {

	private final List<T> sample = new ArrayList<>();
	private final int howMany;
	private long index;
	private RandomLong random = new RandomLong();

	public ReservoirSample(int howMany) {
		this.howMany = howMany;
	}

	public ReservoirSample(int howMany, int seed) {
		this.howMany = howMany;
		this.random.setSeed(seed);
	}

	public void add(T t) {
		if (index < howMany) {
			sample.add(t);
			index++;
		} else {
			index++;
			long r = random.nextLong(index);
			if (r < howMany) {
				sample.set((int) r, t);
			}
		}
	}

	@Override
	public Iterator<T> iterator() {
		if (index < howMany) {
			throw new RuntimeException("Sample is not sufficiently filled " + index + " / " + howMany);
		}
		return sample.iterator();
	}

	public T[] getArray() {
		Object[] a = new Object[sample.size()];
		sample.toArray(a);
		return (T[]) a;
	}

	public static void main(String[] args) {
		int howMany = 10;
		long outOf = 100;
		int cycles = 10000000;
		long[] counts = new long[(int) outOf];

		for (int cycle = 0; cycle < cycles; cycle++) {
			ReservoirSample<Integer> s = new ReservoirSample<>(howMany);
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
