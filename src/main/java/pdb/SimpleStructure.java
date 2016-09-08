package pdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.vecmath.Point3d;

import spark.Alignable;

/**
 *
 * @author Antonin Pavelka
 */
public class SimpleStructure implements Alignable {

	private PdbChain id_;
	private SortedMap<ChainId, SimpleChain> chains = new TreeMap<>();

	private SimpleStructure() {

	}

	public int size() {
		int size = 0;
		for (SimpleChain c : chains.values()) {
			size += c.size();
		}
		return size;
	}

	/*
	 * Just for benchmarking data composed of single chains.
	 */
	public SimpleStructure(CompactStructure cs) {
		id_ = cs.getId();
		SimpleChain c = new SimpleChain(cs.getPoints());
		chains.put(new ChainId("A"), c);
	}

	public SimpleStructure(PdbChain id) {
		id_ = id;
	}

	public PdbChain getId() {
		return id_;
	}

	public SimpleChain getFirstChain() {
		return chains.get(chains.firstKey());
	}

	public void add(ChainId c, Residue r) {
		SimpleChain sc;
		if (!chains.containsKey(c)) {
			sc = new SimpleChain();
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

	public SimpleChain getChainByName(char c) {
		if (c == '_') {
			return chains.get(chains.firstKey());
		}
		c = Character.toUpperCase(c);
		SortedSet<ChainId> match = new TreeSet<>();
		for (ChainId cid : chains.keySet()) {
			if (cid.getName().toUpperCase().charAt(0) == c) {
				match.add(cid);
			}
		}
		SimpleChain sc = chains.get(match.first());
		return sc;
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

}
