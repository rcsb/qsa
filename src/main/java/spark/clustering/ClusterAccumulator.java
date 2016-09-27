package spark.clustering;

import org.apache.spark.util.AccumulatorV2;

public class ClusterAccumulator extends AccumulatorV2<Clusterable, Clustering> {

	private static final long serialVersionUID = 1L;
	private Clustering clustering = new Clustering();

	@Override
	public void add(Clusterable f) {
		synchronized (this) {
			clustering.add(f);
		}
	}

	/*
	 * WHY should it be copied?
	 */

	@Override
	public AccumulatorV2<Clusterable, Clustering> copy() {
		/*if (true) {
			throw new RuntimeException("NEVER");
		}*/
		return this;
	}

	@Override
	public boolean isZero() {
		return clustering.isEmpty();
	}

	@Override
	public void merge(AccumulatorV2<Clusterable, Clustering> other) {
		synchronized (this) {
			clustering = value().merge(other.value());
		}
	}

	/* WHY??? */
	@Override
	public void reset() {
		clustering.clear();
	//	throw new RuntimeException("NEVER");
	}

	@Override
	public Clustering value() {
		return clustering;
	}

}
