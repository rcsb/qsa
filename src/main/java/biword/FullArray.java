package biword;

public class FullArray {

	// ID of biword can be int
	// last dimension is bucket
	int[][][][][][][] array = new int[10][10][10][10][10][10][10]; // can be made partially sparse by top down initalization

	/// how empty it will be...
	// define coordinate system
	// first word - 1. axis
	// from center perpendicular towards other word: 2. axis, duplicate if uncertain
	// 2. word determines orientation of 3. axis, duplicate if uncertain
	// 
	// distance to second word center
	// angle to second word center (duplicate)
	// angle of words (duplicate)
	// torsion angle (duplicate)
	// CA-CB angle of middle residue
	// mostly alpha, mostly beta, else (duplicate uncertain but rarely)
	// length of words
	
	// see distributions in weka
	
	// or just place it into space and learn? especially chebyshev would be nice
	
	// focus on 5 residues (CA-CB for interfaces maybe)
	// 2x phi, psi = 4
	// 6 for orientation, distance, angles, if last two uncertain, leave them in one bucket
	
	// ! focus only on local contacts - at most distance of two beta sheets, but rather:
	// (maximum distance of residues touching), require they actually touch by atoms (all, just in case)
	
	
	public static void main(String[] args) {

	}

	public void add(int[] vector) {
		
		
		for (int d = 0; d < vector.length; d++) {
			Object o = array[0][0];
			
			//if (array[0] == null)
		}
	}

	public void query(int[] vector) {

	}

}
