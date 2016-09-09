package alignment;

import java.util.List;

import org.biojava.nbio.structure.AminoAcidImpl;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.AtomImpl;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.ChainImpl;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.ResidueNumber;
import org.biojava.nbio.structure.align.StructureAlignment;
import org.biojava.nbio.structure.align.StructureAlignmentFactory;
import org.biojava.nbio.structure.align.fatcat.FatCatRigid;
import org.biojava.nbio.structure.align.fatcat.calc.FatCatParameters;
import org.biojava.nbio.structure.align.model.AFPChain;

import pdb.Residue;
import pdb.SimpleStructure;
import spark.interfaces.AlignablePair;
import spark.interfaces.Alignment;
import spark.interfaces.AlignmentWrapper;
import spark.interfaces.StructureAlignmentAlgorithm;

public class Fatcat implements StructureAlignmentAlgorithm {
	private int index;

	@Override
	public Alignment align(AlignablePair pair) {
		index = 0;
		Atom[] ca1 = ca(pair.getA());
		index = 0;
		Atom[] ca2 = ca(pair.getB());
		String name1 = "A";
		String name2 = "B";
		try {
			// To run FATCAT in the flexible variant say
			// FatCatFlexible.algorithmName below
			StructureAlignment algorithm = StructureAlignmentFactory.getAlgorithm(FatCatRigid.algorithmName);
			// get default parameters
			FatCatParameters params = new FatCatParameters();
			AFPChain afpChain = algorithm.align(ca1, ca2, params);
			afpChain.setName1(name1);
			afpChain.setName2(name2);
			afpChain.getTMScore();
			// show original FATCAT output:
			// System.out.println(afpChain.toFatcat(ca1, ca2));
			// show a nice summary print
			// System.out.println(AfpChainWriter.toWebSiteDisplay(afpChain, ca1,
			// ca2));
			// print rotation matrices
			// System.out.println(afpChain.toRotMat());
			// System.out.println(afpChain.toCE(ca1, ca2));
			// print XML representation
			// System.out.println(AFPChainXMLConverter.toXML(afpChain,ca1,ca2));
			Alignment a = new AlignmentWrapper(afpChain);
			return a;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private Atom ra(Residue r) {
		return dummy(r.getCoords());
	}

	private Atom[] ca(SimpleStructure c) {
		if (c.numberOfChains() > 1) {
			throw new IllegalStateException("Fatcat works only with a single chain: " + c.numberOfChains());
		}
		List<Residue> rs = c.getFirstChain().getResidues();
		Atom[] as = new Atom[rs.size()];
		for (int i = 0; i < rs.size(); i++) {
			as[i] = ra(rs.get(i));
		}
		return as;
	}

	private Atom dummy(double[] coords) {
		Atom ca1;
		Chain chain1 = new ChainImpl();
		ca1 = new AtomImpl();
		ca1.setName("CA");
		ca1.setCoords(coords);
		Group aa = new AminoAcidImpl();
		aa.setPDBName("GLY");
		aa.setResidueNumber(ResidueNumber.fromString("" + (index++)));
		aa.addAtom(ca1);
		chain1.addGroup(aa);
		return ca1;
	}

}
