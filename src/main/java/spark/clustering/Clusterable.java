package spark.clustering;

import java.io.Serializable;

public interface Clusterable<T> extends Serializable {
	public double distance(T c);

}
