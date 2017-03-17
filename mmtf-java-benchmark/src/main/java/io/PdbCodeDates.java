package io;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author Antonin Pavelka
 *
 * Provides PDB codes of all entries, or entries reliesed before specified date.
 *
 */
public class PdbCodeDates {

	private final List<String> codes = new ArrayList<>();
	private final List<Date> dates = new ArrayList<>();
	private static SimpleDateFormat formatter
		= new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * @param beforeDate Date in YYYY-MM-DD format.
	 */
	public PdbCodeDates(File datesF, File validF, String beforeDate)
		throws IOException, ParseException {
		Date before = null;
		if (beforeDate != null) {
			before = formatter.parse(beforeDate);
		}
		List<String> lines = ZippedTextFile.readLines(datesF);
		Set<String> valid = new HashSet<>();
		if (validF != null) {
			valid.addAll(ZippedTextFile.readLines(validF));
		}
		for (String line : lines) {
			try {
				StringTokenizer st = new StringTokenizer(line, ",");
				String code = st.nextToken();
				if (st.hasMoreTokens()) {
					String date = st.nextToken();
					Date d = formatter.parse(date);
					if (valid.contains(code) && d.before(before)) {
						codes.add(code);
						dates.add(d);
					}
				}
			} catch (Exception ex) {
				System.err.println(line);
				throw new RuntimeException(ex);
			}
		}
	}
	
	/**
	 * File format: PDB_code,YYYY-MM-DD
	 * Example: 16GS,1999-01-13
	 */
	public PdbCodeDates(File dates) throws IOException, ParseException {
		this(dates, null, null);
	}

	public List<String> getCodes() {
		return codes;
	}

}
