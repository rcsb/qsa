package fragments;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Words implements Iterable<WordImpl> {

	private List<WordImpl> words = new ArrayList<>();

	public void add(WordImpl w) {
		words.add(w);
	}

	@Override
	public Iterator<WordImpl> iterator() {
		return words.iterator();
	}

	public WordImpl[] toArray() {
		WordImpl[] a = new WordImpl[words.size()];
		words.toArray(a);
		return a;
	}
}
