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
	private final Word[] words;
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

	/**
	 * @return Data structure for efficient retrieval of words in contact with a given atom.
	 */
	private GridRangeSearch<AtomToWord> createAtomGrid() {
		GridRangeSearch<AtomToWord> grid = new GridRangeSearch<>(5);
		List<AtomToWord> atoms = new ArrayList<>();
		for (Word word : words) {
			Residue centralResidue = word.getCentralResidue();
			for (double[] atom : centralResidue.getAtoms()) {
				atoms.add(new AtomToWord(atom, word));
			}
		}
		grid.buildGrid(atoms);
		return grid;
	}

	private void findAllWordsInContact(Word queryWord, GridRangeSearch<AtomToWord> grid, Set<AtomToWord> wordsInContact) {
		for (double[] atom : queryWord.getAtoms()) {
			Point p = new Point(atom);
			grid.nearest(p, parameters.getAtomContactDistance(), wordsInContact);
		}
	}

	/**
	 * Residues of a biword cannot be overlapping (which can happen only if they follow each other in sequence too
	 * closely).
	 */
	private void removeOverlapsWithQueryWord(Word queryWord) {

	}

	// TODO move filtering by sequence elsewhere?
	// TODO what about permutations?
	private Map<ChainId, List<Word>> organizeWordsInContactByChain(Word queryWord, Set<AtomToWord> wordsInContact) {
		// organize residues in contact by chain
		Map<ChainId, List<Word>> byChain = new HashMap<>();
		for (AtomToWord aw : wordsInContact) {
			Word y = aw.getWord();
			Residue a = queryWord.getCentralResidue();
			Residue b = y.getCentralResidue();
			//if (!permute && a.getId().compareTo(b.getId()) >= 0) { // admit only on of two possible ordering, just for one side, e.g., query
			//	continue;
			//}
			// deal with both directions later
			if (a.isWithin(b, 4)) {
				continue;
			}
			ChainId c = b.getId().getChain();
			List<Word> l = byChain.get(c);
			if (l == null) {
				l = new ArrayList<>();
				byChain.put(c, l);
			}
			l.add(y);
		}
		return byChain;
	}

	// TODO getter instead, double calling disaster
	public Biwords create() {
		Timer.start();
		GridRangeSearch<AtomToWord> grid = createAtomGrid();
		Set<AtomToWord> wordsInContact = new HashSet<>();
		for (Word queryWord : words) {
			wordsInContact.clear();
			findAllWordsInContact(queryWord, grid, wordsInContact);
			Map<ChainId, List<Word>> byChain = organizeWordsInContactByChain(queryWord, wordsInContact);

			// identify connected residues among contacts
			List<List<Word>> connected = new ArrayList<>();
			for (List<Word> l : byChain.values()) {
				Collections.sort(l);
				List<Word> active = new ArrayList<>(); // active connected chain of residues
				//System.out.println("***");
				for (Word r : l) {
					if (!active.isEmpty()) {
						Word p = active.get(active.size() - 1);
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
			List<Word> chosen = new ArrayList<>();
			// choose only the nearest residues from each strand
			// if ambiguous, use more
			for (List<Word> strand : connected) {
				if (strand.size() == 1) {
					continue;
				}
				double min = Double.POSITIVE_INFINITY;
				Point xp = queryWord.getCentralResidue().getCa();
				for (int i = 0; i < strand.size(); i++) {
					Word r = strand.get(i);
					Point yp = r.getCentralResidue().getCa();
					double d = xp.distance(yp);
					if (d < min) {
						min = d;
					}
					//Residue q = strand.get(i + 1);
					//System.out.println(r.getId() + " " + q.follows(r) + " " + q.getId());
				}
				for (int i = 0; i < strand.size(); i++) {
					Word r = strand.get(i);
					Point yp = r.getCentralResidue().getCa();
					double d = xp.distance(yp);
					if (d <= min + 0.5) {
						chosen.add(r);

					}
				}
			}
			for (Word y : chosen) {
				if (y.getCentralResidue().getId().follows(queryWord.getCentralResidue().getId())) {
					continue;
				}
				if (queryWord.getCentralResidue().equals(y.getCentralResidue())) {
					continue;
				}
				double dist = queryWord.getCentralResidue().distance(y.getCentralResidue());
				if (dist >= 2 || dist <= 20) {
					Biword f = new Biword(structure.getId(), idWithinStructure, queryWord, y);
					biwordList.add(f);
					//fl.add(f.switchWords(idWithinStructure)); // will be entered anyway?
				}
				// TODO: switch only if order cannot be derived from length of word, angles and other, alphabetical order
			}
		}

		createSequentialBiwords();

		Biword[] biwordArray = new Biword[biwordList.size()];
		biwordList.toArray(biwordArray);
		assert checkIds(biwordArray);
		Biwords fs = new Biwords(structure, biwordArray, words);
		if (false) { // visualizing biwords
			save(fs, dirs.getWordConnections(structure.getSource()));
		}
		return fs;
	}

	private boolean checkIds(Biword[] biwordArray) {
		for (int i = 0; i < biwordArray.length; i++) {
			if (i != biwordArray[i].getIdWithingStructure()) {
				return false;
			}
		}
		return true;
	}

	private void createSequentialBiwords() {
		Map<Residue, Word> residueToWord = new HashMap<>();
		for (Word word : words) {
			Residue r = word.getCentralResidue();
			residueToWord.put(r, word);
		}
		for (SimpleChain chain : structure.getChains()) {
			Residue[] residues = chain.getResidues();
			int length = parameters.getWordLength();
			for (int i = length / 2; i < residues.length - length / 2 - parameters.getWordLength(); i++) {
				Word x = residueToWord.get(residues[i]);
				Word y = residueToWord.get(residues[i + 1]); // + wordLength
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
	private final Word word;

	public AtomToWord(double[] point, Word word) {
		this.point = point;
		this.word = word;
	}

	@Override
	public double[] getCoords() {
		return point;
	}

	public Word getWord() {
		return word;
	}

	@Override
	public boolean equals(Object o) {
		AtomToWord other = (AtomToWord) o;
		return word.getCentralResidue().getIndex() == other.word.getCentralResidue().getIndex();

	}
}
