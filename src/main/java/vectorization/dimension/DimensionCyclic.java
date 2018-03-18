package vectorization.dimension;

/**
 *
 * @author Antonin Pavelka
 */
public class DimensionCyclic implements Dimension {

	private final double min, max;

	public DimensionCyclic(double min, double max) {
		this.min = min;
		this.max = max;
	}

	public double computeDifference(double coordinate1, double coordinate2) {
		assert min <= coordinate1 && coordinate1 <= max;
		assert min <= coordinate2 && coordinate2 <= max;
		if (coordinate2 < coordinate1) {
			double temp = coordinate1;
			coordinate1 = coordinate2;
			coordinate2 = temp;
		}
		double straight = coordinate2 - coordinate1;
		double around = (coordinate1 - min) + (max - coordinate2);
		return Math.min(straight, around);
	}
}
