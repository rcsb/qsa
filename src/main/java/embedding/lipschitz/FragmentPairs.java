package embedding.lipschitz;

import fragment.Fragments;
import fragment.cluster.Fragment;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author Antonin Pavelka
 */
public class FragmentPairs implements Iterable<FragmentPair> {

	private FragmentPair[] samples;
	private Random random = new Random(1);

	public FragmentPairs(Fragments fragments, int sampleSize) {
		samples = new FragmentPair[sampleSize];
		for (int i = 0; i < sampleSize; i++) {
			Fragment[] pair = new Fragment[2];
			for (int k = 0; k < 2; k++) {
				pair[k] = fragments.get(random.nextInt(fragments.size()));
			}
			double rmsd = pair[0].getDistance(pair[1]);
			FragmentPair s = new FragmentPair(pair[0], pair[1], rmsd);
		}
	}

	@Override
	public Iterator<FragmentPair> iterator() {
		return Arrays.asList(samples).iterator();
	}

}
