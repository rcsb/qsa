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

	public Cath(Directories dirs) {
		this.dirs = dirs;
		readDomains();
		readNames();
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
					// in case there is more domains + fragments, indexing starts with 01, otherwise it is 00
					if (numberOfDomains + numberOfFragments == 1) {
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
						Segment segment = new Segment(startChain, startNumber, startInsertion,
							endChain, endNumber, endInsertion);
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
				String classification = st.nextToken();
				String domain = st.nextToken();
				if (countDots(classification) == 2) {
					topologies.add(domain);
				}
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

	public Domain getDomain(StructureSource source) {
		String domainId = source.getCathDomainId();
		assert map.containsKey(domainId) : domainId;
		return map.get(domainId);
	}

}
