package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Antonin Pavelka
 *
 * Provides PDB codes of all entries, or entries reliesed before specified date.
 *
 */
public class PdbEntries {

	private final List<String> codes = new ArrayList<>();
	private final List<Date> dates = new ArrayList<>();
	private static SimpleDateFormat formatter
		= new SimpleDateFormat("MM/dd/yyyy");

	/**
	 * @param beforeDate Date in MM/DD/YYYY format.
	 */
	public PdbEntries(File f, String beforeDate) throws IOException, ParseException {
		Date before = null;
		if (beforeDate != null) {
			before = formatter.parse(beforeDate);
		}
		List<String> lines = ZippedTextFile.readLines(f);
		for (String line : lines) {
			try {
				StringTokenizer st = new StringTokenizer(line, "\t");
				String code = st.nextToken();
				String date = st.nextToken();
				Date d = formatter.parse(date);
				if (d.before(before)) {
					codes.add(code);
					dates.add(d);
				}
			} catch (Exception ex) {
				System.err.println(line);
				throw new RuntimeException(ex);
			}
		}
	}

	public PdbEntries(File f) throws IOException, ParseException {
		this(f, null);
	}

	public List<String> getCodes() {
		return codes;
	}

	/**
	 * Saves PDB code and dates into a smaller file.
	 */
	public static void saveDates(File out) throws IOException, ParseException {
		URL url = new URL(
			"ftp://ftp.wwpdb.org/pub/pdb/derived_data/index/entries.idx");
		try (BufferedReader br = new BufferedReader(
			new InputStreamReader(url.openStream()));
			BufferedWriter bw = new BufferedWriter(
				new FileWriter(out.toString()));) {
			String line;
			br.readLine();
			br.readLine();
			List<String> lines = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				String[] cols = line.split("\t");
				formatter.parse(cols[2]);
				lines.add(cols[0] + "\t" + cols[2]);
			}
			ZippedTextFile.writeLines(lines, out);
		}
	}

	public static void main(String[] args) throws Exception {
		File f = new File("entries.txt.zip");
		System.out.println("Saving PDB entries to " + f.getAbsolutePath());
		PdbEntries.saveDates(f);
	}

}
