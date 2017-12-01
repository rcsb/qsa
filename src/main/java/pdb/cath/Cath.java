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
 */
public class Cath {

	Directories dirs;
	private Map<String, CathDomain> map = new HashMap<>();

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
				StringTokenizer st = new StringTokenizer(line, " \t");
				String prefix = st.nextToken();
				String domains = st.nextToken();
				assert domains.startsWith("D");
				int numberOfDomains = Integer.parseInt(domains.substring(1));
				String fragments = st.nextToken();
				assert fragments.startsWith("D");
				for (int i = 0; i < numberOfDomains; i++) {
					Integer numberOfSegments = Integer.parseInt(st.nextToken());
					for (int k = 0; k < numberOfSegments; k++) {
						String startChain = st.nextToken();
						int startNumber = Integer.parseInt(st.nextToken());
						String startInsertion = st.nextToken();
						String endChain = st.nextToken();
						int endNumber = Integer.parseInt(st.nextToken());
						String endInsertion = st.nextToken();
					}
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException(line, ex);
		}
	}

	private void readNames() {

	}

	public List<String> getArchitectureRepresentants() {
		List<String> list = new ArrayList<>();
		return list;
	}

	public CathDomain getDomain(StructureSource source) {
		return null;
	}

}
