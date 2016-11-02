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
	private ChainId cid;
	private static final long serialVersionUID = 1L;
	private List<Residue> residues = new ArrayList<>();

	public SimpleChain(ChainId c) {
		this.cid = c;
	}

	public SimpleChain(ChainId cid, Point3d[] centers) {
		this.cid = cid;
		for (int i = 0; i < centers.length; i++) {
			Point3d x = centers[i];
			if (x != null) {
				ResidueId rid = new ResidueId(cid, i + 1);
				residues.add(new Residue(rid, x));
			}
		}
	}

	public ChainId getId() {
		return cid;
	}

	public void add(Residue r) {
		residues.add(r);
	}

	public List<Residue> getResidues() {
		List<Residue> list = new ArrayList<>();
		list.addAll(residues);
		return list;
	}

	public Point3d[] getPoints() {
		List<Point3d> list = new ArrayList<>();
		for (Residue r : residues) {
			Point3d x = r.getPosition3d();
			list.add(x);
		}
		Point3d[] a = new Point3d[list.size()];
		list.toArray(a);
		return a;
	}

	public int size() {
		return residues.size();
	}
}
