package algorithm.primitives;

/**
 *
 * @author Antonin Pavelka
 */
public class MatrixTriangular {

	private final float[][] data;
	private int size;

	public MatrixTriangular(int size) {
		this.size = size;
		data = new float[size][];
		for (int x = 0; x < size; x++) {
			data[x] = new float[x];
		}
	}

	public void set(int x, int y, float value) {
		if (x == y) {
			throw new RuntimeException("Diagonal cannot be set");
		}
		if (y > x) {
			int temp = x;
			x = y;
			y = temp;
		}
		data[x][y] = value;
	}

	public float get(int x, int y) {
		if (x == y) {
			throw new RuntimeException("Diagonal cannot be queried");
		}
		if (y > x) {
			int temp = x;
			x = y;
			y = temp;
		}
		return data[x][y];
	}

	public int size() {
		return size;
	}
	
}
