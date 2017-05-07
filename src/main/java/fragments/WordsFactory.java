package fragments;

import pdb.Residue;
import pdb.SimpleChain;
import pdb.SimpleStructure;
import util.Counter;

public class WordsFactory {

	private final Counter id = new Counter();
	private int sparsity = 1;
	private int wordLength;
	private final SimpleStructure ss;

	public WordsFactory(SimpleStructure ss, int wordLength) {
		this.ss = ss;
		this.wordLength = wordLength;
	}

	public void setSparsity(int sparsity) {
		this.sparsity = sparsity;
	}

	public Words create() {
		Words words = new Words();
		for (SimpleChain c : ss.getChains()) {
			addWords(c, wordLength, words);
		}
		return words;
	}

	private void addWords(SimpleChain c, int wordLength, Words words) {
		for (int i = 0; i < c.size() - wordLength; i++) {
			if (i % sparsity == 0) {
				Residue[] residues = new Residue[wordLength];
				System.arraycopy(c.getResidues(), i, residues, 0, wordLength);
				WordImpl w = new WordImpl(id.value(), residues);
				//WordImpl w = new WordImpl(id.value(), c.getResidues().subList(i, i + wordLength));
				words.add(w);
				id.inc();
				//words.add(w.invert(id.value()));
				//id.inc();
			}
		}
	}
}
