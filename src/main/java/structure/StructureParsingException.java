package structure;

/**
 *
 * @author Antonin Pavelka
 *
 */
public class StructureParsingException extends Exception {

	private boolean mysterious;

	public StructureParsingException(String message, boolean mysterious) {
		super(message);
	}

	public boolean isMysterious() {
		return mysterious;
	}
}
