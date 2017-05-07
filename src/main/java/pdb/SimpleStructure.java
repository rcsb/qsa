package pdb;

import geometry.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

/**
 *
 * @author Antonin Pavelka
 */
public class SimpleStructure implements Serializable {

	private static final long serialVersionUID = 1L;
	private final String id_;
	private final SortedMap<ChainId, SimpleChain> chains = new TreeMap<>();
	private Map<ResidueId, Residue> residues;

	/*
	 * Just for benchmarking data composed of single chains.
	 */
	public SimpleStructure(CompactStructure cs) {
		this.id_ = cs.getId().getPdb();
		SimpleChain c = new SimpleChain(ChainId.createEmpty(), cs.getPoints());
		chains.put(c.getId(), c);
		getResidues();
	}

	public SimpleStructure(String pdbCode) {
		this.id_ = pdbCode;
	}

	public SimpleStructure(SimpleStructure s) {
		id_ = s.id_;
		for (ChainId ci : s.chains.keySet()) {
			chains.put(ci, new SimpleChain(s.chains.get(ci)));
		}
	}

	public Point getCenter() {
		Point center = new Point(0, 0, 0);
		int n = 0;
		for (Residue r : getResidues().values()) {
			center = center.plus(new Point(r.getCoords()));
			n++;
		}
		return center.divide(n);
	}

	public int numberOfChains() {
		return chains.size();
	}

	public int size() {
		return getResidues().size();
	}

	public String getPdbCode() {
		return id_;
	}

	public SimpleChain getFirstChain() {
		return chains.get(chains.firstKey());
	}

	public void add(ChainId c, Residue r) {
		SimpleChain sc;
		if (!chains.containsKey(c)) {
			sc = new SimpleChain(c);
			chains.put(c, sc);
		} else {
			sc = chains.get(c);
		}
		sc.add(r);
	}

	public void addChain(ChainId id, SimpleChain chain) {
		chains.put(id, chain);
	}

	public Collection<SimpleChain> getChains() {
		return chains.values();
	}

	public Set<ChainId> getChainIds() {
		return chains.keySet();
	}

	/**
	 * If the name c is not unique, returns the id that is first alphabetically.
	 */
	public ChainId getRandomChain(Random random) {
		List<ChainId> list = new ArrayList<>();
		list.addAll(this.chains.keySet());
		ChainId c = list.get(random.nextInt(list.size()));
		return c;
	}

	// TODO add sequence field to chainID
	// add fields seq here
	// search for greatest match by shifting strings or needleman
	public ChainId getChainIdWithNameIdealistic(char c) {
		if (c == '_') {
			return chains.firstKey();
		}
		c = Character.toUpperCase(c);
		SortedSet<ChainId> match = new TreeSet<>();
		for (ChainId cid : chains.keySet()) {
			if (cid.getName().toUpperCase().charAt(0) == c) {
				match.add(cid);
			}
		}
		SimpleChain sc = chains.get(match.first());
		return sc.getId();
	}

	public void removeChainsExcept(ChainId c) {
		HashSet<ChainId> keys = new HashSet<>(chains.keySet());
		for (ChainId k : keys) {
			if (!c.equals(k)) {
				chains.remove(k);
			}
		}
	}

	public Point3d[] getPoints() {
		List<Point3d> ps = new ArrayList<>();
		for (SimpleChain c : chains.values()) {
			ps.addAll(Arrays.asList(c.getPoints()));
		}
		Point3d[] a = new Point3d[ps.size()];
		ps.toArray(a);
		return a;
	}

	public Map<ResidueId, Residue> getResidues() {
		if (residues == null) {
			residues = new HashMap<>();
			for (SimpleChain sc : chains.values()) {
				for (Residue r : sc.getResidues()) {
					residues.put(r.getId(), r);
				}
			}
		}
		return residues;
	}

	public Residue getResidue(ResidueId rid) {
		return getResidues().get(rid);
	}

	public void transform(Matrix4d m) {
		for (Residue r : getResidues().values()) {
			r.transform(m);
		}
	}

}
