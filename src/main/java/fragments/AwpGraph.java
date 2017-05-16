package fragments;

import java.util.List;
import java.util.Set;
import pdb.SimpleStructure;

/**
 * @author Antonin Pavelka
 *
 */
public class AwpGraph {

	public final SimpleStructure structure;
	private final AwpNode[][] neighbors;
	private final double[][] rmsds;
	private final AwpNode[] nodes;

	public AwpGraph(SimpleStructure structure, Set<AwpNode> nodeSet, List<Edge> edges) {
		this.structure = structure;
		neighbors = new AwpNode[nodeSet.size()][];
		rmsds = new double[nodeSet.size()][];
		nodes = new AwpNode[neighbors.length];
		int[] counts = new int[neighbors.length];
		int i = 0;
		for (AwpNode n : nodeSet) {
			neighbors[i] = new AwpNode[n.getConnectivity()];
			rmsds[i] = new double[n.getConnectivity()];
			nodes[i] = n;
			n.id = i;
			i++;
		}

		for (Edge e : edges) {
			AwpNode x = e.getX();
			AwpNode y = e.getY();
			int xi = x.id;
			int yi = y.id;
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

	public AwpNode[] getNeighbors(AwpNode n) {
		return neighbors[n.id];
	}

	public double[] getRmsds(AwpNode n) {
		return rmsds[n.id];
	}
}
