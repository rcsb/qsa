package spark.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Cluster implements Serializable {
	private List<Clusterable> fs = new ArrayList<>();
	private Clusterable core;

	public Cluster(Clusterable f) {
		core = f;
		fs.add(f);
	}

	public boolean belongs(Clusterable f) {
		return core.distance(f) < 0.9;
	}

	public boolean similar(Cluster c) {
		return belongs(c.core);
	}

	public void add(Clusterable f) {
		fs.add(f);
	}
	
	public void addAll(Cluster c) {
		fs.addAll(c.fs);
	}
}
