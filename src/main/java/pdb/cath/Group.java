package pdb.cath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pdb.StructureSource;
import pdb.cath.tree.Classification;
import pdb.cath.tree.Domain;

/**
 *
 * @author Antonin Pavelka
 */
public class Group {

	private final Classification classification;
	private final List<Domain> domains = new ArrayList<>(); // full information about domains, CATH specific
	private Domain representant;

	public Group(Classification classification) {
		this.classification = classification;
	}

	public void setRepresentant(Domain representant) {
		this.representant = representant;
	}

	public void addDomain(Domain domain) {
		domains.add(domain);
	}

	public Domain getRepresentant() {
		if (representant == null) {
			representant = findRepresentant();
		}
		return representant;
	}

	private Domain findRepresentant() {
		Collections.sort(domains);
		Domain medianDomain = domains.get(domains.size() / 2);
		int median = medianDomain.size();
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (Domain domain : domains) {
			int size = domain.size();
			if (size < min) {
				min = size;
			}
			if (size > max) {
				max = size;
			}
		}
		System.out.println(min + " " + median + " " + " " + max + " (min median max) in CATH group " + classification);
		return medianDomain;

	}

	public List<Domain> getMemberDoamains() {
		return domains;
	}

	public List<StructureSource> getMemberSources() {
		List<StructureSource> sources = new ArrayList<>();
		for (Domain domain : domains) {
			StructureSource source = new StructureSource(domain.getId());
			sources.add(source);
		}
		return sources;
	}

	public Classification getClassification() {
		return classification;
	}
}
