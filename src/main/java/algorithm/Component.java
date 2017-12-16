package algorithm;

import biword.Fragment;
import algorithm.graph.AwpNode;
import java.util.HashSet;
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
	private int size = -1;

	public Component(int queryResidues, int targetResidues) {
		//a = new boolean[queryResidues];
		//b = new boolean[targetResidues];
	}

	public void add(AwpNode n) {
		if (size >= 0) {
			throw new IllegalStateException("Adding after size computation " + size);
		}
		//nodes.add(n);
		Fragment x = n.getWords()[0];
		Fragment y = n.getWords()[1];

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
		if (size < 0) {
			size = Math.min(as.size(), bs.size());
		}
		return size;
	}
}
