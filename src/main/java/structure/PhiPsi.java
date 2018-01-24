package structure;

import java.util.List;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Group;

public class PhiPsi {

	public PhiPsi() {
		// for Kryo
	}

	private double phi;
	private double psi;

	public PhiPsi(List<Group> groups, int gi) {
		if (gi > 0 && gi < groups.size() - 1) {
			Group a = groups.get(gi - 1);
			Group b = groups.get(gi);
			Group c = groups.get(gi + 1);

			Atom[] atoms = new Atom[5];
			atoms[0] = a.getAtom("C");
			atoms[1] = b.getAtom("N");
			atoms[2] = b.getAtom("CA");
			atoms[3] = b.getAtom("C");
			atoms[4] = c.getAtom("N");

			boolean complete = true;
			for (Atom atom : atoms) {
				if (atom == null) {
					complete = false;
				}
			}
			if (complete) {
				phi = Calc.torsionAngle(atoms[0], atoms[1], atoms[2], atoms[3]);
				psi = Calc.torsionAngle(atoms[1], atoms[2], atoms[3], atoms[4]);
			}
		}
	}

	public double getPhi() {
		return phi;
	}

	public double getPsi() {
		return psi;
	}

}
