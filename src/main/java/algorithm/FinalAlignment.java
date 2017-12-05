package algorithm;

import algorithm.scoring.WordAlignmentFactory;
import algorithm.scoring.ResidueAlignment;
import fragments.alignment.ExpansionAlignment;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import pdb.Residue;

import pdb.SimpleStructure;
import geometry.SuperPositionQCP;
import global.Parameters;

public class FinalAlignment implements Comparable<FinalAlignment> {

	private final Parameters parameters;
	private final SimpleStructure a;
	private final SimpleStructure b;
	private SimpleStructure tb;
	private final Residue[][] initialPairing;
	private ResidueAlignment residueAlignment;
	private double rmsd;
	private Matrix4d matrix;
	private final double initialTmScore;
	private double tmScore;
	private Point3d[][] points;
	private final ExpansionAlignment expansion;

	public FinalAlignment(Parameters parameters, SimpleStructure a, SimpleStructure b, Residue[][] initialPairing,
		double initialTmScore, ExpansionAlignment expansion) {
		
		
		//for (int i = 0; i < initialPairing[0].length; i++) {
			
		//}
		
		this.parameters = parameters;
		this.a = a;
		this.b = b;
		this.initialPairing = initialPairing;
		this.initialTmScore = initialTmScore;
		this.expansion = expansion;
		alignBiwords();
	}

	// 1st step
	private void alignBiwords() { // is this just very few words? or what is it?
		// how come this tmScore can be bigger than 1? asserts?
		// repeated points? rewrite so that algorithm is clear
		matrix = computeMatrix(initialPairing);
		Point3d[] xs = points[0];
		Point3d[] ys = points[1];
		for (int i = 0; i < ys.length; i++) {
			Point3d y = ys[i];
			matrix.transform(y);
		}
		tmScore = ResidueAlignment.getTmScore(xs, ys, a.size());

		System.out.println(xs.length + " " + ys.length + " " + a.size() + " " + tmScore);

	}

	private Matrix4d computeMatrix(Residue[][] rs) {
		SuperPositionQCP qcp = new SuperPositionQCP();
		Point3d[][] newPoints = {getPoints(rs[0]), getPoints(rs[1])};
		points = newPoints;
		qcp.set(points[0], points[1]);
		Matrix4d m = qcp.getTransformationMatrix();
		rmsd = qcp.getRmsd();
		return m;
	}

	public ExpansionAlignment getExpansionAlignemnt() {
		return expansion;
	}

	public double getInitialTmScore() {
		return initialTmScore;
	}

	public ResidueAlignment getResidueAlignment() {
		return residueAlignment;
	}

	public int getLength() {
		return initialPairing[0].length;
	}

	public double getRmsd() {
		return rmsd;
	}

	@Override
	public int compareTo(FinalAlignment other) {
		return Double.compare(other.tmScore, tmScore);
	}

	public Residue[][] getInitialPairing() {
		return initialPairing;
	}

	private Point3d[] getPoints(Residue[] rs) {
		Point3d[] ps = new Point3d[rs.length];
		for (int i = 0; i < rs.length; i++) {
			ps[i] = rs[i].getPosition3d();
		}
		return ps;
	}

	// 2nd step
	// TODO refactor, repeat one more time, rewrite with cycle
	// profile slow cases separatelly?
	// establish largest connected component, possibly with cross-checks and reject FP based on its size not allowing tm score above 0.3
	// ... but only if assembly is problem and it would differ in complexity, probably would, what is alg.
	// ... revive checks, for assembly and components? for twists along hinge helix... how?
	// filter alignments the same way, by number of matched residues if too low, even for initial
	// grid with buffer, is it in sep. proj.?
	public void refine() {

		System.out.println("before ref " + tmScore + " o " + getTmScore());

		//System.out.println("a " + a.size() + " " + a.getSource().getPdbCode());
		//System.out.println("b " + a.size());
		tb = new SimpleStructure(b.getId(), b);
		tb.transform(matrix);
		WordAlignmentFactory waf = new WordAlignmentFactory(parameters);
		residueAlignment = waf.create(a, tb);
		if (residueAlignment.getResidueParing()[0].length >= initialPairing.length / 2 + 1) { // TODO to params
			matrix = computeMatrix(residueAlignment.getResidueParing());
			tb.transform(matrix);
			waf = new WordAlignmentFactory(parameters);
			ResidueAlignment eq2 = waf.create(a, tb);
			if (eq2.getTmScore() > residueAlignment.getTmScore()) {
				residueAlignment = eq2;
			}
		}

		tmScore = residueAlignment.getTmScore();
		System.out.println("after ref " + tmScore);
		System.out.println("");
	}

	public SimpleStructure getSecondTransformedStructure() {
		return tb;
	}

	public SimpleStructure getFirst() {
		return a;
	}

	public double getTmScore() {
		return tmScore;
	}

}
