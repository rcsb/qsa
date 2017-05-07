package fragments;

import java.io.Serializable;

import pdb.SimpleStructure;

/**
 *
 * @author Antonin Pavelka
 */
public final class Biwords implements Serializable {

	private final Biword[] biwords;
	private final SimpleStructure structure;
	private final WordImpl[] words;

	protected Biwords(SimpleStructure structure, Biword[] fragments, WordImpl[] words) {
		this.structure = structure;
		this.biwords = fragments;
		this.words = words;
	}

	public Biword get(int i) {
		return biwords[i];
	}

	public WordImpl[] getWords() {
		return words;
	}

	public int size() {
		return biwords.length;
	}

	public SimpleStructure getStructure() {
		return structure;
	}

	public Biword[] getFragments() {
		return biwords;
	}

}
