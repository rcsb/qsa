package fragments;

import geometry.Coordinates;
import geometry.GridRangeSearch;
import geometry.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pdb.Residue;
import pdb.SimpleChain;

import pdb.SimpleStructure;

/**
 *
 * @author Antonin Pavelka
 */
public final class BiwordsFactory implements Serializable {

	private static final long serialVersionUID = 1L;
	private Parameters params_ = Parameters.create();
	private static boolean print = false;

	public BiwordsFactory() {
	}

	public Biwords create(SimpleStructure ss, int wordLength, int sparsity) {
		WordsFactory wf = new WordsFactory(ss, wordLength);
		wf.setSparsity(sparsity);
		WordImpl[] words = wf.create().toArray();
		List<Biword> fl = new ArrayList<>();

		GridRangeSearch<AtomToWord> grid = new GridRangeSearch<>(5);
		List<AtomToWord> atoms = new ArrayList<>();

		Map<Residue, WordImpl> residueToWord = new HashMap<>();

		// each atom of the residue in the middle of a word will map to the word
		for (WordImpl w : words) {
			Residue r = w.getCentralResidue();
			residueToWord.put(r, w);
			for (double[] x : r.getAtoms()) {
				AtomToWord central = new AtomToWord(x, w);
				atoms.add(central);
			}
		}
		grid.buildGrid(atoms);

		// try contacts just with 1 - almost unique perpendicular, and it is atoms
		// lets hope for lot less neighbors
		// easier implementation of vectors, but possibly also more hits, but we can always add another turn, if search stops to be a bottleneck
		for (WordImpl x : words) {
			List<AtomToWord> ys = null;
			for (double[] atom : x.getAtoms()) {
				Point p = new Point(atom);
				ys = grid.nearest(p, params_.getAtomContactDistance());
			}
			// locate residues leading to overlap, remove them from results
			// function: iterate over residue array, return in distance

			Map<Residue, WordImpl> hits = new HashMap<>();
			for (AtomToWord aw : ys) { // remove repeated hits
				WordImpl y = aw.getWord();
				if (!x.overlaps(y)) {
					hits.put(y.getCentralResidue(), y);
				}
			}
			//System.out.println(x.getCentralResidue().getId() + " : ");
			for (WordImpl y : hits.values()) {
				//System.out.println(y.getCentralResidue().getId());
				// avoiding case solved lower, sequence neighbors
				if (y.getCentralResidue().getId().follows(x.getCentralResidue().getId())) {
					continue;
				}
				if (x.getCentralResidue().equals(y.getCentralResidue())) {
					continue;
				}
				double dist = x.getCentralResidue().distance(y.getCentralResidue());
				if (dist >= 2 || dist <= 20) {
					Biword f = new Biword(ss.getId(), x, y);
					fl.add(f);
					fl.add(f.switchWords());
				}
				// TODO: switch only if order cannot be derived from length of word, angles and other, alphabetical order
			}
			//System.out.println("");
		}
		for (SimpleChain c : ss.getChains()) {
			Residue[] rs = c.getResidues();
			int l = params_.getWordLength();
			for (int i = l / 2; i < rs.length - l / 2 - wordLength; i++) {
				WordImpl x = residueToWord.get(rs[i]);
				WordImpl y = residueToWord.get(rs[i + 1]); // + wordLength

				if (x != null && y != null) { // word might be missing because of missing residue nearby
					double dist = x.getCentralResidue().distance(y.getCentralResidue());
					if (dist < 5 && dist > 2) {
						Biword f = new Biword(ss.getId(), x, y);
						fl.add(f);
						// no switching, sequence order is good here
						//fl.add(f.switchWords());				
					}
				}
			}
		}
		Biword[] fa = new Biword[fl.size()];
		fl.toArray(fa);
		Biwords fs = new Biwords(ss, fa, words);
		return fs;
	}

	/*public Biwords createBigWords(SimpleStructure ss, int wordLength, int sparsity) {
		WordsFactory wf = new WordsFactory(ss, wordLength);
		wf.setSparsity(sparsity);
		Words words = wf.create();
		if (print) {
			System.out.println("***** " + ss.size());
			for (WordImpl w : words) {
				w.print();
			}
		}
		WordImpl[] wa = words.toArray();
		List<Biword> fl = new ArrayList<>();
		for (int xi = 0; xi < wa.length; xi++) {
			for (int yi = 0; yi < xi; yi++) {
				if (xi == yi) {
					continue;
				}
				WordImpl x = wa[xi];
				WordImpl y = wa[yi];
				if (x.isInContactAndNotOverlapping(y, params_.getResidueContactDistance())) {
					Biword f = new Biword(ss.getId(), x, y);
					fl.add(f);
					fl.add(f.switchWords());
				}
			}
		}
		Biword[] fa = new Biword[fl.size()];
		fl.toArray(fa);
		Biwords fs = new Biwords(ss, fa, wa);
		return fs;
	}*/
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
}
