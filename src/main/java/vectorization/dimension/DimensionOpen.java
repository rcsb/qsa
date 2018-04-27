package vectorization.dimension;

/**
 *
 * @author Antonin Pavelka
 */
public class DimensionOpen implements Dimension {

	public DimensionOpen() {

	}

	public double computeDifference(double coordinate1, double coordinate2) {
		return Math.abs(coordinate1 - coordinate2);
	}

}
