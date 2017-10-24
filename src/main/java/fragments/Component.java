package fragments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pdb.Residue;

/**
 *
 * @author Antonin Pavelka
 */
public class Component {

	//private final List<AwpNode> nodes = new ArrayList<>();
	//private final boolean[] a;
	//private final boolean[] b;
	private Set<Residue> as = new HashSet<>();
	private Set<Residue> bs = new HashSet<>();

	public Component(int queryResidues, int targetResidues) {
		//a = new boolean[queryResidues];
		//b = new boolean[targetResidues];
	}

	public void add(AwpNode n) {
		//nodes.add(n);
		Word x = n.getWords()[0];
		Word y = n.getWords()[1];

		for (Residue r : x.getResidues()) {
			//a[r.getIndex()] = true;
			as.add(r);
		}
		for (Residue r : y.getResidues()) {
			try {
				//b[r.getIndex()] = true;
				bs.add(r);
			} catch (Exception ex) {
				//System.err.println(a.length + " " + b.length + " " + r.getIndex());
				throw new RuntimeException(ex);

			}
		}
	}

	private int count(boolean[] bs) {
		int n = 0;
		for (boolean b : bs) {
			if (b) {
				n++;
			}
		}
		return n;
	}

	public int sizeInResidues() {
		//return Math.min(count(a), count(b));
		return Math.min(as.size(), bs.size());
	}
}
