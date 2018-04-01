package language.search;

import java.util.function.BiFunction;

/**
 *
 * Maintains the best (min or max) object based on the number associated with the object.
 *
 * @author Antonin Pavelka
 */
public class Best<T> {

	private T object;
	private double property;
	private final BiFunction<Double, Double, Boolean> isFirstBetter;

	private Best(BiFunction<Double, Double, Boolean> comparison) {
		this.isFirstBetter = comparison;
	}

	public static <T> Best createSmallest() {
		BiFunction<Double, Double, Boolean> isSmaller = (x, y) -> {
			return x < y;
		};
		Best<T> best = new Best(isSmaller);
		return best;
	}

	public static <T> Best createGreatest() {
		BiFunction<Double, Double, Boolean> isSmaller = (x, y) -> {
			return x > y;
		};
		Best<T> best = new Best(isSmaller);
		return best;
	}

	public void update(T otherObject, double otherProperty) {
		if (object == null) {
			object = otherObject;
			property = otherProperty;
		} else {
			if (isFirstBetter.apply(otherProperty, property)) {
				object = otherObject;
				property = otherProperty;
			}
		}
	}

	public T getBestObject() {
		return object;
	}

	public double getBestProperty() {
		return property;
	}

}
