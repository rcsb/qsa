package analysis.script;

import alignment.StructureSourcePair;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import structure.StructureSource;

/**
 *
 * @author Antonin Pavelka Compares two alignment algorithm results. Takes all alignments where one of the algorithms
 * reached 0.5 TM-score. Makes a sum of their TM-score differences.
 */
public class HowMuchBetter {

	private String dir = "e:/data/qsa/jobs/";
	private File fileA = new File(dir + "job_77/task_1/table.csv");
	private File fileB = new File(dir + "job_78/task_1/table.csv");

	private Map<StructureSourcePair, Double> read(File f) {
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			Map<StructureSourcePair, Double> map = new HashMap<>();
			br.readLine();
			String line;
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, ",");
				String a = st.nextToken();
				String b = st.nextToken();
				double tmScore = Double.parseDouble(st.nextToken());
				StructureSourcePair pair = new StructureSourcePair(new StructureSource(a), new StructureSource(b));
				assert !map.containsKey(pair);
				map.put(pair, tmScore);
			}
			return map;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void run() {
		Map<StructureSourcePair, Double> a = read(fileA);
		Map<StructureSourcePair, Double> b = read(fileB);
		Set<StructureSourcePair> similar = new HashSet<>();
		getSimilar(a, similar);
		getSimilar(b, similar);
		double avgDiff = evaluate(similar, a, b);
		System.out.println(avgDiff);
	}

	private void getSimilar(Map<StructureSourcePair, Double> map,
		Set<StructureSourcePair> out) {

		for (StructureSourcePair key : map.keySet()) {
			double tmScore = map.get(key);
			if (tmScore >= 0.5) {
				out.add(key);
			}
		}
	}

	private double evaluate(Set<StructureSourcePair> similar, 
		Map<StructureSourcePair, Double> a,
		Map<StructureSourcePair, Double> b) {

		double sum = 0;
		int counter = 0;
		for (StructureSourcePair key : similar) {
			double tmScoreA = 0;
			double tmScoreB = 0;
			if (a.containsKey(key)) {
				tmScoreA = a.get(key);
			}
			if (b.containsKey(key)) {
				tmScoreB = b.get(key);
			}
			double dif = tmScoreA - tmScoreB;
			sum += dif;
			counter++;
		}
		return sum / counter;
	}

	public static void main(String[] args) {
		HowMuchBetter m = new HowMuchBetter();
		m.run();
	}
}
