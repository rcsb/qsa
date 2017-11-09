package util;

public class Counter {

	int i = 0;

	public Counter() {

	}

	public Counter(int initialValue) {
		this.i = initialValue;
	}

	public int value() {
		return i;
	}

	public void inc() {
		i++;
	}
}
