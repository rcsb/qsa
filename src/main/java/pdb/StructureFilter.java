package pdb;

import global.Parameters;

/**
 *
 * @author Antonin Pavelka
 */
public class StructureFilter {

	private final Parameters parameters;

	public StructureFilter(Parameters parameters) {
		this.parameters = parameters;
	}

	public boolean accept(SimpleStructure structure) {
		if (structure.size() < parameters.getMinResidues()) {
			System.out.println("Skipped too small structure " + structure.getSource());
			return false;
		} else if (structure.size() > parameters.getMaxResidues()) {
			System.out.println("Skipped too big structure " + structure.getSource());
			return false;
		} else {
			return true;
		}
	}
}
