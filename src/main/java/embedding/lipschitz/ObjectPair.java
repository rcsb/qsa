package embedding.lipschitz;

import embedding.lipschitz.object.AlternativeMode;
import embedding.lipschitz.object.AlternativePointTuples;
import embedding.lipschitz.object.PointTupleDistanceMeasurement;

/**
 *
 * Distance computation assumes the alternatives are permutations and it is enough to permute just one side.
 *
 * @author Antonin Pavelka
 *
 *
 *
 */
public class ObjectPair {

	public final AlternativePointTuples a;
	public final AlternativePointTuples b;
	private final double distance;

	public ObjectPair(AlternativePointTuples a, AlternativePointTuples b, PointTupleDistanceMeasurement measurement,
		AlternativeMode mode) {

		this.a = a;
		this.b = b;
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < mode.numberOfPointTuples(); i++) {
			double d = measurement.getDistance(a.getAlternative(i, mode), b.getCanonicalTuple());
			if (d < min) {
				min = d;
			}
		}
		distance = min;
	}

	public double getDistance() {
		return distance;
	}
}
