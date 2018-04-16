package embedding.lipschitz.object;

/**
 *
 * @author Antonin Pavelka
 */
public interface PointTupleDistanceMeasurement {

	public double getDistance(PointTuple a, PointTuple b);
}
