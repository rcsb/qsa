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

	private int id;
	private List<AwpNode> nodes = new ArrayList<>();
	private AwpClustering clustering;
	// A points to word pair B that was used to add A to the cluster 
	private Map<AwpNode, AwpNode> link = new HashMap<>();

	public AwpCluster(int id, AwpNode node, AwpClustering clustering) {
		this.id = id;
		nodes.add(node);
		this.clustering = clustering;
		this.clustering.add(this);
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
		this.nodes.addAll(other.nodes);
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

	public ResidueId[][] computeAlignment() {
		ResiduePairs a = new ResiduePairs();
		for (AwpNode awp : nodes) {
			Residue[] x = awp.getWords()[0].getResidues();
			Residue[] y = awp.getWords()[1].getResidues();
			for (int i = 0; i < x.length; i++) {
				ResidueId xi = x[i].getId();
				ResidueId yi = y[i].getId();
				a.add(xi, yi, awp.getRmsd());
			}
		}
		Set<ResidueId> usedX = new HashSet<>();
		Set<ResidueId> usedY = new HashSet<>();
		List<ResidueId[]> aln = new ArrayList<>();
		for (RankedResiduePair rrp : a.values()) {
			ResidueId x = rrp.getX();
			ResidueId y = rrp.getY();
			if (!usedX.contains(x) && !usedY.contains(y)) {
				usedX.add(x);
				usedY.add(y);
				ResidueId[] p = {x, y};
				aln.add(p);
			}
		}
		ResidueId[][] alignment = new ResidueId[2][aln.size()];
		for (int i = 0; i < aln.size(); i++) {
			alignment[0][i] = aln.get(i)[0];
			alignment[1][i] = aln.get(i)[1];
		}

		if (alignment[0].length > 80) {
			for (int i = 0; i < alignment[0].length; i++) {
				System.out.println(alignment[0][i] + " - " + alignment[1][i]);
			}
			System.out.println("");
		}
		return alignment;
	}

}
