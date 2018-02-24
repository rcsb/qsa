package language;

import java.util.Objects;

public class Pair<T> {

	public final T _1;
	public final T _2;

	public Pair(T left, T right) {
		this._1 = left;
		this._2 = right;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair)) {
			return false;
		}
		Pair p = (Pair) o;
		return _1.equals(p._1) && _2.equals(p._2);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 11 * hash + Objects.hashCode(this._1);
		hash = 11 * hash + Objects.hashCode(this._2);
		return hash;
	}

	public String toString() {
		return _1.toString() + "," + _2.toString();
	}
}
