package fragments.vector;

import io.Directories;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

/**
 *
 * @author Antonin Pavelka
 */
public class PdbDataset {

	private Directories dirs = Directories.createDefault();
	private Random random = new Random(1);

	public List<String> loadRandomRepresentants() throws IOException {
		List<String> representants = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(dirs.getPdbClusters50()))) {
			String line;
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, " ");
				List<String> cluster = new ArrayList<>();
				while (st.hasMoreTokens()) {
					String id = st.nextToken().replace("_", "");
					if (id.length() == 5) {
						cluster.add(id);
					}
				}
				if (!cluster.isEmpty()) {
					representants.add(cluster.get(random.nextInt(cluster.size())));
				}
			}
		}
		Collections.shuffle(representants, random);
		return representants;
	}

	public List<String> loadAll() {
		List<String> codes = new ArrayList<>();
		String line=null;
		try (BufferedReader br = new BufferedReader(new FileReader(dirs.getPdbEntryTypes()))) {			
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, " \t");
				String code = st.nextToken();
				String type = st.nextToken();
				if (type.equals("prot")) {
					codes.add(code);
				}
			}
		} catch (Exception ex) {
			System.err.println(line);
			throw new RuntimeException(ex);
		}
		Collections.shuffle(codes, random);

		return codes;
	}

}
