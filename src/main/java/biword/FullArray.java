package biword;

import fragments.Biword;
import fragments.Biwords;
import fragments.WordImpl;
import geometry.Point;
import io.LineFile;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import pdb.PdbLine;

public class FullArray {

	// ID of biword can be int
	// last dimension is bucket
	int[][][][][][][] array = new int[10][10][10][10][10][10][10]; // can be made partially sparse by top down initalization

	/// how empty it will be...
	// define coordinate system
	// first word - 1. axis
	// from center perpendicular towards other word: 2. axis, duplicate if uncertain
	// 2. word determines orientation of 3. axis, duplicate if uncertain
	// 
	// distance to second word center
	// angle to second word center (duplicate)
	// angle of words (duplicate)
	// torsion angle (duplicate)
	// CA-CB angle of middle residue
	// mostly alpha, mostly beta, else (duplicate uncertain but rarely)
	// length of words
	// see distributions in weka
	// or just place it into space and learn? especially chebyshev would be nice
	// focus on 5 residues (CA-CB for interfaces maybe)
	// 2x phi, psi = 4
	// 6 for orientation, distance, angles, if last two uncertain, leave them in one bucket
	// ! focus only on local contacts - at most distance of two beta sheets, but rather:
	// (maximum distance of residues touching), require they actually touch by atoms (all, just in case)
	public static void main(String[] args) {
		BiwordsProvider bp = new BiwordsProvider();
		Biwords bs;
		int proteins = 0;
		int biwords = 0;
		LineFile lf = new LineFile(new File("c:/kepler/rozbal/biwords.csv"));
		while ((bs = bp.next()) != null) {
			int count = 0;
			save(bs.getBiwords(), new File("c:/kepler/rozbal/" + bp.getLastPdbCode() + ".pdb"));
			for (Biword bw : bs.getBiwords()) {
				count++;
			}
			lf.writeLine(bp.getLastPdbCode() + "," + bp.getLastSize() + "," + count);
			biwords += count;
			proteins++;
			System.out.println(biwords + " | " + "count " + count + " | " + proteins);
			System.out.println("	EXIT");
			return ;
		}
	}

	public static void save(Biword[] biwords, File f) {
		try {
			int serial = 1;
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
				for (int i = 0; i < biwords.length; i++) {
					for (int k = 0; k < 2; k++) {
						WordImpl w = biwords[i].getWords()[k];
						Point p = w.getCenter();
						PdbLine pl = new PdbLine(serial + k, "CA", "C", "GLY",
							Integer.toString(serial + k), 'A', p.x, p.y, p.z);
						bw.write(pl.toString());
					}
					bw.newLine();
					bw.write(PdbLine.getConnectString(serial, serial + 1));
					bw.newLine();
					serial += 2;
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void add(int[] vector) {

		for (int d = 0; d < vector.length; d++) {
			Object o = array[0][0];

			//if (array[0] == null)
		}
	}

	public void query(int[] vector) {

	}

}
