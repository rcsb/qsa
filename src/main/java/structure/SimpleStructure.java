package structure;

import geometry.primitives.Point;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.vecmath.Matrix4d;

/**
 *
 * @author Antonin Pavelka
 */
public class SimpleStructure implements Serializable {

	private int id;
	private StructureSource source;
	private SortedMap<ChainId, SimpleChain> chains = new TreeMap<>();
	private Map<ResidueId, Residue> residues;

	public SimpleStructure() {
		// for Kryo
	}

	public SimpleStructure(int id, SimpleStructure s) {
		this.id = id;
		source = s.source;
		for (ChainId ci : s.chains.keySet()) {
			chains.put(ci, new SimpleChain(s.chains.get(ci)));
		}
	}

	public SimpleStructure(int id, StructureSource reference) {
		this.id = id;
		this.source = reference;
	}

	public void addChain(SimpleChain c) {
		if (chains.containsKey(c.getId())) {
			throw new RuntimeException(source + ":" + c.getId().getId());
		}
		chains.put(c.getId(), c);
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

	public StructureSource getSource() {
		return source;
	}

	public int getId() {
		return id;
	}

	public SimpleChain getFirstChain() {
		return chains.get(chains.firstKey());
	}

	public Collection<SimpleChain> getChains() {
		return chains.values();
	}

	public Set<ChainId> getChainIds() {
		return chains.keySet();
	}

	/**
	 * Using name, not id. Needed for some benchmarks.
	 *
	 * @param c
	 */
	public void removeChainsByNameExcept(ChainId c) {
		HashSet<ChainId> keys = new HashSet<>(chains.keySet());
		for (ChainId k : keys) {
			if (!c.getName().equals(k.getName())) {
				chains.remove(k);
			}
		}
	}

	public Map<ResidueId, Residue> getResidues() {
		if (residues == null) {
			residues = new HashMap<>();
			for (SimpleChain sc : chains.values()) {
				for (Residue r : sc.getResidues()) {
					assert !residues.containsKey(r.getId());
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
