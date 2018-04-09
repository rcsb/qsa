package embedding.lipschitz;

import fragment.cluster.Fragment;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author Antonin Pavelka
 */
public class ObjectPairs implements Iterable<ObjectPair> {

	private ObjectPair[] samples;
	private Random random = new Random(1);

	public ObjectPairs(Similar[] objects, int sampleSize) {
		samples = new ObjectPair[sampleSize];
		for (int i = 0; i < sampleSize; i++) {
			Similar[] pair = new Fragment[2];
			for (int k = 0; k < 2; k++) {
				pair[k] = objects[random.nextInt(objects.length)];
			}
			double rmsd = pair[0].getDistance(pair[1]);
			ObjectPair objectPair = new ObjectPair(pair[0], pair[1], rmsd);
			samples[i] = objectPair;
		}
	}

	@Override
	public Iterator<ObjectPair> iterator() {
		return Arrays.asList(samples).iterator();
	}

}
