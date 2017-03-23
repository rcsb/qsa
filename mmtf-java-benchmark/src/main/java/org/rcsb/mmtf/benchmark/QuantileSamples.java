package org.rcsb.mmtf.benchmark;

import io.Directories;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureTools;

/**
 * Allows to gather all PDB entries with number of atoms close to the specified
 * value. All entries are sorted and the atom sizes of entry at the index in the
 * 25 %, 50 % and 75 % of the list of all entries are used to collect 100
 * entries with similar number of atoms.
 *
 * @author Antonin Pavelka
 */
public class QuantileSamples {

	private final Directories dirs;

	public QuantileSamples(Directories dirs) {
		this.dirs = dirs;
	}

	public void generateDatasets(int n) throws IOException {
		if (dirs.getPdbSizes().exists()) {
			saveSizes(dirs.getPdbSizes());
		}
		PdbEntry[] es = readEntries(dirs.getPdbSizes());

		System.out.println("Entry at 25 % has " + quantileIndex(es.length, 0.25)
			+ " atoms");
		System.out.println("Entry at 50 % has " + quantileIndex(es.length, 0.50)
			+ " atoms");
		System.out.println("Entry at 75 % has " + quantileIndex(es.length, 0.75)
			+ " atoms");

		saveDataset(sample(es, quantileIndex(es.length, 0.25), n),
			dirs.getSample25());
		saveDataset(sample(es, quantileIndex(es.length, 0.5), n),
			dirs.getSample50());
		saveDataset(sample(es, quantileIndex(es.length, 0.75), n),
			dirs.getSample75());
	}

	private void saveSizes(File f) throws IOException {
		Downloader d = new Downloader(dirs);
		Parser p = new Parser(dirs);
		Counter counter = new Counter();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for (String c : d.getCodes()) {
				counter.next();
				Structure s = p.parseMmtfToBiojava(c);
				bw.write(c + "," + StructureTools.getNrAtoms(s) + "\n");
			}
		}
	}

	private int quantileIndex(int length, double percent) {
		return (int) Math.round(percent * length);
	}

	private PdbEntry[] readEntries(File f) throws IOException {
		List<PdbEntry> all = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			while ((line = br.readLine()) != null) {
				String code = line.substring(0, 4);
				int size = Integer.parseInt(line.substring(5));
				PdbEntry pe = new PdbEntry(code, size);
				all.add(pe);
			}
		}
		PdbEntry[] a = all.toArray(new PdbEntry[all.size()]);
		Arrays.sort(a);
		System.out.println(a[a.length - 1].getCode() + " largest");
		return a;
	}

	/**
	 * Generates sample of entries with similar number of atoms as the entry
	 * with index center in the array all, which is sorted by atom size.	 
	 */
	private PdbEntry[] sample(PdbEntry[] all, int center, int n) {
		Downloader downloader = new Downloader(dirs);
		PdbEntry[] sample = new PdbEntry[n];
		sample[0] = all[center];
		int index = 1;
		int a = center - 1;
		int b = center + 1;
		while (index < n) {
			int da = all[a].getNumAtoms() - all[center].getNumAtoms();
			int db = all[center].getNumAtoms() - all[b].getNumAtoms();
			PdbEntry entry;
			if (da < db) {
				entry = all[a--];
			} else {
				entry = all[b++];
			}
			if (downloader.isAvailable(entry.getCode())) {
				sample[index++] = entry;
			}

		}
		Arrays.sort(sample);
		return sample;
	}

	private void saveDataset(PdbEntry[] data, File out) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
			for (PdbEntry e : data) {
				bw.write(e.getCode() + "," + e.getNumAtoms() + "\n");
			}
		}
	}
}
