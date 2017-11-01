package fragments;

/**
 * Aligned word pair.
 */
public class AwpNode {

	private final WordImpl x, y; // from first and second protein
	private int id = -1;
	private int connectivity;
	private Component component;

	public AwpNode(WordImpl x, WordImpl y) {
		this.x = x;
		this.y = y;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public boolean before(AwpNode other) {
		if (x.getId() < other.x.getId()) {
			return true;
		} else if (x.getId() > other.x.getId()) {
			return false;
		} else { // x ~ other.x
			if (y.getId() < other.y.getId()) {
				return true;
			} else if (y.getId() > other.y.getId()) {
				return false;
			} else {
				throw new RuntimeException();
			}
		}
	}

	public void setComponent(Component c) {
		this.component = c;
	}

	public Component getComponent() {
		return component;
	}

	public void connect() {
		connectivity++;
	}

	public int getConnectivity() {
		return connectivity;
	}

	public WordImpl[] getWords() {
		WordImpl[] words = {x, y};
		return words;
	}

	@Override
	public int hashCode() {
		/*final int prime = 31;
		int result = 1;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		return result;*/
		if (id < 0) {
			throw new IllegalStateException();
		}
		return id;
	}

	@Override
	public String toString() {
		return x.getId() + "_" + y.getId();
	}

	@Override
	public boolean equals(Object o) {
		if (true) {
			throw new UnsupportedOperationException();
		}
		AwpNode other = (AwpNode) o;
		return x.equals(other.x) && y.equals(other.y);
	}

}
