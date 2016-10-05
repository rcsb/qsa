package analysis;

import javax.vecmath.Point3d;

import org.biojava.nbio.structure.AminoAcidImpl;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.AtomImpl;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.ChainImpl;
import org.biojava.nbio.structure.Element;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.ResidueNumber;
import org.biojava.nbio.structure.StructureImpl;
import org.biojava.nbio.structure.gui.BiojavaJmol;

import pdb.PdbLine;

public class JmolVisualizer {
	StructureImpl s;
	private static char ci = 'A';
	private static int model = 0;

	public JmolVisualizer() {
		s = new StructureImpl();
	}

	public void addToPdb() {
		PdbLine pl;
		
	}
	
	public void add(Point3d[] points) {
		Atom[] atoms = new Atom[points.length];
		Chain chain = new ChainImpl();
		chain.setId("" + ci);
		chain.setChainID("" + ci);
		chain.setName("" + ci);
		ci++;
		for (int i = 0; i < atoms.length; i++) {
			Atom a = new AtomImpl();
			a = new AtomImpl();
			a.setName("CA");
			a.setElement(Element.C);
			a.setAltLoc(Character.valueOf(' '));
			a.setPDBserial(i);
			double dd = 1;
			a.setX(points[i].x / dd);
			a.setY(points[i].y / dd);
			a.setZ(points[i].z / dd);
		
			atoms[i] = a;
			
			Group aa = new AminoAcidImpl();
			aa.setPDBName("GLY");
			aa.setResidueNumber(ResidueNumber.fromString(i + ""));
			aa.addAtom(atoms[i]);
			chain.addGroup(aa);			
		}
		s.addChain(chain);
		
		// connect (atomno=100 and chain=A) (atomno=20 and chain=A) 
		
		// FileConvert fc = new FileConvert(s);
		// System.out.println(fc.toPDB());
	}

	public void display() {
		try {
			BiojavaJmol jmolPanel = new BiojavaJmol();
			jmolPanel.setStructure(s);
			jmolPanel.evalString("select chain=A; color green;");
			jmolPanel.evalString("select chain=B; color red;");
			jmolPanel.evalString("connect (atomno=100 and chain=A) (atomno=20 and chain=A)");
			
			// jmolPanel.evalString("select *; spacefill off; wireframe off;
			// backbone 0.4; ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * public static void main(String[] args) { Visualizer v = new Visualizer();
	 * Point3d[] ps = { new Point3d(1, 20, 0), new Point3d(2, 0, 0), new
	 * Point3d(3, 0, 0), new Point3d(4, 0, 0), new Point3d(5, 10, 0), new
	 * Point3d(7, 1, 2) }; System.out.println("aaagga"); v.add(ps); v.diplay();
	 * }
	 */
}
