package pdb;

import java.io.Serializable;

/**
 *
 * @author Antonin Pavelka
 */
public class SimpleChain implements Serializable {

	private static final long serialVersionUID = 1L;
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
