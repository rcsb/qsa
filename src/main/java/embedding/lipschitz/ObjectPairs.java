package embedding.lipschitz;

import embedding.lipschitz.object.AlternativeMode;
import embedding.lipschitz.object.AlternativePointTuples;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import embedding.lipschitz.object.PointTupleDistanceMeasurement;

/**
 *
 * @author Antonin Pavelka
 */
public class ObjectPairs implements Iterable<ObjectPair> {

	private ObjectPair[] samples;
	private Random random = new Random(1);

	public ObjectPairs() {

	}

	public ObjectPairs(
		PointTupleDistanceMeasurement distanceMeasurement,
		AlternativePointTuples[] objects,
		int sampleSize,
		AlternativeMode alternativeMode) {

		samples = new ObjectPair[sampleSize];
		for (int i = 0; i < sampleSize; i++) {
			AlternativePointTuples[] pair = new AlternativePointTuples[2];
			for (int k = 0; k < 2; k++) {
				pair[k] = objects[random.nextInt(objects.length)];
			}
			ObjectPair objectPair = new ObjectPair(pair[0], pair[1], distanceMeasurement, alternativeMode);
			samples[i] = objectPair;
		}
	}

	@Override
	public Iterator<ObjectPair> iterator() {
		return Arrays.asList(samples).iterator();
	}

}
