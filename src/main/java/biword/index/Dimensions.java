package biword.index;

/**
 *
 * @author Antonin Pavelka
 */
public class Dimensions {

	private boolean[] cyclic;

	public Dimensions(boolean... cyclic) {
		this.cyclic = cyclic;
	}

	public Dimensions merge(Dimensions other) {
		boolean[] newCyclic = new boolean[cyclic.length + other.cyclic.length];
		System.arraycopy(cyclic, 0, newCyclic, 0, cyclic.length);
		System.arraycopy(other.cyclic, 0, newCyclic, cyclic.length, other.cyclic.length);
		return new Dimensions(newCyclic);
	}

	public int number() {
		return cyclic.length;
	}

	public boolean isCyclic(int index) {
		return cyclic[index];
	}

}
