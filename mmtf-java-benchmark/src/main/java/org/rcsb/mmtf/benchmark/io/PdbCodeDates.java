package org.rcsb.mmtf.benchmark.io;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import org.rcsb.mmtf.utils.Lines;

/**
 *
 * @author Antonin Pavelka
 *
 * Provides PDB codes of all entries, or entries reliesed before specified date.
 *
 */
public class PdbCodeDates {

	/**
	 *
	 * @return List of all PDB codes from a file download from
	 * http://www.rcsb.org/pdb/static.do?p=general_information/about_pdb/summaries.html
	 * @throws IOException
	 */
	public static List<String> getCodes() throws IOException {
		List<String> codes = new ArrayList<>();
		for (String line : Lines.readResource(
			"/mmtf-benchmark/entries.txt.gz")) {
			codes.add(line.trim().substring(0, 4).toLowerCase());
		}
		Collections.sort(codes);
		return codes;
	}

	/**
	 * Return PDB codes of the entries released before the specified date.
	 *
	 * @param beforeDate Date in YYYY-MM-DD format. Example: 16GS,1999-01-13
	 * @return List of PDB codes.
	 * @throws java.io.IOException
	 * @throws java.text.ParseException
	 */
	public static List<String> getCodesBefore(String beforeDate) throws IOException,
		ParseException {
		List<String> valid = getCodes();
		Set<String> before = getAllCodesBefore(beforeDate);
		valid.retainAll(before);
		List<String> result = new ArrayList<>(valid);
		Collections.sort(result); // to be deterministic
		return result;
	}

	// includes obsoleted entries and models, to be removed in public method
	private static Set<String> getAllCodesBefore(String beforeDate) throws IOException,
		ParseException {
		Set<String> codes = new HashSet<>();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date deadline = formatter.parse(beforeDate);
		for (String line
			: Lines.readResource("/mmtf-benchmark/release_dates.csv.gz")) {
			StringTokenizer st = new StringTokenizer(line, ",");
			String code = st.nextToken().toLowerCase();
			if (st.hasMoreTokens()) { // do not include unknown dates
				String date = st.nextToken();
				Date d = formatter.parse(date);
				
				if (d.before(deadline)) {
					codes.add(code);
				}
			}
		}
		return codes;
	}
}
