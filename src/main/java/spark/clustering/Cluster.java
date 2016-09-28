package spark.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Cluster implements Serializable {
	private List<Clusterable> fs = new ArrayList<>();
	private Clusterable core;
	private static Random random = new Random(1);

	public Cluster(Clusterable f) {
		core = f;
		fs.add(f);
	}

	public boolean belongs(Clusterable f) {
		return core.distance(f) < 0.01;
	}

	public boolean similar(Cluster c) {
		return belongs(c.core);
	}

	public double distance(Cluster other) {
		return core.distance(other.core);
	}

	public void add(Clusterable f) {
		fs.add(f);
	}

	public void addAll(Cluster c) {
		fs.addAll(c.fs);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			Clusterable f = fs.get(random.nextInt(fs.size()));
			sb.append(f.toString()).append(" ");
		}
		return sb.toString();
	}
}
