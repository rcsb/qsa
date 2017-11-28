package util;

public class Counter {

	int i = 0;

	public Counter() {

	}

	public Counter(int initialValue) {
		this.i = initialValue;
	}

	public synchronized int value() {
		return i;
	}

	public synchronized int inc() {
		int result = i;
		i++;
		return result;
	}
}
