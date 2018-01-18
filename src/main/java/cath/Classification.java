package cath;

import java.util.Arrays;

/**
 *
 * @author Antonin Pavelka
 */
public class Classification {

	private int[] levels;

	public Classification(String content) {
		String[] split = content.split("\\.");
		levels = new int[split.length];
		for (int i = 0; i < levels.length; i++) {
			levels[i] = Integer.parseInt(split[i]);
		}
	}

	public boolean belongsTo(Classification other) {
		if (this.getDepth() >= other.getDepth()) {
			throw new RuntimeException();
		}
		for (int i = 0; i < this.getDepth(); i++) {
			if (this.levels[i] != other.levels[i]) {
				return false;
			}
		}
		return true;
	}

	public Classification createParrent() {
		int n = getDepth();
		if (n <= 1) {
			throw new RuntimeException();
		}
		int[] newLevels = Arrays.copyOfRange(levels, 0, n - 1);
		return new Classification(newLevels);
	}

	public Classification(int[] levels) {
		this.levels = levels;
	}

	public int getDepth() {
		return levels.length;
	}

	public int getClazz() {
		return levels[0];
	}

	public int getArchitecture() {
		return levels[1];
	}

	public int getToplogy() {
		return levels[2];
	}

	public int getHomologousFamily() {
		return levels[3];
	}

	public Classification createHomologousFamily() {
		int[] family = new int[3];
		for (int i = 0; i < 3; i++) {
			family[i] = levels[i];
		}
		return new Classification(family);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < levels.length; i++) {
			sb.append(levels[i]);
			if (i < levels.length - 1) {
				sb.append(".");

			}
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 19 * hash + Arrays.hashCode(this.levels);
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		Classification other = (Classification) o;
		if (levels.length != other.levels.length) {
			return false;
		}
		for (int i = 0; i < levels.length; i++) {
			if (levels[i] != other.levels[i]) {
				return false;
			}
		}
		return true;
	}
}
