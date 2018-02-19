package structure;

import global.io.LineFile;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

public class PdbEntries {

	private final List<StructureSource> codes = new ArrayList<>();
	private final List<StructureSource> shuffledCodes = new ArrayList<>();
	private final List<String> types = new ArrayList<>();
	private final List<String> experiments = new ArrayList<>();
	private int index;

	public PdbEntries(File f) {
		LineFile lf = new LineFile(f);
		List<String> lines = lf.readLines();
		for (String line : lines) {
			parseLine(line);
		}
		Collections.shuffle(shuffledCodes, new Random(1));
	}

	private void parseLine(String line) {
		StringTokenizer st = new StringTokenizer(line, " \t");
		String code = st.nextToken();
		String type = st.nextToken();
		String experiment = st.nextToken();
		if (type.equals("prot")) {
			StructureSource source = new StructureSource(code);
			codes.add(source);
			shuffledCodes.add(source);
			types.add(type);
			experiments.add(experiment);
		}
	}

	public List<StructureSource> getCodes() {
		return codes;
	}

	public int size() {
		return codes.size();
	}

	public StructureSource get(int index) {
		return codes.get(index);
	}

	public StructureSource getNextRandom() {
		return get(index++);
	}

}
