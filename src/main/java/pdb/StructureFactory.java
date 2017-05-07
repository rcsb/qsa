package pdb;

import geometry.Point;
import io.Directories;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Element;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import static org.biojava.nbio.structure.StructureTools.CA_ATOM_NAME;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.biojava.nbio.structure.io.mmtf.MmtfStructureReader;

import org.rcsb.mmtf.api.StructureDataInterface;
import org.rcsb.mmtf.dataholders.MmtfStructure;
import org.rcsb.mmtf.decoder.GenericDecoder;
import org.rcsb.mmtf.decoder.ReaderUtils;
import org.rcsb.mmtf.decoder.StructureDataToAdapter;

import util.MyFileUtils;

public class StructureFactory {

	private Directories dirs;

	PrintStream out = System.out;

	public StructureFactory(Directories dirs) {
		this.dirs = dirs;
	}

	private void print(String s) {
		out.println(s);
	}

	private void print(int s) {
		out.println(s);
	}

	private void print(float s) {
		out.println(s);
	}

	private void print() {
		out.println();
	}

	/*@Deprecated
	public SimpleStructure getStructure(String pdbCode, ChainId chainId) {
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
		try {
			MmtfStructure mmtfData = MyReaderUtils.getDataFromFile(mmtf);
			StructureDataInterface s = new GenericDecoder(mmtfData);
			return getStructure(pdbCode, s, chainId);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			out.close();
		}
	}*/

	public Structure getStructure(String pdbCode) throws IOException {
		Structure s = null;
		Path mmtfPath = dirs.getMmtf(pdbCode);
		if (!Files.exists(mmtfPath)) {
			try {
				MyFileUtils.download("http://mmtf.rcsb.org/v1.0/full/" + pdbCode, mmtfPath);
			} catch (Exception e) {
				// no worries, some files are missing (obsoleted, models)
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
			Path pdbPath = dirs.getPdb(pdbCode);
			if (!Files.exists(pdbPath)) {
				MyFileUtils.download("https://files.rcsb.org/download/" + pdbCode + ".pdb.gz",
					pdbPath);
			}
			s = pdbReader.getStructure(pdbPath.toFile());
			//	try (InputStream is = new FileInputStream(pdbPath.toFile())) {
			//	s = pdbReader.getStructure(is);
			//	}
		}
		return s;
	}

	// format e.g. 1cv2A or 1egf
	public List<Chain> getSingleChain(String id) throws IOException {
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
	private Structure parseMmtfToBiojava(Path p) {
		try {
			MmtfStructureReader mmtfStructureReader = new MmtfStructureReader();
			byte[] array = Files.readAllBytes(p);
			array = ReaderUtils.deflateGzip(array);
			ByteArrayInputStream bai = new ByteArrayInputStream(array);
			MmtfStructure mmtf = ReaderUtils.getDataFromInputStream(bai);
			GenericDecoder gd = new GenericDecoder(mmtf);
			new StructureDataToAdapter(gd, mmtfStructureReader);
			return mmtfStructureReader.getStructure();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Atom[] getAtoms(List<Chain> chains) {
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
	}

	public SimpleStructure getSimpleStructurePdb(String filename) throws IOException {
		Path p = dirs.getCathFile(filename);
		return parsePdb(p.toFile());
	}

	public Structure getStructurePdb(String filename) throws IOException {
		Path p = dirs.getCathFile(filename);
		return pdbReader.getStructure(p.toFile());
	}

	private static PDBFileReader pdbReader = new PDBFileReader();

	public static SimpleStructure parsePdb(File f) throws IOException {
		return convert(pdbReader.getStructure(f), f.getName());
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

	public static SimpleStructure convert(List<Chain> chains, String id) {
		SimpleStructure ss = new SimpleStructure(id);
		for (Chain c : chains) {
			ChainId cid = new ChainId(c.getId());
			List<Residue> residues = new ArrayList<>();
			int index = 0;
			for (Group g : c.getAtomGroups()) {
				Point p = null;
				Integer serial = null;
				for (Atom a : g.getAtoms()) {
					if (a.getName().toUpperCase().equals("CA")) {
						p = new Point(a.getX(), a.getY(), a.getZ());
						serial = a.getPDBserial();
					}
				}
				if (p != null) {
					ResidueId rid = new ResidueId(cid, index);
					Residue r = new Residue(rid, serial, p.x, p.y, p.z);
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

	public static SimpleStructure convert(Structure s, String id) {
		SimpleStructure ss = new SimpleStructure(id);
		for (int model = 0; model <= 0; model++) {
			return convert(s.getModel(model), id);
		}
		return ss;
	}

	/*@Deprecated
	public SimpleStructure getStructure(String pdbCode, StructureDataInterface s, ChainId chainId) {
		SimpleStructure structure = new SimpleStructure(pdbCode);
		int[] chainsPerModel = s.getChainsPerModel();
		int mi = 0; // model index
		int ci = 0; // chain index
		int gi = 0; // group index
		int ai = 0; // atom index
		for (int mc_ = 0; mc_ < 1; mc_++) { // chainsPerModel.length
			// models, take just first
			for (int cc_ = 0; cc_ < chainsPerModel[mi]; cc_++) { // chains
				ChainId cid = new ChainId(s.getChainIds()[ci], s.getChainNames()[ci]);
				int chainGroupCount = s.getGroupsPerChain()[ci];
				for (int gc_ = 0; gc_ < chainGroupCount; gc_++) {
					int group = s.getGroupTypeIndices()[gi];
					int groupAtomCount = s.getGroupAtomNames(group).length;
					for (int i = 0; i < groupAtomCount; i++) {
						if ((chainId == null || cid.equals(chainId))
							&& s.getGroupAtomNames(group)[i].toUpperCase().equals("CA")) {
							ResidueId rid = new ResidueId(cid, s.getGroupIds()[gi]);
							Residue r = new Residue(rid, ai, s.getxCoords()[ai], s.getyCoords()[ai], s.getzCoords()[ai]);
							structure.add(cid, r);
						}
						ai++;
					}
					gi++;
				}
				ci++;
			}
			mi++;
		}
		return structure;
	}*/

	/*public static void main(String[] args) {
		MmtfStructureProvider p = new MmtfStructureProvider(Directories.createDefault().getHome().toPath());
		SimpleStructure s = p.getStructure("1cv2");
		for (Residue r : s.getFirstChain().getResidues()) {
			System.out.println(r.getIndex() + " " + r.getPosition());
		}

	}*/
}
