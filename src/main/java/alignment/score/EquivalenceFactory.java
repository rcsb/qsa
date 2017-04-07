package alignment.score;

import fragments.Word;
import fragments.WordsFactory;
import geometry.Point;
import geometry.Transformer;
import pdb.SimpleStructure;

/**
 * Creates residue - residue 1 : 1 mapping. Whole words are matched to prevent matching isolated
 * residues. Structures must be rotated and translated.
 */
public class EquivalenceFactory {

	private static ScorePars pars = new ScorePars();
	private static Transformer tr = new Transformer();

	public static Equivalence create(SimpleStructure sa, SimpleStructure sb) {

		Equivalence eq = new Equivalence();

		Word[] wa = getWords(sa);
		Word[] wb = getWords(sb);

		for (Word a : wa) {
			for (Word b : wb) {
				if (a.getCenter().distance(b.getCenter()) < pars.initCenterDist) {
					if (dist(a, b) < pars.initDist) {
						tr.set(a.getPoints3d(), b.getPoints3d());
						double rmsd = tr.getRmsd();
						if (rmsd < pars.rmsd) {
							
						}
					}
				}
			}
		}

		return eq;
	}
// superimpose words optionally
	// create average rotation vector and rotate

	private static double dist(Word a, Word b) {
		Point[] ap = a.getPoints();
		Point[] bp = b.getPoints();
		double d = 0;
		for (int i = 0; i < ap.length; i++) {
			d += ap[i].minus(bp[i]).size();
		}
		return d / ap.length;
	}

	private static Word[] getWords(SimpleStructure ss) {
		WordsFactory wf = new WordsFactory(ss);
		return wf.create().toArray();
	}
	
	public static void main(String[] args) {
		
	}
}
