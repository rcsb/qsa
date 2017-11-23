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

	public static BiwordId decode(long value) {
		int x = (int) (value >> 32);
		int y = (int) value;
		return new BiwordId(x, y);
	}

	public long endcode() {
		long value = (((long) structureId) << 32) | (idWithinStructure & 0xffffffffL);
		return value;
	}

	public boolean equals(Object o) {
		BiwordId other = (BiwordId) o;
		return structureId == other.structureId
			&& idWithinStructure == other.idWithinStructure;
	}
}
