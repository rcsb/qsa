package pdb.cath;

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

/**
 *
 * @author Antonin Pavelka
 *
 * Class (C), architecture (A), topology (T) and homologous superfamily (H).
 *
 */
public class Cath {

	private final Directories dirs;
	private Map<String, Domain> map = new HashMap<>(); // 7 letter id (3ryhD03)
	private final List<String> topologies = new ArrayList<>();
	private final List<String> superfamilies = new ArrayList<>();
	private final Map<String, String> domainNameToCategory = new HashMap<>();
	private final Map<String, String> pdbToDomainName = new HashMap<>();
	private final Map<String, String> categoryToName = new HashMap<>();
	private final List<String> unclassified = new ArrayList<>();

	public Cath(Directories dirs) {
		this.dirs = dirs;
		readDomains();
		readNames();
		readDomainList();
	}

	private void readDomains() {
		map = new HashMap<>();
		String line = null;
		try (BufferedReader br = new BufferedReader(new FileReader(dirs.getCathDomainBoundaries()))) {
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				}
				StringTokenizer st = new StringTokenizer(line, " \t");
				String prefix = st.nextToken();
				String domains = st.nextToken();
				assert domains.startsWith("D");
				int numberOfDomains = Integer.parseInt(domains.substring(1));
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
					map.put(domain.getId(), domain);
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException(line, ex);
		}
	}

	private void readNames() {
		String line = null;
		try (BufferedReader br = new BufferedReader(new FileReader(dirs.getCathNames()))) {
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#")) {
					continue;
				}
				StringTokenizer st = new StringTokenizer(line, " \t");
				String category = st.nextToken();
				String domain = st.nextToken();
				String name = st.nextToken();
				int dots = countDots(category);
				if (dots == 2) {
					topologies.add(domain);
				} else if (dots == 3) {
					superfamilies.add(domain);
				}
				categoryToName.put(category, name);

			}
		} catch (IOException ex) {
			throw new RuntimeException(line, ex);
		}
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
				String c = st.nextToken();
				String a = st.nextToken();
				String t = st.nextToken();
				String h = st.nextToken();
				domainNameToCategory.put(domainName, c + "." + a + "." + t + "." + h);
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

	private int countDots(String s) {
		int count = 0;
		for (char c : s.toCharArray()) {
			if (c == '.') {
				count++;
			}
		}
		return count;
	}

	public List<String> getTopologyRepresentants() {
		return topologies;
	}

	public List<String> getSuperfamilyRepresentants() {
		return superfamilies;
	}

	public List<String> getUnclassifiedDomains() {
		return unclassified;
	}

	public Domain getDomain(StructureSource source) {
		String domainId = source.getCathDomainId();
		assert map.containsKey(domainId) : domainId;
		return map.get(domainId);
	}

	public void printPdbClassifications(String pdbCode) {
		String domainName = getDomainNameForPdb(pdbCode);
		String category = getCategoryForDomain(domainName);
		String name = categoryToName.get(category);
		System.out.println(category + " " + name);

	}

	private String getDomainNameForPdb(String pdbCode) {
		return pdbToDomainName.get(pdbCode);
	}

	private String getCategoryForDomain(String domainName) {
		return domainNameToCategory.get(domainName);
	}

}
