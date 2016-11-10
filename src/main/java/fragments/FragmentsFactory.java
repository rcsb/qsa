package fragments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import pdb.SimpleChain;
import pdb.SimpleStructure;

/**
 *
 * @author Antonin Pavelka
 */
public final class FragmentsFactory implements Serializable {

	private static final long serialVersionUID = 1L;
	private Parameters params_ = Parameters.create();
	private static boolean print = false;

	public FragmentsFactory() {
	}

	/*
	 * public Fragments create(SimpleStructure ss, int sparsity) { List<Word>
	 * words = new ArrayList<>(); for (SimpleChain chain : ss.getChains()) {
	 * words.addAll(getWords(chain, sparsity)); } if (print) {
	 * System.out.println("***** " + ss.size()); for (Word w : words) {
	 * w.print(); } } Fragments fs = new Fragments(ss); for (int xi = 0; xi <
	 * words.size(); xi++) { for (int yi = 0; yi < xi; yi++) { Word x =
	 * words.get(xi); Word y = words.get(yi); if (x.isInContact(y,
	 * params_.getResidueContactDistance())) { Fragment f = new Fragment(x, y);
	 * fs.add(f); fs.add(f.switchWords()); } } } return fs; }
	 */

	public Fragments createSingleWords(SimpleStructure ss, int sparsity) {
		List<Word> words = new ArrayList<>();
		for (SimpleChain chain : ss.getChains()) {
			words.addAll(getWords(chain, sparsity));
		}
		if (print) {
			System.out.println("***** " + ss.size());
			for (Word w : words) {
				w.print();
			}
		}
		Fragments fs = new Fragments(ss);
		for (int xi = 0; xi < words.size(); xi++) {
			Word x = words.get(xi);
			Fragment f = new Fragment(x);
			fs.add(f);
		}
		return fs;
	}

	public List<Word> getWords(SimpleChain polymer, int sparsity) {
		List<Word> words = new ArrayList<>();
		for (int i = 0; i < polymer.size() - params_.getWordLength(); i++) {
			if (i % sparsity == 0) {
				Word w = new Word(polymer.getResidues().subList(i, i + params_.getWordLength()));
				words.add(w);
			}
		}
		return words;
	}
}
