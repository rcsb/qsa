package embedding.lipschitz;

import fragment.cluster.Fragment;

/**
 *
 * @author Antonin Pavelka
 */
public class FragmentPair {

	public final Fragment a;
	public final Fragment b;
	public final double rmsd;

	public FragmentPair(Fragment a, Fragment b, double rmsd) {
		this.a = a;
		this.b = b;
		this.rmsd = rmsd;
	}
}
