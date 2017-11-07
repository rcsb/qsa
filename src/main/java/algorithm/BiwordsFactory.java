package algorithm;

import global.Parameters;
import geometry.Coordinates;
import geometry.GridRangeSearch;
import geometry.Point;
import global.io.Directories;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.rcsb.mmtf.dataholders.Entity;
import pdb.ChainId;
import pdb.PdbLine;
import pdb.Residue;
import pdb.SimpleChain;

import pdb.SimpleStructure;
import util.Counter;
import util.Timer;

/**
 *
 * @author Antonin Pavelka
 *
 * Generator of all biwords from a single (quaternary) structure.
 *
 */
public final class BiwordsFactory implements Serializable {

	private final Parameters parameters = Parameters.create();
	private final Directories dirs;
	private final SimpleStructure structure;
	private final WordImpl[] words;
	private final boolean permute;
	private final Counter idWithinStructure = new Counter();
	private List<Biword> biwordList = new ArrayList<>(); // TODO rename

	public BiwordsFactory(Directories dirs, SimpleStructure structure, int sparsity, boolean permute) {
		this.dirs = dirs;
		this.structure = structure;
		this.permute = permute;
		WordsFactory wf = new WordsFactory(structure, parameters.getWordLength());
		wf.setSparsity(sparsity);
		words = wf.create().toArray();
	}

	// TODO getter instead, double calling disaster
	public Biwords create() {
		Timer.start();

		GridRangeSearch<AtomToWord> grid = new GridRangeSearch<>(5);
		List<AtomToWord> atoms = new ArrayList<>();

		// each atom of the residue in the middle of a word will map to the word
		for (WordImpl w : words) {
			Residue r = w.getCentralResidue();
			//System.out.println("p " + r.getId() + " " + r.getIndex());
			for (double[] x : r.getAtoms()) {
				AtomToWord central = new AtomToWord(x, w);
				atoms.add(central);
			}
		}
		grid.buildGrid(atoms);
		// try contacts just with 1 - almost unique perpendicular, and it is atoms
		// lets hope for lot less neighbors
		// easier implementation of vectors, but possibly also more hits, but we can always add another turn, if search stops to be a bottleneck
		Set<AtomToWord> ys = new HashSet<>();
		Set<AtomToWord> test = new HashSet<>();
		for (WordImpl x : words) {
			//Timer.start();
			//List<AtomToWord> ys = new ArrayList<>();
			ys.clear();
			test.clear();
			int n = 0;
			for (double[] atom : x.getAtoms()) {
				Point p = new Point(atom);
				grid.nearest(p, parameters.getAtomContactDistance(), ys);
				for (AtomToWord y : atoms) {
					Point yp = new Point(y.getCoords());
					if (p.distance(yp) <= parameters.getAtomContactDistance()) {
						test.add(y);
					}
				}
			}
			if (ys.size() != test.size()) {
				throw new RuntimeException();
			}

			// organize residues in contact by chain
			Map<ChainId, List<WordImpl>> byChain = new HashMap<>();
			for (AtomToWord aw : ys) {
				WordImpl y = aw.getWord();
				Residue a = x.getCentralResidue();
				Residue b = y.getCentralResidue();
				//if (!permute && a.getId().compareTo(b.getId()) >= 0) { // admit only on of two possible ordering, just for one side, e.g., query
				//	continue;
				//}
				// deal with both directions later
				if (a.isWithin(b, 4)) {
					continue;
				}
				ChainId c = b.getId().getChain();
				List<WordImpl> l = byChain.get(c);
				if (l == null) {
					l = new ArrayList<>();
					byChain.put(c, l);
				}
				l.add(y);
			}

			// identify connected residues among contacts
			List<List<WordImpl>> connected = new ArrayList<>();
			for (List<WordImpl> l : byChain.values()) {
				Collections.sort(l);
				List<WordImpl> active = new ArrayList<>(); // active connected chain of residues
				//System.out.println("***");
				for (WordImpl r : l) {
					if (!active.isEmpty()) {
						WordImpl p = active.get(active.size() - 1);
						if (r.getCentralResidue().follows(p.getCentralResidue())) {
							active.add(r);
						} else {
							connected.add(active);
							active = new ArrayList<>();
							//System.out.println("---");
						}
					} else {
						active.add(r);
					}
					//System.out.println(r.getId());
				}
			}
			//System.out.println("conn " + connected.size());

			//Timer.stop();
			//time += Timer.getNano();
			List<WordImpl> chosen = new ArrayList<>();
			// choose only the nearest residues from each strand
			// if ambiguous, use more
			for (List<WordImpl> strand : connected) {
				if (strand.size() == 1) {
					continue;
				}
				double min = Double.POSITIVE_INFINITY;
				Point xp = x.getCentralResidue().getCa();
				for (int i = 0; i < strand.size(); i++) {
					WordImpl r = strand.get(i);
					Point yp = r.getCentralResidue().getCa();
					double d = xp.distance(yp);
					if (d < min) {
						min = d;
					}
					//Residue q = strand.get(i + 1);
					//System.out.println(r.getId() + " " + q.follows(r) + " " + q.getId());
				}
				for (int i = 0; i < strand.size(); i++) {
					WordImpl r = strand.get(i);
					Point yp = r.getCentralResidue().getCa();
					double d = xp.distance(yp);
					if (d <= min + 0.5) {
						chosen.add(r);

					}
				}
			}
			for (WordImpl y : chosen) {
				if (y.getCentralResidue().getId().follows(x.getCentralResidue().getId())) {
					continue;
				}
				if (x.getCentralResidue().equals(y.getCentralResidue())) {
					continue;
				}
				double dist = x.getCentralResidue().distance(y.getCentralResidue());
				if (dist >= 2 || dist <= 20) {
					Biword f = new Biword(structure.getId(), idWithinStructure, x, y);
					biwordList.add(f);
					//fl.add(f.switchWords(idWithinStructure)); // will be entered anyway?
				}
				// TODO: switch only if order cannot be derived from length of word, angles and other, alphabetical order
			}
		}

		createSequentialBiwords();

		Biword[] biwordArray = new Biword[biwordList.size()];
		biwordList.toArray(biwordArray);
		for (int i = 0; i < biwordArray.length; i++) {
			if (i != biwordArray[i].getIdWithingStructure()) {
				throw new RuntimeException(i + " " + biwordArray[i].getIdWithingStructure());
			}
		}
		Biwords fs = new Biwords(structure, biwordArray, words);
		if (false) { // visualizing biwords
			save(fs, dirs.getWordConnections(structure.getSource()));
		}
		return fs;
	}

	private void createSequentialBiwords() {
		Map<Residue, WordImpl> residueToWord = new HashMap<>();
		for (WordImpl w : words) {
			Residue r = w.getCentralResidue();
			residueToWord.put(r, w);
		}
		for (SimpleChain c : structure.getChains()) {
			Residue[] rs = c.getResidues();
			int l = parameters.getWordLength();
			for (int i = l / 2; i < rs.length - l / 2 - parameters.getWordLength(); i++) {
				WordImpl x = residueToWord.get(rs[i]);
				WordImpl y = residueToWord.get(rs[i + 1]); // + wordLength

				if (x != null && y != null) { // word might be missing because of missing residue nearby
					double dist = x.getCentralResidue().distance(y.getCentralResidue());
					if (dist < 5 && dist > 2) {
						Biword f = new Biword(structure.getId(), idWithinStructure, x, y);
						biwordList.add(f);
						// no switching, sequence order is good here
						//fl.add(f.switchWords());				
					}
				}
			}
		}
	}

	private void save(Biwords bws, File f) {
		int serial = 1;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for (Biword x : bws.getBiwords()) {
				for (int i = 0; i < 2; i++) {
					double[] p = x.getWords()[i].getCentralResidue().getAtom("CA");
					PdbLine pl = new PdbLine(serial + i, "CA", "C", "GLY",
						Integer.toString(serial + i), 'A', p[0], p[1], p[2]);
					bw.write(pl.toString());
					bw.write("\n");
				}
				bw.write(PdbLine.getConnectString(serial, serial + 1));
				bw.write("\n");
				serial += 2;
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

	}
}

class AtomToWord implements Coordinates {

	private final double[] point;
	private final WordImpl word;

	public AtomToWord(double[] point, WordImpl word) {
		this.point = point;
		this.word = word;
	}

	@Override
	public double[] getCoords() {
		return point;
	}

	public WordImpl getWord() {
		return word;
	}

	@Override
	public boolean equals(Object o) {
		AtomToWord other = (AtomToWord) o;
		return word.getCentralResidue().getIndex() == other.word.getCentralResidue().getIndex();

	}
}
