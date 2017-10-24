package util;

import java.util.ArrayList;
import java.util.List;

public class Mayhem {

	/*
     * Estimates how much memory was available at the time of calling, throws
     * OutOfMemoryError in the end.
	 */
	public static void mayhem() {
		int MB = 1000000 / 4;
		int x = 10;
		List list = new ArrayList();
		for (int i = 0; i < 1000; i++) {
			list.add(new int[x * MB]);
			// for murtagh clustering, see Clustering.estimateMatrixSize()
			long bytes = ((long) i + 1) * x * 1000 * 1000;
			int matrixSize = (int) Math.floor(
				(1 + Math.sqrt(1 + bytes * 2)) / 2);
			System.out.println(((i + 1) * x)
				+ " MB allocated (murtaghMatrixSize "
				+ matrixSize + ")");
		}

	}
}
