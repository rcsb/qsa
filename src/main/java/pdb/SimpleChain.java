package pdb;

import fragments.Parameters;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 */
public class SimpleChain implements Serializable {

	private static final long serialVersionUID = 1L;
	private static Parameters params = Parameters.create();
	private ChainId cid;
	private Residue[] residues;

	public SimpleChain() {
	}

	public SimpleChain(ChainId chainId, Residue[] residues) {
		this.cid = chainId;
		this.residues = residues;
		for (int i = 1; i < residues.length; i++) {
			residues[i].setPrevious(residues[i - 1]);
		}
		for (int i = 1; i < residues.length; i++) {
			residues[i - 1].setNext(residues[i]);
		}
	}

	public SimpleChain(SimpleChain sc) {
		cid = sc.cid;
		residues = new Residue[sc.residues.length];
		for (int i = 0; i < residues.length; i++) {
			residues[i] = new Residue(sc.residues[i]);
		}
	}

	// NOW create the residues and pairs over each chain, store it and use it
	/**
	 * @return Centers of overlapping words and neighboring words (cause this is just for contact evaluation, neighbors
	 * are not evaluated by contacts)
	 */
	public Set<Residue> getForbidden(Residue center) {
		Set<Residue> f = new HashSet<>();
		int i = getIndex(center);
		for (int d = 0; d <= params.getWordLength(); d++) {
			int a = i - d;
			if (a >= 0) {
				f.add(residues[a]);
			}
			int b = i + d;
			if (b < residues.length) {
				f.add(residues[b]);
			}
		}
		return f;
	}

	public Integer getIndex(Residue residue) {
		for (int i = 0; i < residues.length; i++) {
			if (residue.equals(residues[i])) {
				return i;
			}
		}
		throw new RuntimeException("Residue " + residue + " not found.");
	}

	public ChainId getId() {
		return cid;
	}

	public char getSingleLetterId() {
		String s = getId().getName();
		if (s.length() > 1) {
			throw new RuntimeException(s + " too long");
		}
		return s.charAt(0);
	}

	public Residue[] getResidues() {
		return residues;
	}

	public int size() {
		return residues.length;
	}
}
