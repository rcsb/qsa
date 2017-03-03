package structure;

/**
 * Stores information about atom from PDB file.
 */
public class AtomLite implements Comparable<AtomLite> {

	private final int serialNumber;
	private final float x, y, z;
	private final String element;
	private final float temperatureFactor; // B-factor
	private final String name;
	private final ResidueLite residue;
	private final char altLoc;

	public AtomLite(int serialNumber, String name, char altLoc, ResidueLite residue,
		float x, float y, float z, float temperatureFactor, String element) {
		this.serialNumber = serialNumber;
		this.name = name;
		this.residue = residue;
		this.x = x;
		this.y = y;
		this.z = z;
		this.temperatureFactor = temperatureFactor;
		this.element = element;
		this.altLoc = altLoc;
	}

	public int getSerialNumber() {
		return serialNumber;
	}

	public ResidueLite getResidue() {
		return residue;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public String getElement() {
		return element;
	}

	public double getTemperatureFactor() {
		return temperatureFactor;
	}

	public String getName() {
		return name;
	}

	public boolean isBackbone() {
		if (name.trim().toUpperCase().equals("H")
			|| name.trim().toUpperCase().equals("N")
			|| name.trim().toUpperCase().equals("CA")
			|| name.trim().toUpperCase().equals("HA")
			|| name.trim().toUpperCase().equals("C")
			|| name.trim().toUpperCase().equals("O")) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return name + " " + serialNumber + " " + altLoc + ".";
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 71 * hash + this.serialNumber;
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof AtomLite)) {
			return false;
		}
		AtomLite a = (AtomLite) o;
		return this.serialNumber == a.serialNumber;
	}

	@Override
	public int compareTo(AtomLite a) {
		return new Integer(this.serialNumber).compareTo(a.serialNumber);
	}
}
