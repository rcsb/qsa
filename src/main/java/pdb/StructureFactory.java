package pdb;

import global.io.Directories;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Element;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.ResidueNumber;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.biojava.nbio.structure.io.mmtf.MmtfStructureReader;

import org.rcsb.mmtf.dataholders.MmtfStructure;
import org.rcsb.mmtf.decoder.GenericDecoder;
import org.rcsb.mmtf.decoder.ReaderUtils;
import org.rcsb.mmtf.decoder.StructureDataToAdapter;
import pdb.cath.Cath;
import pdb.cath.CathDomainResidueFilter;

import util.MyFileUtils;

/**
 * Creates SimpleStructure object from any StructureSource.
 *
 * @author Antonin Pavelka
 */
public class StructureFactory {

	private final Directories dirs;
	private static final PDBFileReader pdbReader = new PDBFileReader();
	private final Cath cath;

	public StructureFactory(Directories dirs, Cath cath) {
		this.dirs = dirs;
		this.cath = cath;
	}

	public SimpleStructure getStructure(int id, StructureSource source) throws IOException {
		Structure s = null;
		switch (source.getType()) {
			case StructureSource.PDB_CODE:
			case StructureSource.PDB_CODE_CHAIN:
			case StructureSource.CATH_DOMAIN:
				Path mmtfPath = dirs.getMmtf(source.getPdbCode());
				if (!Files.exists(mmtfPath)) {
					try {
						MyFileUtils.download("http://mmtf.rcsb.org/v1.0/full/" + source.getPdbCode(), mmtfPath);
					} catch (Exception e) {
						// some files might be missing (obsoleted, models)
					}
				}
				try {
					if (Files.exists(mmtfPath)) { // if MMTF format failed, try PDB
						s = parseMmtfToBiojava(mmtfPath);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				if (s == null) {
					Path pdbPath = dirs.getPdb(source.getPdbCode());
					if (!Files.exists(pdbPath)) {
						MyFileUtils.download("https://files.rcsb.org/download/" + source.getPdbCode() + ".pdb.gz",
							pdbPath);
					}
					s = pdbReader.getStructure(pdbPath.toFile());
				}
				break;
			case StructureSource.FILE:
				if (source.isMmtf()) {
					s = parseMmtfToBiojava(source.getFile().toPath());
				} else if (source.isPdb()) {
					s = pdbReader.getStructure(source.getFile());
				} else {
					throw new IOException("Unknown structure file ending: " + source.getFile().getAbsolutePath());
				}
				break;
		}
		ResidueFilter filter;
		if (source.getType() == StructureSource.CATH_DOMAIN) {
			filter = new CathDomainResidueFilter(cath.getDomain(source));
		} else {
			filter = new EmptyResidueFilter();
		}
		SimpleStructure ss = convertProteinChains(s.getModel(0), id, source, filter);
		if (source.specifiesChain()) {
			ss.removeChainsByNameExcept(source.getChain());
		}
		return ss;
	}

	public static void downloadPdbFile(StructureSource source, File pdbFile) throws IOException {
		MyFileUtils.download("https://files.rcsb.org/download/" + source.getPdbCode() + ".pdb.gz", pdbFile.toPath());
	}

	private Structure parseMmtfToBiojava(Path p) throws IOException {
		MmtfStructureReader mmtfStructureReader = new MmtfStructureReader();
		byte[] array = Files.readAllBytes(p);
		array = ReaderUtils.deflateGzip(array);
		ByteArrayInputStream bai = new ByteArrayInputStream(array);
		MmtfStructure mmtf = ReaderUtils.getDataFromInputStream(bai);
		GenericDecoder gd = new GenericDecoder(mmtf);
		new StructureDataToAdapter(gd, mmtfStructureReader);
		return mmtfStructureReader.getStructure();
	}

	private SimpleStructure convertProteinChains(List<Chain> chains, int id, StructureSource source,
		ResidueFilter filter) {

		int residueIndex = 0;
		SimpleStructure ss = new SimpleStructure(id, source);
		for (Chain chain : chains) {
			if (!chain.isProtein()) {
				continue;
			}
			ChainId cid = new ChainId(chain.getId(), chain.getName());
			List<Residue> residues = new ArrayList<>();
			int index = 0;
			List<Group> groups = chain.getAtomGroups();
			for (int gi = 0; gi < groups.size(); gi++) {
				Group g = chain.getAtomGroup(gi);
				if (source.hasPdbCode()) {
					ResidueNumber rn = g.getResidueNumber();
					assert filter != null;
					assert source != null;
					assert source.getPdbCode() != null;
					assert chain.getName() != null;
					assert rn.getSeqNum() != null;
					if (filter.reject(source.getPdbCode(), chain.getName(), rn.getSeqNum(), rn.getInsCode())) {
						continue;
					}
				}

				Double phi = null;
				Double psi = null;
				Atom[] phiPsiAtoms = new Atom[5];
				if (gi > 0 && gi < groups.size() - 1) {

					Group a = groups.get(gi - 1);
					Group b = groups.get(gi);
					Group c = groups.get(gi + 1);

					phiPsiAtoms[0] = a.getAtom("C");
					phiPsiAtoms[1] = b.getAtom("N");
					phiPsiAtoms[2] = b.getAtom("CA");
					phiPsiAtoms[3] = b.getAtom("C");
					phiPsiAtoms[4] = c.getAtom("N");

					boolean quit = false;
					for (int i = 0; i < phiPsiAtoms.length; i++) {
						if (phiPsiAtoms[i] == null) {
							quit = true;
						}
					}
					if (!quit) {
						phi = Calc.torsionAngle(phiPsiAtoms[0], phiPsiAtoms[1], phiPsiAtoms[2], phiPsiAtoms[3]);
						psi = Calc.torsionAngle(phiPsiAtoms[1], phiPsiAtoms[2], phiPsiAtoms[3], phiPsiAtoms[4]);
					}
				}

				int atomCounter = 0;
				for (Atom a : g.getAtoms()) {
					if (!a.getElement().equals(Element.H)) {
						atomCounter++;
					}
				}
				double[][] atoms = new double[atomCounter][3];
				String[] atomNames = new String[atomCounter];
				double[] point;
				double[] carbonAlpha = null;
				Integer serial = null;
				boolean caFound = false;
				int i = 0;
				for (Atom a : g.getAtoms()) {
					if (a.getElement().equals(Element.H)) {
						continue;
					}
					point = new double[3];
					point[0] = a.getX();
					point[1] = a.getY();
					point[2] = a.getZ();
					atoms[i] = point;
					atomNames[i] = a.getName();
					if (a.getName().toUpperCase().equals("CA")) {
						carbonAlpha = point;
						serial = a.getPDBserial();
						caFound = true;
					}
					i++;
				}
				if (caFound) {
					ResidueId rid = new ResidueId(cid, index);
					Residue r = new Residue(residueIndex++, rid, serial, carbonAlpha, atoms,
						atomNames, phi, psi, phiPsiAtoms, g.getPDBName());
					residues.add(r);
					index++;
				}
			}
			Residue[] a = new Residue[residues.size()];
			residues.toArray(a);
			SimpleChain sic = new SimpleChain(cid, a);
			ss.addChain(sic);
		}
		return ss;
	}

}
