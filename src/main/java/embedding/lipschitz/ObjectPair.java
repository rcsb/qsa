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

	private AlternativePointTuples a;
	private AlternativePointTuples b;
	private double distance;

	public ObjectPair() {
	}

	public ObjectPair(AlternativePointTuples a, AlternativePointTuples b, PointTupleDistanceMeasurement measurement,
		AlternativeMode mode) {

		this.a = a;
		this.b = b;
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < mode.numberOfPointTuples(); i++) {
			assert measurement != null;
			assert a != null;
			assert a.getAlternative(i, mode) != null;
			assert b.getCanonicalTuple() != null;

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

	public AlternativePointTuples a() {
		return a;
	}

	public AlternativePointTuples b() {
		return b;
	}
}
