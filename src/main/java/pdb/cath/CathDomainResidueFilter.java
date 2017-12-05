package pdb.cath;

import pdb.ResidueFilter;

/**
 *
 * @author Antonin Pavelka
 */
public class CathDomainResidueFilter implements ResidueFilter {

	private final Domain domain;

	public CathDomainResidueFilter(Domain domain) {
		this.domain = domain;
	}

	@Override
	public boolean reject(String pdbCode, String chain, int residueNumber, Character insertionCode) {
		return !domain.doesResidueBelong(chain, residueNumber, insertionCode);
	}
}
