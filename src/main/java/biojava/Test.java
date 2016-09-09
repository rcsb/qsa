package biojava;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.align.util.AtomCache;

public class Test {
	public static void main(String[] args) {
		System.out.println("now to biojava:");
		try {
			AtomCache cache = new AtomCache();
			long timeS = System.currentTimeMillis();
			Structure s = cache.getStructure("4hhb");
			long timeE = System.currentTimeMillis();
			System.out.println("took " + (timeE - timeS) + " ms. to load structure");
			System.out.println(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
