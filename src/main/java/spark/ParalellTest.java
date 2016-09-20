package spark;

import java.util.stream.IntStream;

public class ParalellTest {
	public static void main(String[] args) {
		long time1 = System.nanoTime();
		IntStream.range(0, 10).parallel().forEach(nbr -> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
			}
			System.out.println(nbr);
		});
		long time2 = System.nanoTime();
		long time = (time2 - time1) / 1000000;
		System.out.println("Finished in " + time + " ms.");
	}

}
