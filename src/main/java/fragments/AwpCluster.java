package fragments;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.exception.NoDataException;

public class AwpCluster {
	private int id;
	private int size;
	private List<AwpNode> nodes = new ArrayList<>();
	private AwpClustering clustering;

	public AwpCluster(int id, AwpNode node, AwpClustering clustering) {		
		this.id = id;		
		size = 1;
		nodes.add(node);
		this.clustering = clustering;
		this.clustering.add(this);
	}

	public int getId() {
		return id;
	}

	public int size() {
		return size;
	}
		
	public AwpClustering getClustering() {
		return clustering;
	}

	public void add(AwpCluster other) {
		this.size += other.size;
		this.nodes.addAll(other.nodes);
	}
	
	public void replaceBy(AwpCluster other) {
		for (AwpNode n : nodes) {
			n.setClusterId(other.getId());
		}
	}

	public String toString() {
		return id + ": ";// + xs + " " + ys;
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
}
