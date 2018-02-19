package fragment.alignment;

import structure.Residue;

public class ResiduePair {

	public final Residue x;
	public final Residue y;

	public ResiduePair(Residue xr, Residue yr) {
		this.x = xr;
		this.y = yr;
	}

	@Override
	public int hashCode() {
		return Integer.MIN_VALUE + x.hashCode() * 10001 + y.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		ResiduePair rp = (ResiduePair) o;
		return x.equals(rp.x) && y.equals(rp.y);
	}

}
