package algorithm.primitives;

/**
 *
 * @author Antonin Pavelka
 */
public class TriangularMatrix {

	private final float[][] data;
	private int size;

	public TriangularMatrix(int size) {
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

	public static void main(String[] args) {
		TriangularMatrix m = new TriangularMatrix(3);
		m.set(0, 1, 01);
		m.set(1, 0, 10);
		m.set(2, 0, 20);
		m.set(2, 1, 21);
		m.set(0, 2, 02);
		System.out.println(m.get(0, 1));
		System.out.println(m.get(0, 2));
		System.out.println(m.get(2, 0));
		System.out.println(m.get(1, 0));
		System.out.println(m.get(1, 0) == m.get(0, 1));
		System.out.println(m.get(1, 2) == 21);
	}
}
