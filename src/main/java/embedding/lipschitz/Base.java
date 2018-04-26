package embedding.lipschitz;

import embedding.lipschitz.object.PointTuple;
import embedding.lipschitz.object.PointTupleDistanceMeasurement;

/**
 *
 * Lipschitz base.
 *
 * @author Antonin Pavelka
 */
public class Base {

	private PointTuple[] objects;
	private PointTupleDistanceMeasurement distance;

	public Base() {

	}

	public Base(PointTupleDistanceMeasurement distance, PointTuple... objects) {
		this.objects = objects;
		this.distance = distance;
	}

	public double getDistance(PointTuple other) {
		double min = Double.MAX_VALUE;
		for (PointTuple object : objects) {
			double d = distance.getDistance(object, other);
			if (d < min) {
				min = d;
			}
		}
		return min;
	}

	public double getDistance(Base other) {
		double min = Double.MAX_VALUE;
		for (PointTuple object : objects) {
			for (PointTuple otherObject : other.objects) {
				double d = distance.getDistance(object, otherObject);
				if (d < min) {
					min = d;
				}
			}
		}
		return min;
	}
}
