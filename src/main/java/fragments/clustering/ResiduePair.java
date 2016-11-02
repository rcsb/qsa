package fragments.clustering;

import pdb.ResidueId;

public class ResiduePair {
	ResidueId x;
	ResidueId y;

	public ResiduePair(ResidueId xr, ResidueId yr) {
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
