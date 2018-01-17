package pdb.cath;

import pdb.cath.tree.Domain;
import pdb.cath.tree.Classification;
import global.io.Directories;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import pdb.StructureSource;
import pdb.cath.tree.Level;

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
public class CathOldVersion {

	private final Directories dirs;
	private final Level families = new Level();

	private Map<StructureSource, Domain> domains = new HashMap<>(); // 7 letter id (3ryhD03)
	private final List<String> topologies = new ArrayList<>();
	private final List<String> homologies = new ArrayList<>();
	private final Map<StructureSource, Classification> domainNameToCategory = new HashMap<>();
	private final Map<String, String> pdbToDomainName = new HashMap<>();
	private final Map<String, String> categoryToName = new HashMap<>();
	private final List<String> unclassified = new ArrayList<>();

	public CathOldVersion(Directories dirs) {
		this.dirs = dirs;
		//readDomains();
		//readNames();
		//readDomainList();
		// one of those might possibly be omitted
	}

	public Level getH() {
		return families;
	}

	//TODO second, construct also the classisification?
/*	private void readDomains() {
		String line = null;
		try (BufferedReader br = new BufferedReader(new FileReader(dirs.getCathDomainBoundaries()))) {
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				}
				StringTokenizer st = new StringTokenizer(line, " \t");
				String prefix = st.nextToken();
				String domainsCount = st.nextToken();
				assert domainsCount.startsWith("D");
				int numberOfDomains = Integer.parseInt(domainsCount.substring(1));
				String fragments = st.nextToken();
				int numberOfFragments = Integer.parseInt(fragments.substring(1));
				assert fragments.startsWith("F") : line;
				for (int i = 0; i < numberOfDomains; i++) {
					Integer numberOfSegments = Integer.parseInt(st.nextToken());
					int domainIndex;
					// if whole structure is used, domain id is 00, otherwise the numbering starts with 01
					if (numberOfDomains + numberOfFragments == 1 && numberOfSegments == 1) {
						domainIndex = 0;
					} else {
						domainIndex = i + 1;
					}
					Domain domain = new Domain(prefix, domainIndex);
					for (int k = 0; k < numberOfSegments; k++) {
						String startChain = st.nextToken();
						int startNumber = Integer.parseInt(st.nextToken());
						String startInsertion = st.nextToken();
						String endChain = st.nextToken();
						int endNumber = Integer.parseInt(st.nextToken());
						String endInsertion = st.nextToken();
						assert startInsertion.length() == 1;
						assert endInsertion.length() == 1;
						Segment segment = new Segment(startChain, startNumber, startInsertion.charAt(0),
							endChain, endNumber, endInsertion.charAt(0));
						domain.addSegment(segment);
					}
					domains.put(domain.getSource(), domain);
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException(line, ex);
		}
	}

	public Classification getClassification(StructureSource domain) {
		return domainNameToCategory.get(domain);
	}

	private void readDomainList() {
		String line = null;
		try (BufferedReader br = new BufferedReader(new FileReader(dirs.getCathDomainList()))) {
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				}
				StringTokenizer st = new StringTokenizer(line, " \t");
				String domainName = st.nextToken();
				StructureSource source = new StructureSource(domainName);
				int c = Integer.parseInt(st.nextToken());
				int a = Integer.parseInt(st.nextToken());
				int t = Integer.parseInt(st.nextToken());
				int h = Integer.parseInt(st.nextToken());
				int[] levels = {c, a, t, h};
				domainNameToCategory.put(source, new Classification(levels));
				pdbToDomainName.put(domainName.substring(0, 4), domainName);
			}
		} catch (IOException ex) {
			throw new RuntimeException(line, ex);
		}
	}

	private void parseUnclassified() {
		String line = null;
		try (BufferedReader br = new BufferedReader(new FileReader(dirs.getCathDomainList()))) {
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				}
				StringTokenizer st = new StringTokenizer(line, " \t");
				String domainName = st.nextToken();
				unclassified.add(domainName);
			}
		} catch (IOException ex) {
			throw new RuntimeException(line, ex);
		}
	}

	/*public List<String> getTopologyRepresentants() {
		return topologies;
	}

	public List<String> getHomologousFamilyRepresentants() {
		return homologies;
	}

	public List<String> getUnclassifiedDomains() {
		return unclassified;
	}

	public List<String> getTopologyContent(String id) {
		Classification category = getCategoryForDomain(id);
		return getDomainsInTopology(category);
	}

	public List<String> getHomologyContent(String id) {
		Classification category = getCategoryForDomain(id);
		return getDomainsInHomology(category);
	}

	private List<String> getDomainsInTopology(Classification topologyRepresentant) {
		List<String> domains = new ArrayList<>();
		for (String domain : domainNameToCategory.keySet()) {
			Classification category = domainNameToCategory.get(domain);
			if (category.shareTopology(topologyRepresentant)) {
				domains.add(domain);
			}
		}
		return domains;
	}

	private List<String> getDomainsInHomology(Classification representant) {
		List<String> domains = new ArrayList<>();
		for (String domain : domainNameToCategory.keySet()) {
			Classification category = domainNameToCategory.get(domain);
			if (category.shareHomology(representant)) {
				domains.add(domain);
			}
		}
		return domains;
	}*/
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
	}

	// seems incomplete  1.10.1650
	@Deprecated
	private void readNames() {
		String line = null;
		try (BufferedReader br = new BufferedReader(new FileReader(dirs.getCathNames()))) {
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				}
				StringTokenizer st = new StringTokenizer(line, " \t");
				String classificationS = st.nextToken();
				Classification classification = new Classification(classificationS);
				String representantDomainId = st.nextToken();
				Domain domain = domains.get(representantDomainId);
				String name = st.nextToken();
				int depth = classification.getDepth();
				if (depth == 2) {
					topologies.add(representantDomainId);
				} else if (depth == 3) {
					homologies.add(representantDomainId);
					families.addRepresentant(classification, domain);
				} else if (depth == 4) {
					families.addMember(classification.createHomologousFamily(), domain);
				} else if (depth != 1) {
					throw new RuntimeException(line);
				}
				//categoryToName.put(classification, name);
				//tree.add(classification, domain);
			}
		} catch (IOException ex) {
			throw new RuntimeException(line, ex);
		}
	}

}
