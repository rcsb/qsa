package spark.interfaces;

import java.io.Serializable;

public abstract class Alignment implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String SEP = ",";
	public static final String NEW_LINE = "\n";

	public abstract String getLine();

//	public abstract String getHeader();
}
