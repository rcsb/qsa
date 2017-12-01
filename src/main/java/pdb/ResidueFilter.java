package pdb;

/**
 *
 * @author Antonin Pavelka
 */
public interface ResidueFilter {

	public boolean reject(String pdbCode, String chain, int residueNumber, Character insertionCode);
}
