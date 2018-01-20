package cath;

import global.io.Directories;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Antonin Pavelka
 *
 * Class (C), architecture (A), topology (T) and homologous superfamily (H).
 *
 * This class should provide important levels (T and H) and convert domain IDs (StructureSource) to residues of the
 * domain (Domain objects)
 *
 */
public class Cath {

	private final Directories dirs;
	private final Level families = new Level();

	//private Map<String, Domain> domains = new HashMap<>(); // 7 letter id (3ryhD03)
	/*private final List<String> topologies = new ArrayList<>();
	private final List<String> homologies = new ArrayList<>();
	private final Map<StructureSource, Classification> domainNameToCategory = new HashMap<>();
	private final Map<String, String> pdbToDomainName = new HashMap<>();
	private final Map<String, String> categoryToName = new HashMap<>();
	private final List<String> unclassified = new ArrayList<>();*/
	Map<String, Domain> domains = new HashMap<>();

	public Cath(Directories dirs) {
		this.dirs = dirs;
		parse();
		build();
	}

	public Level getHomologousSuperfamilies() {
		return families;
	}

	private void parse() {
		String line = null;
		DomainParser parser = new DomainParser();
		try (BufferedReader br = new BufferedReader(new FileReader(dirs.getCathDomains()))) {
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				}
				Domain domain = parser.parse(line);
				domains.put(domain.getId(), domain);
			}
		} catch (IOException ex) {
			throw new RuntimeException(line, ex);
		}
	}

	private void build() {
		for (Domain domain : domains.values()) {
			families.addMember(domain.getClassification(), domain);
		}
	}

	public Domain getDomain(String id) {
		return domains.get(id);
	}

	/*public Classification getClassification(StructureSource domain) {
		return domainNameToCategory.get(domain);
	}

	public Domain getDomain(StructureSource source) {
		String domainId = source.getCathDomainId();
		assert domains.containsKey(domainId) : domainId;
		return domains.get(domainId);
	}

	public void printPdbClassifications(String pdbCode) {
		String domainName = getDomainNameForPdb(pdbCode);
		Classification category = getCategoryForDomain(domainName);
		String name = categoryToName.get(category);
	}

	private String getDomainNameForPdb(String pdbCode) {
		return pdbToDomainName.get(pdbCode);
	}

	private Classification getCategoryForDomain(String domainName) {
		return domainNameToCategory.get(domainName);
	}*/
}
