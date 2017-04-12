package util;

import java.util.Objects;

public class Pair<T> {

	public final T x;
	public final T y;

	public Pair(T left, T right) {
		this.x = left;
		this.y = right;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair)) {
			return false;
		}
		Pair p = (Pair) o;
		return x.equals(p.x) && y.equals(p.y);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 11 * hash + Objects.hashCode(this.x);
		hash = 11 * hash + Objects.hashCode(this.y);
		return hash;
	}

	public String toString() {
		return x.toString() + "," + y.toString();
	}
}
