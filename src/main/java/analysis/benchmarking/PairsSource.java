package analysis.benchmarking;

import global.io.Directories;
import global.io.LineFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import structure.StructureSource;
import util.FlexibleLogger;

public class PairsSource implements Iterable<StructurePair> {

	private Directories dirs;
	private List<StructurePair> pairs = new ArrayList<>();

	public enum Source {
		CUSTOM, TOPOLOGY89, MALIDUP, MALISAM, GENERATE, CLICK, HOMSTRAD
	}

	public PairsSource(Directories dirs, Source source) {
		this.dirs = dirs;
		switch (source) {
			case TOPOLOGY89:
				initCustom(dirs.get89Pairs());
				break;
			case CUSTOM:
				initCustom(dirs.getCustomPairs());
				break;
			case MALIDUP:
				initMicanBenchmark(dirs.getMalidupPairs());
				break;
			case MALISAM:
				initMicanBenchmark(dirs.getMalisamPairs());
				break;
			case GENERATE:
				initGenerate();
				break;
			case CLICK:
				initClick();
				break;
			case HOMSTRAD:
				initHomstrad();
				break;
		}
	}

	@Override
	public Iterator<StructurePair> iterator() {
		return pairs.iterator();
	}

	private void initCustom(File file) {
		String line;
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			while ((line = br.readLine()) != null) {
				try {
					StringTokenizer st = new StringTokenizer(line, ",; \t");
					StructureSource a = new StructureSource(st.nextToken().trim());
					StructureSource b = new StructureSource(st.nextToken().trim());
					pairs.add(new StructurePair(a, b));
				} catch (Exception ex) {
					System.err.println("fail: " + line);
					FlexibleLogger.error(ex);
					//ex.printStackTrace();
				}
			}
		} catch (Exception ex) {
			FlexibleLogger.error(ex);
		}
	}

	private String prefix(File f) {
		return f.getName().substring(0, f.getName().length() - 6);
	}

	/**
	 * MALIDUP and MALISAM datasets used to test the tool MICAN.
	 *
	 * @param which
	 */
	private void initMicanBenchmark(File which) {
		File[] files = which.listFiles();
		for (int x = 0; x < files.length; x++) {
			File a = files[x];
			String an = prefix(a);
			for (int y = 0; y < x; y++) {
				File b = files[y];
				String bn = prefix(b);
				if (an.equals(bn)) {
					pairs.add(new StructurePair(new StructureSource(a), new StructureSource(b)));
				}
			}

		}
	}

	/**
	 * CLICK file format. http://mspc.bii.a-star.edu.sg/minhn/reference.html
	 */
	private void initClick() {
		try (BufferedReader br = new BufferedReader(new FileReader(
			dirs.getTopologyIndependentPairs()))) {
			String line;
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, "\t");
				StructureSource a = new StructureSource(st.nextToken().trim());
				StructureSource b = new StructureSource(st.nextToken().trim());
				pairs.add(new StructurePair(a, b));
			}
		} catch (Exception ex) {
			FlexibleLogger.error(ex);
		}
	}

	private void initHomstrad() {
		String line;
		try (BufferedReader br = new BufferedReader(new FileReader(dirs.getHomstradPairs()))) {
			br.readLine();
			while ((line = br.readLine()) != null) {
				try {
					StringTokenizer st = new StringTokenizer(line, "\t");
					st.nextToken();
					StructureSource a = new StructureSource(st.nextToken().trim());
					StructureSource b = new StructureSource(st.nextToken().trim());
					pairs.add(new StructurePair(a, b));
				} catch (Exception ex) {
					FlexibleLogger.error(ex);
				}
			}
		} catch (Exception ex) {
			FlexibleLogger.error(ex);
		}
	}

	private void initGenerate() {
		Set<String> codeSet = new TreeSet<>();
		LineFile lf = new LineFile(dirs.getCathS20());
		for (String line : lf.readLines()) {
			String code = line.trim().substring(0, 4).toLowerCase();
			codeSet.add(code);
		}
		String[] codes = new String[codeSet.size()];
		codeSet.toArray(codes);
		Random random = new Random(1);
		for (int i = 0; i < 1000; i++) {
			StructureSource a = new StructureSource(codes[random.nextInt(codes.length)]);
			StructureSource b = new StructureSource(codes[random.nextInt(codes.length)]);
			pairs.add(new StructurePair(a, b));
		}
	}

}
