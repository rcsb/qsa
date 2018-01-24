package structure;

/**
 *
 * @author Antonin Pavelka
 */
public class StructureSizeFilter {

	private final int min, max;

	public StructureSizeFilter(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public boolean accept(SimpleStructure structure) {
		if (structure.size() < min) {
			System.out.println("Skipped too small structure " + structure.getSource());
			return false;
		} else if (structure.size() > max) {
			System.out.println("Skipped too big structure " + structure.getSource());
			return false;
		} else {
			return true;
		}
	}
}
