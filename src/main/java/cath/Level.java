package cath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pdb.StructureSource;
import cath.Group;

/**
 *
 * Represents one level of CATH database tree. Contains all groups in that level, e.g. all homologous superfamilies.
 *
 * @author Antonin Pavelka
 */
public class Level {

	private Map<Classification, Group> groups = new HashMap<>();

	public void addRepresentant(Classification classification, Domain representant) {
		Group group = findOrCreateGroup(classification);
		group.setRepresentant(representant);
	}

	public void addMember(Classification classification, Domain domain) {
		Group group = findOrCreateGroup(classification);
		group.addDomain(domain);
	}

	public Group getGroup(Classification classification) {
		return groups.get(classification);
	}

	public Collection<Group> getRepresentants() {
		return groups.values();
	}

	public Set<Classification> getGroupClasses() {
		return groups.keySet();
	}

	public List<StructureSource> getRepresentantSources() {
		List<StructureSource> representants = new ArrayList<>();
		for (Group group : getRepresentants()) {
			Domain domain = group.getRepresentant();
			try {
				StructureSource source = new StructureSource(domain.getId());
				representants.add(source);
			} catch (Exception ex) {
				throw new RuntimeException(group.getClassification().toString());
			}
		}
		return representants;
	}

	private Group findOrCreateGroup(Classification classification) {
		Group group = groups.get(classification);
		if (group == null) {
			group = new Group(classification);
			groups.put(classification, group);
		}
		return group;
	}

}
