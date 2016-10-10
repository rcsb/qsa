package analysis;

import java.util.Random;

public class MultidimensionalSphere {
	
	public static void p(double[] ds) {
		for (double d : ds) {
			System.out.print(d + " " );
		}
		System.out.println();
	}
	public static void main(String[] args) {

		Random random = new Random();
		for (int d = 2; d <30; d++) {
			double r = 0.5*0.5;
			long in = 0;
			long out = 0;
			double[] v = new double[d];
			for (int i = 0; i < 100000; i++) {
				double sum = 0;
				for (int j = 0; j < v.length; j++) {
					v[j] = random.nextDouble() - 0.5;
					sum += v[j] * v[j];
				}
			//p(v);
				if (sum <= r) {
					in++;
				} else {
					out++;
				}
			}
			System.out.println(d + " " + (((double) in) / (in + out)) + " ");
		}
	}
}
