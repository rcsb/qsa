package org.rcsb.mmtf.benchmark;

/**
 *
 * @author Antonin Pavelka
 */
public class Counter {

	long count;
	long total;

	public Counter() {
	}

	public Counter(long total) {
		this.total = total;
	}

	public void next() {
		count++;
		if (count % 10000 == 0) {
			System.out.println(count);
		}
	}
}
