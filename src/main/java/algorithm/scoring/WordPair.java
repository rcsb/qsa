package algorithm.scoring;

import fragment.word.Word;

/**
 *
 * @author Antonin Pavelka
 */
public class WordPair implements Comparable<WordPair> {

	public final Word a, b;
	double dist;

	public WordPair(Word a, Word b, double dist) {
		this.a = a;
		this.b = b;
		this.dist = dist;
	}

	@Override
	public int compareTo(WordPair o) {
		return Double.compare(this.dist, o.dist);
	}
}
