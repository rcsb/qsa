package fragments.alignment;

import fragments.AwpGraph;
import fragments.AwpNode;
import fragments.Edge;
import fragments.Word;
import fragments.clustering.ResiduePair;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import pdb.Residue;

public class ExpansionAlignment implements Alignment {

	private final AwpGraph graph;
	private final Set<AwpNode> nodes = new HashSet<>();
	private final Map<Residue, Residue> residuesA = new HashMap<>();
	private final Map<Residue, Residue> residuesB = new HashMap<>();
	private final Map<ResiduePair, Double> rmsds = new HashMap<>();
	private final PriorityQueue<Edge> queue = new PriorityQueue<>();

	public ExpansionAlignment(AwpNode origin, AwpGraph graph) {
		this.graph = graph;
		add(origin, null);
		expand();
	}

	private void add(AwpNode node, Double rmsd) {
		nodes.add(node);
		saveResiduePairing(node, rmsd);
		List<Edge> edges = graph.getConnections(node);
		if (edges != null) {
			queue.addAll(edges);
		} else { // does it happen for some good reason or is it a bug?
		}
	}

	private void expand() {
		while (!queue.isEmpty()) {
			Edge e = queue.poll();
			AwpNode x = e.getX();
			AwpNode y = e.getY();
			if (nodes.contains(y)) {
				continue;
			}
			assert nodes.contains(x);
			// let's add y
			if (isConsistent(y)) {
				add(y, e.getRmsd());
			}
		}
	}

	public final void saveResiduePairing(AwpNode node, Double rmsd) {
		Word[] ws = node.getWords();
		Residue[] ras = ws[0].getResidues();
		Residue[] rbs = ws[1].getResidues();
		int n = ras.length;
		for (int i = 0; i < n; i++) {
			Residue ra = ras[i];
			Residue rb = rbs[i];
			assert residuesA.size() == residuesB.size();
			residuesA.put(ra, rb);
			residuesB.put(rb, ra);
			assert residuesA.size() == residuesB.size();
			if (rmsd != null) { // null only for the first node, values will be added 
				rmsds.put(new ResiduePair(ra, rb), rmsd);
			}
		}
	}

	/**
	 * Checks if the node does not assign a word differently than some node of the cluster.
	 *
	 * @return true iff the new word pairing defined by node is consistent with pairings defined by
	 * nodes already in this cluster, i.e. Guarantees
	 */
	public final boolean isConsistent(AwpNode node) {
		Word[] ws = node.getWords(); // new word pairing
		Residue[] ras = ws[0].getResidues(); // word in protein A
		Residue[] rbs = ws[1].getResidues(); // matching word in protein B
		int n = ras.length;
		for (int i = 0; i < n; i++) {
			Residue ra = ras[i];
			Residue rb = rbs[i];
			Residue rbo = residuesA.get(ra); // existing match for word nwa
			if (rbo != null && !rbo.equals(rb)) { // if it was matched and the match is different
				return false; // one word would be paired with two different words
			} // now let's do the same in oposite direction
			Residue rao = residuesB.get(rb);
			if (rao != null && !rao.equals(ra)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Set<AwpNode> getNodes() {
		return nodes;
	}

	public int sizeInWords() {
		return nodes.size();
	}

	public int sizeInResidues() {
		//assert residuesA.size() == residuesB.size(); // clashes arises when clusters merge
		return residuesA.size();
	}

	@Override
	public double getScore() {
		double score = 0;
		for (double rmsd : rmsds.values()) {
			score += 1.0 / (1.0 + rmsd * rmsd);
		}
		return score;
	}

}
