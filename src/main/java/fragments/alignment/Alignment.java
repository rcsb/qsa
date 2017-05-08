package fragments.alignment;

import fragments.AwpNode;
import java.util.Collection;
import pdb.Residue;

/**
 *
 * @author Antonin Pavelka
 */
public interface Alignment {

	public double getScore();

	public Residue[][] getBestPairing();
}
