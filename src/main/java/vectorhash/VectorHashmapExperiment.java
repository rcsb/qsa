package vectorhash;

import java.util.HashMap;
import java.util.Random;
import util.Timer;

public class VectorHashmapExperiment {

	//public static void main(String[] args) {
	// try multidimensional array, if fast enough, proceed, also check correctnes
	// if not, merge several levels? create int from several coordinates
	//}
	public static void main(String[] args) {
		Timer.start();
		HashMap<Vector, Integer> map = new HashMap<>();
		HashMap<Integer, Integer> mapi = new HashMap<>();
		Random random = new Random();
		for (long i = 0; i < 10 * 1000L * 1000L; i++) {
			Vector v = new Vector();
			map.put(v, (int) i);
			//mapi.put(random.nextInt(), (int) i);
		}
		Timer.stop();
		System.out.println(Timer.get());

		Vector r = new Vector();
		Timer.start();
		int hit = 0;
		for (Vector v : map.keySet()) {
			if (map.get(v) != null) {
				hit++;
			}
			//if (map.get(r) != null) {
			//	hit++;
			//}
		}

		/*for (int v : mapi.keySet()) {
			if (map.get(v) != null) {
				hit++;
			}
			if (map.get(564) != null) {
				hit++;
			}
		}*/
		System.out.println(hit + " " + map.size());

		Timer.stop();
		System.out.println(Timer.get());

	}

}
