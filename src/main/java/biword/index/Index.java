package biword.index;

import algorithm.Biword;
import grid.sparse.MultidimensionalArray;
import grid.sparse.BufferOfLong;

/**
 * In memory index and biword database.
 */
public class Index {

	private double[] globalMin;
	private double[] globalMax;
	private int bracketN;
	private int biwordN;
	private MultidimensionalArray grid;
	private BufferOfLong out;
	private float[] box;

	Index() {

	}

	Index(int dimensions, int bins, int biwordN, float[] box, double[] globalMin, double[] globalMax) {
		this.bracketN = bins;
		this.box = box;
		this.biwordN = biwordN;
		this.globalMin = globalMin;
		this.globalMax = globalMax;
		this.out = new BufferOfLong(biwordN);
		this.grid = new MultidimensionalArray(dimensions, bins, biwordN);
		for (int i = 0; i < 4; i++) { // angles are cyclic - min and max values are neighbors
			this.grid.setCycle(i);
		}
	}

	void insert(Biword bw) {
		float[] v = bw.getSmartVector();
		if (v != null) {
			grid.insert(discretize(v), bw.getId().endcode());
		}
	}

	private void printBoundaries() {
		System.out.println("BOUNDARIES");
		for (int d = 0; d < globalMin.length; d++) {
			System.out.println(globalMin[d] + " - " + globalMax[d] + " | ");
		}
		System.out.println("----");
	}

	public BufferOfLong query(Biword bw) {
		float[] vector = bw.getSmartVector();
		int dim = vector.length;
		float[] min = new float[dim];
		float[] max = new float[dim];
		for (int i = 0; i < dim; i++) {
			min[i] = vector[i] - box[i];
			max[i] = vector[i] + box[i];
		}
		out.clear();
		grid.getRange(discretize(min), discretize(max), out);
		return out;
	}

	private byte[] discretize(float[] x) {
		byte[] indexes = new byte[x.length];
		for (int i = 0; i < x.length; i++) {
			float v = x[i];
			int index = (int) Math.floor((v - globalMin[i]) / (globalMax[i] - globalMin[i]) * bracketN);
			if (index < Byte.MIN_VALUE || index > Byte.MAX_VALUE) {
				throw new RuntimeException();
			}
			indexes[i] = (byte) index;
		}
		return indexes;
	}
}
