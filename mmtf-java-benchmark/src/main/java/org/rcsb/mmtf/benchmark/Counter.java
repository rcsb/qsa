package org.rcsb.mmtf.benchmark;

/**
 *
 * @author Antonin Pavelka
 */
public class Counter {

	private long count;
	private long total;
	private int sparsity = 10000;

	public Counter() {
	}

	public Counter(int sparsity) {
		this.sparsity = sparsity;
	}

	public Counter(long total) {
		this.total = total;
	}

	public void next() {
		count++;
		if (count % sparsity == 0) {
			System.out.println(count);
		}
	}
}
