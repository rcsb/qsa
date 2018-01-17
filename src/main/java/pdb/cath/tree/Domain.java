package pdb.cath.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import pdb.ResidueId;
import pdb.cath.Segment;

/**
 *
 * @author Antonin Pavelka
 *
 * Represents domain of a protein, i.e., series of sequential segments.
 *
 * Can be sorted according to domain size. The size estimation uses only boundary residues, therefore it is not accurate
 * when insertion codes are present or residues are missing in structure.
 *
 */
public class Domain implements Comparable<Domain> {

	private final String id;
	private final List<Segment> segments = new ArrayList<>();
	private final Classification classification;
	private int size;

	public Domain(String id, Classification classification) {
		this.id = id;
		this.classification = classification;
	}

	public String getId() {
		return id;
	}

	public Classification getClassification() {
		return classification;
	}

	public void addSegment(Segment segment) {
		segments.add(segment);
		size += segment.size();
	}

	public boolean doesResidueBelong(ResidueId residueId) {
		for (Segment segment : segments) {
			if (segment.contains(residueId)) {
				return true;
			}
		}
		return false;
	}

	public int size() {
		return size;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + Objects.hashCode(this.id);
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		Domain other = (Domain) o;
		return this.id.equals(other.id);
	}

	@Override
	public int compareTo(Domain other) {
		return Integer.compare(this.size(), other.size());
	}
}
