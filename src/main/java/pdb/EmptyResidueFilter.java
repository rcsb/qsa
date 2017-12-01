package pdb;

/**
 *
 * @author Antonin Pavelka
 *
 * Rejects nothing.
 */
public class EmptyResidueFilter implements ResidueFilter {

	@Override
	public boolean reject(String pdbCode, String chain, int residueNumber, Character insertionCode) {
		return false;
	}
}
