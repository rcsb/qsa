package spark.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Clustering implements Serializable {
	private List<Cluster> cs = new ArrayList<>();

	public int size() {
		return cs.size();
	}

	public void add(Clusterable f) {
		boolean added = false;
		for (Cluster c : cs) {
			if (c.belongs(f)) {
				c.add(f);
				added = true;
				break;
			}
		}
		if (!added) {
			cs.add(new Cluster(f));
		}
	}

	public boolean isEmpty() {
		return cs.isEmpty();
	}

	public void clear() {
		cs.clear();
	}

	public void add(Cluster c) {
		cs.add(c);
	}

	public List<Cluster> get() {
		return cs;
	}

	public void addAll(Clustering other) {
		cs.addAll(other.cs);
	}

	public Clustering merge(Clustering other) {
		Clustering a, b;
		if (this.size() <= other.size()) {
			a = this;
			b = other;
		} else {
			a = other;
			b = this;
		}
		// adding list in ac to bc, result
		for (Cluster ac : a.get()) {
			boolean added = false;
			for (Cluster bc : b.get()) {
				if (ac.similar(bc)) {
					bc.addAll(ac);
					added = true;
				}
			}
			if (!added) {
				b.add(ac);
			}
		}
		return b;
	}
}
