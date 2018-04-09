package embedding.lipschitz;

/**
 *
 * @author Antonin Pavelka
 */
public class ObjectPair {

	public final Similar a;
	public final Similar b;
	public final double rmsd;

	public ObjectPair(Similar a, Similar b, double rmsd) {
		this.a = a;
		this.b = b;
		this.rmsd = rmsd;
	}
}
