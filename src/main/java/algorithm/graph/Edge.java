package algorithm.graph;

public class Edge implements Comparable<Edge> {

	private static long counter;
	private AwpNode x, y;
	private float rmsd;

	public Edge(AwpNode x, AwpNode y, double rmsd) {
		counter++;
		//if (counter % 10000 == 0) {
		//	long l = Runtime.getRuntime().totalMemory();
		//	System.out.println((counter) + " m edges " + (l / 1000000));
		//}
		assert x != null;
		assert y != null;
		assert Double.isFinite(rmsd) && !Double.isNaN(rmsd) : rmsd;
		if (x.before(y)) {
			this.x = x;
			this.y = y;
		} else {
			this.x = y;
			this.y = x;
		}
		this.rmsd = (float) rmsd;
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
