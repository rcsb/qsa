package algorithm.graph;

import java.util.List;

/**
 * @author Antonin Pavelka
 *
 */
public class AwpGraph {

	private final AwpNode[][] neighbors;
	private final double[][] rmsds;
	private final AwpNode[] nodes;

	public AwpGraph(AwpNode[] nodeSet, List<Edge> edges) {
		neighbors = new AwpNode[nodeSet.length][];
		rmsds = new double[nodeSet.length][];
		nodes = new AwpNode[neighbors.length];
		int[] counts = new int[neighbors.length];
		int i = 0;
		for (AwpNode n : nodeSet) {
			neighbors[i] = new AwpNode[n.getConnectivity()];
			rmsds[i] = new double[n.getConnectivity()];
			nodes[i] = n;
			n.setId(i);
			i++;
		}

		for (Edge e : edges) {
			AwpNode x = e.getX();
			AwpNode y = e.getY();
			int xi = x.getId();
			int yi = y.getId();
			double rmsd = e.getRmsd();
			neighbors[xi][counts[xi]] = y;
			rmsds[xi][counts[xi]] = rmsd;
			counts[xi]++;
			assert counts[xi] != 0;
			neighbors[yi][counts[yi]] = x;
			rmsds[yi][counts[yi]] = rmsd;
			counts[yi]++;
		}
	}

	public AwpNode[] getNodes() {
		return nodes;
	}

	public int size() {
		return nodes.length;
	}

	public AwpNode[] getNeighbors(AwpNode n) {
		return neighbors[n.getId()];
	}

	public double[] getRmsds(AwpNode n) {
		return rmsds[n.getId()];
	}

}
