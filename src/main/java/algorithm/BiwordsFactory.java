package algorithm;

import biword.Word;
import biword.WordsFactory;
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
import structure.ChainId;
import structure.PdbLine;
import structure.Residue;
import structure.SimpleChain;
import structure.SimpleStructure;
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

	private final Parameters parameters;
	private final Directories dirs;
	private final SimpleStructure structure;
	private final Word[] words;
	private final boolean permute;
	private final Counter idWithinStructure = new Counter();
	//private final List<Biword> biwordList = new ArrayList<>();
	private final BiwordedStructure biwords;

	private static int fails;
	private static int total;

	// TODO extract all number to parameters
	public BiwordsFactory(Parameters parameters, Directories dirs, SimpleStructure structure, int sparsity, boolean permute) {
		this.parameters = parameters;
		this.dirs = dirs;
		this.structure = structure;
		this.permute = permute;
		WordsFactory wf = new WordsFactory(parameters, structure);
		wf.setSparsity(sparsity);
		words = wf.create().toArray();
		biwords = create();
		if (parameters.isVisualizeBiwords()) {
			save(biwords, dirs.getWordConnections(structure.getSource()));
		}
	}

	public BiwordedStructure getBiwords() {
		return biwords;
	}

	// TODO use permute
	private BiwordedStructure create() {
		Timer.start();
		GridRangeSearch<AtomToWord> grid = createAtomGrid();
		Set<AtomToWord> wordsInContact = new HashSet<>();
		List<Word[]> pairs = new ArrayList<>();
		for (Word queryWord : words) {
			wordsInContact.clear();
			findAllWordsInContact(queryWord, grid, wordsInContact);
			List<AtomToWord> notOverlapping = getNotOverlapping(queryWord, wordsInContact);
			List<AtomToWord> notTouching = getNotTouching(queryWord, notOverlapping);
			Map<ChainId, List<Word>> byChain = organizeWordsInContactByChain(queryWord, notTouching);
			List<List<Word>> strands = getConnectedStrands(byChain);
			List<Word> chosen = getShortest(queryWord, strands);
			List<Word> reasonable = getReasonable(queryWord, chosen);
			pairs.addAll(getPairs(queryWord, reasonable));
		}
		if (!permute) {
			pairs = getOneDirection(pairs);
		}
		int strSize = pairs.size();
		addSequentialBiwords(pairs);
		int totalSize = pairs.size();
		//System.out.println("str bwStr bwTot " + structure.size() + " " + strSize + " " + totalSize);
		if (structure.size() == 0) {
			fails++;
		}
		if (totalSize < structure.size()) {
			fails++;
			System.out.println("sparse " + structure.getSource() + " " + totalSize);
		}
		if (totalSize >= structure.size() * 10) {
			System.out.println("dense " + structure.getSource() + " " + totalSize);
			fails++;
		}
		total++;
		if (fails > 0) {
			System.out.println("biwords fails " + fails + " / " + total);
		}
		return createBiwords(pairs);
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

	// TEST?
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
	private List<AtomToWord> getNotOverlapping(Word queryWord, Set<AtomToWord> wordsInContact) {
		List<AtomToWord> result = new ArrayList<>();
		for (AtomToWord atomToWord : wordsInContact) {
			if (!atomToWord.getWord().overlaps(queryWord)) {
				result.add(atomToWord);
			}
		}
		return result;
	}

	private List<AtomToWord> getNotTouching(Word queryWord, List<AtomToWord> words) {
		List<AtomToWord> result = new ArrayList<>();
		for (AtomToWord atomToWord : words) {
			if (!atomToWord.getWord().isInContact(queryWord)) {
				result.add(atomToWord);
			}
		}
		return result;
	}

	// TODO move filtering by sequence elsewhere?
	private Map<ChainId, List<Word>> organizeWordsInContactByChain(Word queryWord, List<AtomToWord> wordsInContact) {
		// organize residues in contact by chain
		Map<ChainId, List<Word>> byChain = new HashMap<>();
		for (AtomToWord aw : wordsInContact) {
			Word y = aw.getWord();
			Residue ra = queryWord.getCentralResidue();
			Residue rb = y.getCentralResidue();
			if (ra.isWithin(rb, 4)) {
				continue;
			}
			ChainId chain = rb.getId().getChain();
			List<Word> list = byChain.get(chain);
			if (list == null) {
				list = new ArrayList<>();
				byChain.put(chain, list);
			}
			list.add(y);
		}
		return byChain;
	}

	private List<List<Word>> getConnectedStrands(Map<ChainId, List<Word>> byChain) {
		// identify connected residues among contacts
		List<List<Word>> connected = new ArrayList<>();
		for (List<Word> l : byChain.values()) {
			Collections.sort(l);
			List<Word> active = new ArrayList<>(); // active connected chain of residues
			for (Word r : l) {
				if (!active.isEmpty()) {
					Word p = active.get(active.size() - 1);
					if (r.getCentralResidue().isFollowedBy(p.getCentralResidue())) {
						active.add(r);
					} else {
						connected.add(active);
						active = new ArrayList<>();
					}
				} else {
					active.add(r);
				}
			}
		}
		return connected;
	}

	private List<Word> getShortest(Word queryWord, List<List<Word>> strands) {
		List<Word> chosen = new ArrayList<>();
		// choose only the nearest residues from each strand
		// if ambiguous, use more
		for (List<Word> strand : strands) {
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
		return chosen;
	}

	private List<Word> getReasonable(Word queryWord, List<Word> partners) {
		List<Word> reasonable = new ArrayList<>();
		for (Word partner : partners) {
			if (partner.getCentralResidue().getId().isFollowedBy(queryWord.getCentralResidue().getId())) {
				continue;
			}
			if (queryWord.getCentralResidue().equals(partner.getCentralResidue())) {
				continue;
			}
			double dist = queryWord.getCentralResidue().distance(partner.getCentralResidue());
			if (dist >= 2 || dist <= 20) {
				reasonable.add(partner);
			}
		}
		return reasonable;
	}

	private boolean checkIds(Biword[] biwordArray) {
		for (int i = 0; i < biwordArray.length; i++) {
			if (i != biwordArray[i].getIdWithingStructure()) {
				return false;
			}
		}
		return true;
	}

	private BiwordedStructure createBiwords(List<Word[]> pairs) {
		Biword[] array = new Biword[pairs.size()];
		for (int i = 0; i < pairs.size(); i++) {
			Word[] pair = pairs.get(i);
			Biword biword = new Biword(structure.getId(), idWithinStructure, pair[0], pair[1]);
			array[i] = biword;
		}
		assert checkIds(array);
		return new BiwordedStructure(structure, array, words);
	}

	private List<Word[]> getPairs(Word queryWord, List<Word> targetWords) {
		List<Word[]> pairs = new ArrayList<>();
		for (Word targetWord : targetWords) {
			Word[] pair = {queryWord, targetWord};
			pairs.add(pair);
		}
		return pairs;
	}

	/**
	 * Returns new list without pairs differing only by order of words.
	 */
	private List<Word[]> getOneDirection(List<Word[]> pairs) {
		Map<BiwordPairId, Word[]> unique = new HashMap<>();
		for (Word[] pair : pairs) {
			BiwordPairId id = new BiwordPairId(pair[0].getId(), pair[1].getId());
			if (unique.containsKey(id)) {
				Word[] other = unique.get(id);
				if (before(pair, other)) {
					unique.put(id, pair);
				}
			} else {
				unique.put(id, pair);
			}
		}
		List<Word[]> result = new ArrayList<>();
		result.addAll(unique.values());
		return result;
	}

	private boolean before(Word[] a, Word[] b) {
		if (a[0].getId() < b[0].getId()) {
			return true;
		} else if (a[0].getId() == b[0].getId()) {
			assert a[1].getId() != b[1].getId();
			return a[1].getId() < b[1].getId();
		} else {
			return false;
		}
	}

	private void addSequentialBiwords(List<Word[]> pairs) {
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
						Word[] pair = {x, y};
						pairs.add(pair);
					}
				}
			}
		}
	}

	private void save(BiwordedStructure bws, File f) {
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

	@Override
	public int hashCode() {
		return word.getCentralResidue().getIndex();
	}
}

class BiwordPairId {

	private int a, b;

	public BiwordPairId(int a, int b) {
		if (a <= b) {
			this.a = a;
			this.b = b;
		} else {
			this.b = a;
			this.a = b;
		}
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + this.a;
		hash = 97 * hash + this.b;
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		BiwordPairId other = (BiwordPairId) o;
		return a == other.a && b == other.b;
	}

}
