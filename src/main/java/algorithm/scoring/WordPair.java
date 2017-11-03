package algorithm.scoring;

import algorithm.WordImpl;

/**
 *
 * @author Antonin Pavelka
 */
public class WordPair implements Comparable<WordPair> {

	public final WordImpl a, b;
	double dist;

	public WordPair(WordImpl a, WordImpl b, double dist) {
		this.a = a;
		this.b = b;
		this.dist = dist;
	}

	@Override
	public int compareTo(WordPair o) {
		return Double.compare(this.dist, o.dist);
	}
}
