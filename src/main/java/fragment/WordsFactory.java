package fragment;

import global.Parameters;
import structure.Residue;
import structure.SimpleChain;
import structure.SimpleStructure;
import util.Counter;

public class WordsFactory {

	private final Parameters parameters;
	private final Counter id = new Counter();
	private final int sparsity;
	private final SimpleStructure structure;

	public WordsFactory(Parameters parameters, SimpleStructure structure, int sparsity) {
		this.parameters = parameters;
		this.structure = structure;
		this.sparsity = sparsity;
	}

	public Words create() {
		Words words = new Words();
		for (SimpleChain chain : structure.getChains()) {
			addWords(chain, parameters.getWordLength(), words);
		}
		return words;
	}

	private void addWords(SimpleChain c, int wordLength, Words words) {
		for (int i = 0; i < c.size() - wordLength; i++) { // probably wrong shift or too strict?
			assert sparsity == 1; // current version assumes this, remove if that change
			if (i % sparsity != 0) {
				continue;
			}
			Residue[] fragment = new Residue[wordLength];
			System.arraycopy(c.getResidues(), i, fragment, 0, wordLength);
			if (isContinuous(fragment)) {
				Word w = new Word(id.value(), fragment);
				words.add(w);
				id.inc();
			}
		}
	}

	protected boolean isContinuous(Residue[] fragment) {
		for (int k = 0; k < fragment.length - 1; k++) {
			Residue a = fragment[k];
			Residue b = fragment[k + 1];
			if (!areConnected(a, b)) {
				return false;
			}
		}
		return true;
	}

	protected boolean areConnected(Residue a, Residue b) {
		double distance = a.getPosition().distance(b.getPosition());
		double min = parameters.getContinuousDistanceMin();
		double max = parameters.getContinuousDistanceMax();
		return min < distance && distance < max;
	}
}
