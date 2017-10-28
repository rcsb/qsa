package pdb;

import io.Directories;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.biojava.nbio.structure.io.mmtf.MmtfStructureReader;

import org.rcsb.mmtf.dataholders.MmtfStructure;
import org.rcsb.mmtf.decoder.GenericDecoder;
import org.rcsb.mmtf.decoder.ReaderUtils;
import org.rcsb.mmtf.decoder.StructureDataToAdapter;

import util.MyFileUtils;

/**
 * Creates SimpleStructure object from PDB code or file, using BioJava.
 *
 * @author Antonin Pavelka
 */
public class StructureFactory {

	private Directories dirs;
	private static PDBFileReader pdbReader = new PDBFileReader();

	public StructureFactory(Directories dirs) {
		this.dirs = dirs;
	}

	public SimpleStructure getStructure(StructureReference ref, int id) throws IOException {
		Structure s = null;
		switch (ref.getType()) {
			case StructureReference.PDB_CODE:
				Path mmtfPath = dirs.getMmtf(ref.getPdbCode());
				if (!Files.exists(mmtfPath)) {
					try {
						MyFileUtils.download("http://mmtf.rcsb.org/v1.0/full/" + ref.getPdbCode(), mmtfPath);
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
					Path pdbPath = dirs.getPdb(ref.getPdbCode());
					if (!Files.exists(pdbPath)) {
						MyFileUtils.download("https://files.rcsb.org/download/" + ref.getPdbCode() + ".pdb.gz",
							pdbPath);
					}
					s = pdbReader.getStructure(pdbPath.toFile());
				}
				break;
			case StructureReference.FILE:
				if (ref.isMmtf()) {
					s = parseMmtfToBiojava(ref.getFile().toPath());
				} else if (ref.isPdb()) {
					s = pdbReader.getStructure(ref.getFile());
				} else {
					throw new IOException("Unknown structure file ending: " + ref.getFile().getAbsolutePath());
				}
				break;
		}
		return convertFirstModel(s, id);
	}

	private SimpleStructure convertFirstModel(Structure s, int id) {
		return convertProteinChains(s.getModel(0), id, s.getPDBCode());
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

	// format e.g. 1cv2A or 1egf
	/*public List<Chain> getSingleChain(String id) throws IOException {
		List<Chain> one;
		if (id.length() == 4 || id.length() == 5) { // PDB code
			if (id.length() == 4) {
				one = getStructure(id).getChains();
				assert one.size() >= 1 : id;
			} else {
				String code = id.substring(0, 4);
				String chain = id.substring(4, 5);
				List<Chain> chains = getStructure(code).getChains();
				one = StructureFactory.filter(chains, chain);
				assert one.size() >= 1;
			}
		} else { // CATH domain id
			one = getStructurePdb(id).getChains();
		}
		assert one.size() >= 1;
		if (one.size() > 1) {
			one = select(one);

		}
		assert one.size() >= 1;
		assert one.size() == 1;
		return one;
	}

	private List<Chain> select(List<Chain> chains) {
		Chain best = null;
		for (Chain c : chains) {
			if (best == null) {
				best = c;
			} else {
				if (c.isProtein() && !best.isProtein()) {
					best = c;
				} else if (c.isProtein() && best.isProtein()
					&& c.getAtomGroups().size() > best.getAtomGroups().size()) {
					best = c;
				}
			}
		}
		List<Chain> result = new ArrayList<>();
		result.add(best);
		assert best.getAtomGroups().size() > 0;
		assert best.isProtein();
		return result;
	}
	 */
 /*public Structure getStructureMmtf(String pdbCode) {
		Path mmtf = dirs.getMmtf(pdbCode);
		if (!Files.exists(mmtf)) {
			try {
				MyFileUtils.download("http://mmtf.rcsb.org/v1.0/full/" + pdbCode, mmtf);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Structure s = parseMmtfToBiojava(mmtf);
		return s;
	}*/
 /*public static Atom[] getAtoms(List<Chain> chains) {
		List<Atom> atoms = new ArrayList<>();
		for (Chain c : chains) {
			for (Group g : c.getAtomGroups()) {
				if (g.hasAtom(CA_ATOM_NAME)
					&& g.getAtom(CA_ATOM_NAME).getElement() == Element.C) {
					atoms.add(g.getAtom(CA_ATOM_NAME));
				}
			}
		}
		return atoms.toArray(new Atom[atoms.size()]);
	}*/

 /*public SimpleStructure getSimpleStructurePdb(String filename) throws IOException {
		throw new UnsupportedOperationException();
		//Path p = dirs.getCathFile(filename);
		//return parsePdb(p.toFile());
	}

	public Structure getStructurePdb(String filename) throws IOException {
		Path p = dirs.getCathFile(filename);
		return pdbReader.getStructure(p.toFile());
	}

	public static SimpleStructure parsePdb(File f) throws IOException {
		throw new UnsupportedOperationException();
		//return convertProteinChains(pdbReader.getStructure(f), f.getName());
	}

	public static List<Chain> filter(List<Chain> chains, String chain) {
		StringBuilder sb = new StringBuilder();
		for (Chain c : chains) {
			sb.append(c.getName()).append(",");
		}
		List<Chain> result = new ArrayList<>();
		for (int i = chains.size() - 1; i >= 0; i--) {
			Chain c = chains.get(i);
			//System.out.println("  " + chain + " == " + c.getName() +  " " + c.getId() + c.getChainID() + " " + c.getInternalChainID() );
			if (c.getName().toLowerCase().equals(chain.toLowerCase())) {
				result.add(c);
			}
		}
		return result;
	}
	 */
	// TODO filter out H atoms
	private SimpleStructure convertProteinChains(List<Chain> chains, int id, String pdbCode) {
		int residueIndex = 0;
		SimpleStructure ss = new SimpleStructure(id, pdbCode);
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
				double[][] atoms = new double[g.getAtoms().size()][3];
				String[] atomNames = new String[g.getAtoms().size()];
				double[] point;
				double[] carbonAlpha = null;
				Integer serial = null;
				boolean caFound = false;
				for (int i = 0; i < g.getAtoms().size(); i++) {
					Atom a = g.getAtoms().get(i);
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
				}
				if (caFound) {
					ResidueId rid = new ResidueId(cid, index);
					Residue r = new Residue(residueIndex++, rid, serial, carbonAlpha, atoms,
						atomNames, phi, psi, phiPsiAtoms);
					residues.add(r);
					index++;
				}
			}
			Residue[] a = new Residue[residues.size()];
			residues.toArray(a);
			SimpleChain sic = new SimpleChain(cid, a);
			ss.addChain(sic);
		}
		ss.size();
		return ss;
	}

}
