package fragments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import fragments.clustering.RankedResiduePair;
import fragments.clustering.ResiduePairs;
import java.util.HashMap;
import java.util.Map;
import pdb.Residue;
import pdb.ResidueId;

public class AwpCluster {

	public final int id;
	private List<AwpNode> nodes = new ArrayList<>();
	private AwpClustering clustering;
	// A points to word pair B that was used to add A to the cluster 
	private Map<AwpNode, AwpNode> link = new HashMap<>();
	private Debugger debug = new Debugger();

	public AwpCluster(int id, AwpNode node, AwpClustering clustering) {
		this.id = id;
		nodes.add(node);
		debug.add(node);
		this.clustering = clustering;
		this.clustering.add(this);
	}

	public Debugger getDebugger() {
		return debug;
	}

	public int getId() {
		return id;
	}

	public int size() {
		return nodes.size();
	}

	public List<AwpNode> getNodes() {
		return nodes;
	}

	public AwpClustering getClustering() {
		return clustering;
	}

	public void add(AwpCluster other) {
		debug.add(other.debug);
		this.nodes.addAll(other.nodes);
		for (AwpNode n : other.link.keySet()) {
			link.put(n, other.link.get(n));
		}
	}

	/**
	 * For each AwpNode, remember why was it added. Used later to prevent problems when one word has
	 * ambiguous match (e.g. slightly rotated alpha helix still has good RMSD, but RMSD of 3 words
	 * it connects can be terrible).
	 */
	public void connectWords(AwpNode a, AwpNode b) {
		if (!link.containsKey(a)) {
			link.put(a, b);
		}
		if (!link.containsKey(b)) {
			link.put(b, a);
		}
	}

	public AwpNode getLinked(AwpNode n) {
		return link.get(n);
	}

	public void replaceBy(AwpCluster other) {
		for (AwpNode n : nodes) {
			n.setClusterId(other.getId());
		}
	}

	@Override
	public boolean equals(Object o) {
		AwpCluster other = (AwpCluster) o;
		return id == other.id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		return id + ": " + size();
	}

	public Residue[][] computeAlignment() {
		ResiduePairs a = new ResiduePairs();
		for (AwpNode awp : nodes) {
			Residue[] x = awp.getWords()[0].getResidues();
			Residue[] y = awp.getWords()[1].getResidues();
			for (int i = 0; i < x.length; i++) {
				Residue xi = x[i];
				Residue yi = y[i];
				a.add(xi, yi, awp.getRmsd());
			}
		}
		Set<Residue> usedX = new HashSet<>();
		Set<Residue> usedY = new HashSet<>();
		List<Residue[]> aln = new ArrayList<>();
		for (RankedResiduePair rrp : a.values()) {
			Residue x = rrp.getX();
			Residue y = rrp.getY();
			if (!usedX.contains(x) && !usedY.contains(y)) {
				usedX.add(x);
				usedY.add(y);
				Residue[] p = {x, y};
				aln.add(p);
			}
		}
		Residue[][] alignment = new Residue[2][aln.size()];
		for (int i = 0; i < aln.size(); i++) {
			alignment[0][i] = aln.get(i)[0];
			alignment[1][i] = aln.get(i)[1];
		}
		return alignment;
	}

}
