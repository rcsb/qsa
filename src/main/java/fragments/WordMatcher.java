package fragments;

import geometry.Transformer;

public class WordMatcher {
	private Word[] as, bs;
	private double[][] m;

	public WordMatcher(Word[] as, Word[] bs) {
		this.as = as;
		this.bs = bs;
		m = new double[as.length][bs.length];
		run();
	}

	private void check() {
		for (int i = 0; i < as.length; i++) {
			if (as[i].getId() != i) {
				throw new RuntimeException();
			}
		}
		for (int i = 0; i < bs.length; i++) {
			if (bs[i].getId() != i) {
				throw new RuntimeException();
			}
		}
	}

	private final void run() {
		Transformer tr = new Transformer();
		check();
		for (int ia = 0; ia < as.length; ia++) {
			for (int ib = 0; ib < bs.length; ib++) {
				tr.set(as[ia].getPoints3d(), bs[ib].getPoints3d());
				double rmsd = tr.getRmsd();
				m[ia][ib] = rmsd;
			}
		}

	}

	public double getRmsd(int a, int b) {
		return m[a][b];
	}
}
