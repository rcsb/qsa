package fragments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
					Biword f = new Biword(ss.getId(), ss, x, y);
					fl.add(f);
					fl.add(f.switchWords());
				}
			}
		}
		Biword[] fa = new Biword[fl.size()];
		fl.toArray(fa);
		Biwords fs = new Biwords(ss, fa, wa);
		return fs;
	}
}
