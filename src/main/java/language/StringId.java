package language;

import java.util.Objects;

/**
 *
 * @author Antonin Pavelka
 */
public class StringId {

	private final String id;

	public StringId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		StringId other = (StringId) o;
		return id.equals(other.id);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + Objects.hashCode(this.id);
		return hash;
	}

}
