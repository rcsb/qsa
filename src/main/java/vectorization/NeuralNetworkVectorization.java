package vectorization;

import fragments.WordImpl;
import fragments.Words;
import fragments.WordsFactory;
import geometry.Point;
import geometry.Transformer;
import io.Directories;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import pdb.SimpleStructure;
import pdb.StructureFactory;

public class NeuralNetworkVectorization {

	private Directories dirs = Directories.createDefault();
	private Random random = new Random(1);
	int folds = 2;
	int instanceN = 10000;

	public void run() throws IOException {
		int counter = 0;
		int structures = 0;
		StructureFactory provider = new StructureFactory(dirs);
		for (int fold = 0; fold < folds; fold++) {
			List<WordImpl> words = new ArrayList<>();
			try (BufferedReader br = new BufferedReader(new FileReader(dirs.getPdbFold(fold)))) {
				String line;
				while ((line = br.readLine()) != null) {
					String id = line.trim();
					if (id.length() != 5) { // two letter chains
						System.out.println("ignoring " + id);
						continue;
					}
					try {
						SimpleStructure ss = StructureFactory.convertProteinChains(provider.getSingleChain(id), id);
						WordsFactory wf = new WordsFactory(ss, 10);
						Words ws = wf.create();
						words.addAll(ws.get());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					System.out.println("words " + words.size() + " structures " + structures);
					structures++;
					if (structures > 1000) { // !!!!!!!!!!!!!!!!!!!!!!
						break;
					}
				}
			}
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(dirs.getVectorFold(fold)))) {
				WordImpl ex = words.get(random.nextInt(words.size()));
				int fn = ex.getFeatures().size();
				bw.write("@RELATION VECTORIZATION\n");
				for (int i = 0; i < fn; i++) {
					bw.write("@ATTRIBUTE A_" + i + " NUMERIC\n");
				}
				for (int i = 0; i < fn; i++) {
					bw.write("@ATTRIBUTE B_" + i + " NUMERIC\n");
				}
				bw.write("@ATTRIBUTE LABEL {0,1}\n");
				bw.write("@DATA\n");
				int i = 0;
				while (i < instanceN) {
					WordImpl a = words.get(random.nextInt(words.size()));
					WordImpl b = words.get(random.nextInt(words.size()));
					Transformer tr = new Transformer();
					tr.set(a.getPoints3d(), b.getPoints3d());
					double rmsd = tr.getRmsd();
					int l = getLabel(rmsd);
					if (l == 0 || (l == 1 && random.nextInt(3202 + 46798) <= 3202)) {
						bw.write(getArffLine(a.getFeatures(), b.getFeatures(), rmsd) + "\n");
						i++;
					}
				}
			}

		}
	}

	public int getLabel(double label) {
		if (label < 2.0) {
			return 0;
		} else {
			return 1;
		}
	}

	public String getArffLine(List<Double> af, List<Double> bf, double label) {
		StringBuilder sb = new StringBuilder();
		for (double d : af) {
			sb.append(d).append(",");
		}
		for (double d : bf) {
			sb.append(d).append(",");
		}
		sb.append(getLabel(label));
		return sb.toString();
	}

	public void generatePdbPairs() throws IOException {
		List<String> representants = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(dirs.getPdbClusters50()))) {
			String line;
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, " ");
				List<String> cluster = new ArrayList<>();
				while (st.hasMoreTokens()) {
					String id = st.nextToken().replace("_", "");
					cluster.add(id);
				}
				representants.add(cluster.get(random.nextInt(cluster.size())));
			}
		}
		Collections.shuffle(representants, random);
		int n = representants.size();
		int fn = n / folds;
		for (int i = 0; i < folds; i++) {
			savePdbCodes(representants.subList(i * fn, (i + 1) * fn), dirs.getPdbFold(i));
		}
	}

	public void savePdbCodes(List<String> codes, File file) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			for (String code : codes) {
				bw.write(code + "\n");
			}
		}
	}

	public static void main(String[] args) throws IOException {
		NeuralNetworkVectorization m = new NeuralNetworkVectorization();
		m.generatePdbPairs();
		m.run();
	}

}
