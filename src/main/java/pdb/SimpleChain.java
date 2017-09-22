package pdb;

import fragments.Parameters;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
	private double[] phi, psi;

	public SimpleChain() {
	}

	public SimpleChain(ChainId chainId, Residue[] residues) {
		this.cid = chainId;
		this.residues = residues;
		/*for (int i = 1; i < residues.length - 1; i++) {
			Residue a = residues[i - 1];
			Residue b = residues[i];
			Residue c = residues[i + 1];
		}*/
	}

	public SimpleChain(ChainId cid, Point3d[] centers) {
		this.cid = cid;
		residues = new Residue[centers.length];
		for (int i = 0; i < centers.length; i++) {
			Point3d x = centers[i];
			if (x != null) {
				ResidueId rid = new ResidueId(cid, i + 1);
				residues[i] = new Residue(rid, i, x);
			}
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

	/**
	 * @return Word defined by center neighbors
	 * @param shift means further neighbors, 0 is tight neibors, 1 is gap 1 012345 0123456789 uxoxu||||| uxoxu
	 */
	public List<Residue> getNext(Residue center, int shift) {
		List<Residue> next = new ArrayList<>();
		int i = getIndex(center);
		int d = params.getWordLength() + shift;
		int a = i - d;
		if (a >= 0) {
			next.add(residues[a]);
		}
		int b = i + d;
		if (b < residues.length) {
			next.add(residues[b]);
		}
		return next;
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

	public Residue[] getResidues() {
		return residues;
	}

	public int size() {
		return residues.length;
	}
}
