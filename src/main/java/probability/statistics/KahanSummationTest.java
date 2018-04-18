package probability.statistics;

import util.Timer;

/**
 * https://rosettacode.org/wiki/Kahan_summation
 */
public class KahanSummationTest {

	public static void main(String[] args) {
		Timer.start();
		for (int i = 0; i < 10; i++) {
			testFloat();
		}
		Timer.stop();
		System.out.println(Timer.get());
		Timer.start();
		for (int i = 0; i < 10; i++) {
			testDouble();
		}
		Timer.stop();
		System.out.println(Timer.get());
		Timer.start();
		for (int i = 0; i < 1000; i++) {
			testFloat();
		}
		Timer.stop();
		System.out.println(Timer.get());
	}

	private static void testFloat() {
		KahanSumFloat kahanSum = new KahanSumFloat();
		for (int i = 0; i < 100000000; i++) {
			float f = 0.01f;
			kahanSum.add(f);
		}
	}

	private static void testDouble() {
		KahanSumDouble kahanSum = new KahanSumDouble();
		for (int i = 0; i < 100000000; i++) {
			double d = 0.01;
			kahanSum.add(d);
		}
	}

}
