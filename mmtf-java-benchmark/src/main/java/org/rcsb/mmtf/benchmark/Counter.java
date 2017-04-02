package org.rcsb.mmtf.benchmark;

/**
 *
 * @author Antonin Pavelka
 */
public class Counter {

	private long count;
	private long total;
	private long sparsity = 10000;
	private String name;

	public Counter(String name, int reportTimes, long total) {
		this.name = name;
		if (total != 0) {
			this.sparsity = total / reportTimes;
		}
		this.total = total;
	}

	public Counter setTotal(long total) {
		this.total = total;
		return this;
	}

	public void next() {
		count++;
		if (count % sparsity == 0) {
			System.out.println(name + ": " + count + " / " + total);
		}
	}
}
