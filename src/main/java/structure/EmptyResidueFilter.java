package structure;

/**
 *
 * @author Antonin Pavelka
 *
 * Rejects nothing.
 */
public class EmptyResidueFilter implements ResidueFilter {

	@Override
	public boolean reject(String pdbCode, ResidueId residueId) {
		return false;
	}
}
