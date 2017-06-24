package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 */
public class Randomness {

	Random random;

	public Randomness(int seed) {
		random = new Random(seed);
	}

	public int next(int max) {
		return random.nextInt(max);
	}

	public int[] subsample(int howMany, int fromSize) {
		List<Integer> numbers = new ArrayList<>();
		if (fromSize < howMany) {
			howMany = fromSize;
		}
		for (int i = 0; i < fromSize; i++) {
			numbers.add(i);
		}
		int[] sample = new int[howMany];
		for (int i = 0; i < howMany; i++) {
			sample[i] = numbers.remove(random.nextInt(numbers.size()));
		}
		return sample;
	}

	public Point3d[][] subsample(int howMany, Point3d[][] from) {
		if (from.length < howMany) {
			howMany = from.length;
		}
		Point3d[][] result = new Point3d[howMany][];
		int[] indexes = subsample(howMany, from.length);
		for (int i = 0; i < howMany; i++) {
			result[i] = from[indexes[i]];
		}
		return result;
	}

	public <T> List<T> subsample(int howMany, List<T> from) {
		if (from.size() < howMany) {
			howMany = from.size();
		}
		List<T> result = new ArrayList<>();
		int[] indexes = subsample(howMany, from.size());
		for (int i = 0; i < howMany; i++) {
			result.add(from.get(indexes[i]));
		}
		return result;
	}

}
