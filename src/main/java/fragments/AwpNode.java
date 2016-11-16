package fragments;

/**
 * Aligned word pair.
 */
public class AwpNode {
	private WordInterface x, y; // from first and second protein
	private int clusterId;

	public AwpNode(WordInterface x, WordInterface y) {
		this.x = x;
		this.y = y;
	}

	public int getClusterId() {
		return clusterId;
	}

	public void setClusterId(int id) {
		this.clusterId = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		return result;
	}

	public String toString() {
		return x.getId() + "_" + y.getId();
	}

	@Override
	public boolean equals(Object o) {
		AwpNode other = (AwpNode) o;
		return x.equals(other.x) && y.equals(other.y);
	}

}