package algorithm;

import java.io.Serializable;

import pdb.SimpleStructure;

/**
 *
 * @author Antonin Pavelka
 */
public final class Biwords implements Serializable {

	private Biword[] biwords;
	private SimpleStructure structure;
	private Word[] words;

	/**
	 * For Kryo.
	 */
	public Biwords() {

	}

	protected Biwords(SimpleStructure structure, Biword[] fragments, Word[] words) {
		this.structure = structure;
		this.biwords = fragments;
		this.words = words;
		assert check();
	}

	private boolean check() {
		for (int i = 0; i < words.length; i++) {
			if (i != words[i].getId()) {
				return false;
			}
		}
		return true;
	}

	public Biword get(int i) {
		return biwords[i];
	}

	public Word[] getWords() {
		return words;
	}

	public int size() {
		return biwords.length;
	}

	public SimpleStructure getStructure() {
		return structure;
	}

	public Biword[] getBiwords() {
		return biwords;
	}

}
