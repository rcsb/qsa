package fragments;

public class DummyWord implements WordInterface {

	private int id;

	public DummyWord(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		WordInterface other = (WordInterface) o;
		return getId() == other.getId();
	}

	public String toString() {
		return Integer.toString(id);
	}

}
