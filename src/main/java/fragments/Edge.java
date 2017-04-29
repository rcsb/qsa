package fragments;

public class Edge implements Comparable<Edge> {

	private AwpNode x, y;
	private double rmsd;

	public Edge(AwpNode x, AwpNode y, double rmsd) {
		assert x != null;
		assert y != null;
		assert Double.isFinite(rmsd) && !Double.isNaN(rmsd) : rmsd;
		this.x = x;
		this.y = y;
		this.rmsd = rmsd;
	}

	public int compareTo(Edge other) {
		return Double.compare(rmsd, other.rmsd);
	}

	public double getRmsd() {
		return rmsd;
	}

	public AwpNode getX() {
		return x;
	}

	public AwpNode getY() {
		return y;
	}

}
