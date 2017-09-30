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
		double seqLim = Parameters.create().sequenceNeighborLimit();
		for (int i = 0; i < c.size() - wordLength; i++) {
			if (i % sparsity == 0) {
				Residue[] residues = new Residue[wordLength];
				System.arraycopy(c.getResidues(), i, residues, 0, wordLength);
				boolean unbroken = true;
				for (int k = 0; k < residues.length - 1; k++) {
					Residue a = residues[k];
					Residue b = residues[k + 1];
					double distance = a.getPosition().distance(b.getPosition());
					if (distance > seqLim || distance < 3) {
						unbroken = false;
						break;
					}
				}
				if (unbroken) {
					WordImpl w = new WordImpl(c.getSingleLetterId(), id.value(), residues);
					words.add(w);
					id.inc();
				}
			}
		}
	}
}
