package biword;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordId {

	private final int structureId;
	private final int idWithinStructure;

	public BiwordId(int structureId, int idWithinStructure) {
		this.structureId = structureId;
		this.idWithinStructure = idWithinStructure;
	}

	public int getStructureId() {
		return structureId;
	}

	public int getIdWithinStructure() {
		return idWithinStructure;
	}

}
