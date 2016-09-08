package spark.interfaces;

public class AlignmentWrapper implements Alignment {
	private Object o;

	public AlignmentWrapper(Object o) {
		this.o = o;
	}

	public Object get() {
		return o;
	}
}
