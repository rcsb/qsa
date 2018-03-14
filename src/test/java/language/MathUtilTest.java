package language;

import junit.framework.TestCase;

/**
 *
 * @author Antonin Pavelka
 */
public class MathUtilTest extends TestCase {

	public MathUtilTest(String testName) {
		super(testName);
	}

	public void testWrap() {
		for (int i = 0; i < 10; i++) {
			double min = Util.random();
			double max = min + Util.randomNonnegative();
			double valueInside = (max - min) * Math.random() + min;
			double shift = Math.round(Util.random());
			double value = valueInside + shift * (max - min);
			double x = valueInside;
			double y = MathUtil.wrap(value, min, max);
			if (!Util.similar(x, y)) {
				System.out.println(min + " " + max);
				System.out.println("inside " + valueInside);
				System.out.println("shift " + shift);
				System.out.println("value " + value);
				System.out.println("<>");
				System.out.println(x);
				System.out.println(y);
				throw new RuntimeException();
			}
		}
	}

}
