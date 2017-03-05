package pdb;

import io.LineFile;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class PdbEntries {

	private final List<String> codes = new ArrayList<>();

	public PdbEntries(File f) {
		LineFile lf = new LineFile(f);
		List<String> lines = lf.readLines();
		for (String line : lines) {
			StringTokenizer st = new StringTokenizer(line, " \t");
			String code = st.nextToken();
			codes.add(code);
		}
	}

	public List<String> getCodes() {
		return codes;
	}

}
