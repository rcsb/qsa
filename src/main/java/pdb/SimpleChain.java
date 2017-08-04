package pdb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

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

	public SimpleChain(ChainId c, Residue[] residues) {
		this.cid = c;
		this.residues = residues;
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
