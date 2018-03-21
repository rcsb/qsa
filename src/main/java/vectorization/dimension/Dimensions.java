package vectorization.dimension;

/**
 *
 * @author Antonin Pavelka
 */
public class Dimensions {

	private Dimension[] types;

	public Dimensions(Dimension... cyclic) {
		this.types = cyclic;
	}

	public double computeDifference(int dimension, float a, float b) {
		return types[dimension].computeDifference(a, b);
	}

	public double computeDifference(int dimension, double a, double b) {
		return types[dimension].computeDifference(a, b);
	}

	public Dimension getLastDimension() {
		return types[types.length - 1];
	}

	/**
	 *
	 * Temporary hack.
	 *
	 * Maybe the cyclicity should be solved more in DimensionCyclic and less outside.
	 *
	 * Also breaks OOP principles.
	 */
	@Deprecated
	public boolean isCyclic(int d) {
		return types[d] instanceof DimensionCyclic;
	}

	public Dimensions merge(Dimensions other) {
		Dimension[] newTypes = new Dimension[types.length + other.types.length];
		System.arraycopy(types, 0, newTypes, 0, types.length);
		System.arraycopy(other.types, 0, newTypes, types.length, other.types.length);
		return new Dimensions(newTypes);
	}

	public int number() {
		return types.length;
	}

}
