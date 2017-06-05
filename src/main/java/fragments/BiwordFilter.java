package fragments;

public class BiwordFilter implements Filter<Biword> {

	private final double[] x;
	private final double[] maxDiffs;
	
	public BiwordFilter(Biword template, double[] maxDiffs) {
		this.x = template.getCoords();
		this.maxDiffs = maxDiffs;
	}
	
	@Override
	public boolean include(Biword biword) {
		double[] y = biword.getCoords();
		for (int i = 0; i < x.length; i++) {
			double diff = Math.abs(x[i] - y[i]);
			if (diff > maxDiffs[i]) {
				return false;
			}
		}
		return true;
	}

}
