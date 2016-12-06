package fragments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pdb.SimpleChain;
import pdb.SimpleStructure;
import util.Counter;

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

    public Fragments create(SimpleStructure ss, int sparsity) {
        List<Word> words = createWords(ss, sparsity);
        Counter id = new Counter();
        if (print) {
            System.out.println("***** " + ss.size());
            for (Word w : words) {
                w.print();
            }
        }
        Word[] wa = new Word[words.size()];
        words.toArray(wa);
        List<Fragment> fl = new ArrayList<>();
        for (int xi = 0; xi < words.size(); xi++) {
            for (int yi = 0; yi < xi; yi++) {
                Word x = wa[xi];
                Word y = wa[yi];
                if (x.isInContact(y, params_.getResidueContactDistance())) {
                    Fragment f = new Fragment(x, y);
                    fl.add(f);
                    fl.add(f.switchWords());
                }
            }
        }
        Fragment[] fa = new Fragment[fl.size()];
        fl.toArray(fa);
        Fragments fs = new Fragments(ss, fa, wa);
        return fs;
    }

    /*
	 * public Fragments createSingleWords(SimpleStructure ss, int sparsity) {
	 * List<Word> words = new ArrayList<>(); for (SimpleChain chain :
	 * ss.getChains()) { words.addAll(getWords(chain, sparsity)); } if (print) {
	 * System.out.println("***** " + ss.size()); for (Word w : words) {
	 * w.print(); } } Fragments fs = new Fragments(ss); for (int xi = 0; xi <
	 * words.size(); xi++) { Word x = words.get(xi); Fragment f = new
	 * Fragment(x); fs.add(f); } return fs; }
     */
    
    public List<Word> createWords(SimpleStructure ss, int sparsity) {
        List<Word> words = new ArrayList<>();
        Counter id = new Counter();
        for (SimpleChain chain : ss.getChains()) {
            words.addAll(getWords(id, chain, sparsity));
        }
        return words;

    }

    private List<Word> getWords(Counter id, SimpleChain polymer, int sparsity) {
        List<Word> words = new ArrayList<>();
        for (int i = 0; i < polymer.size() - params_.getWordLength(); i++) {
            if (i % sparsity == 0) {
                Word w = new Word(id.value(), polymer.getResidues().subList(i, i + params_.getWordLength()));
                id.inc();
                words.add(w);
            }
        }
        return words;
    }
}
