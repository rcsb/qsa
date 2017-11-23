package biword;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordId {

	private final int structureId;
	private final int idWithinStructure;

	/*public static BiwordId decode() {

	}

	public long encode() {

	}

	private long endcode(int first, int second) {
		long l = (((long) x) << 32) | (y & 0xffffffffL);
		int x = (int) (l >> 32);
		int y = (int) l;
	}
	
	private int decodeFirst(long value) {
		
	}
	
	private int decodeSecond(long value) {
		
	}*/

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
