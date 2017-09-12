package vectorhash;

import java.util.Random;

public class Vector {

	private int dim = 1000;
	private static Random random = new Random(1);
	public final byte[] coords = new byte[dim];
	private int hash;

	public Vector() {
		for (int i = 0; i < dim; i++) {
			coords[i] = (byte) random.nextInt(10);
		}		
		for (int i = 0; i < dim; i++) {
			hash = 31 * hash + coords[i];
		}		
	}

	@Override
	public boolean equals(Object obj) {
		Vector other = (Vector) obj;
		for (int i = 0; i < dim; i++) {
			if (coords[i] != other.coords[i]) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		return hash;
	}

}
