package pdb.cath;

import static org.jmol.script.T.chain;
import pdb.cath.tree.Domain;
import pdb.ResidueFilter;
import pdb.ResidueId;

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
	public boolean reject(String pdbCode, ResidueId residueId) {
		return !domain.doesResidueBelong(residueId);
	}
}
