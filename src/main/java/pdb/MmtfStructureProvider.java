package pdb;

import static analysis.MultidimensionalSphere.p;
import geometry.Point;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.PDBFileReader;

import org.rcsb.mmtf.api.StructureDataInterface;
import org.rcsb.mmtf.dataholders.MmtfStructure;
import org.rcsb.mmtf.decoder.GenericDecoder;

import util.MyFileUtils;

public class MmtfStructureProvider {

	private Path home;

	PrintStream out = System.out;

	public MmtfStructureProvider(Path home) {
		this.home = home;
		try {
			out = new PrintStream(new File(home + "/strutures.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	public SimpleStructure getStructure(String pdbCode, ChainId chainId) {
		Path mmtf = home.resolve(pdbCode);
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
	}

	private static PDBFileReader pdbReader = new PDBFileReader();

	public static SimpleStructure parsePdb(File f) throws IOException {
		return convert(pdbReader.getStructure(f.toString()));
	}

	private static SimpleStructure convert(Structure s) {
		SimpleStructure ss = new SimpleStructure(s.getPDBCode());
		for (int model = 0; model <= 0; model++) {
			List<Chain> chains = s.getModel(model);
			for (Chain c : chains) {
				ChainId cid = new ChainId(c.getId());
				SimpleChain sic = new SimpleChain(cid);
				int index = 0;
				for (Group g : c.getAtomGroups()) {
					Point p = null;
					Integer serial = null;
					for (Atom a : g.getAtoms()) {
						if (p == null || a.getName().toUpperCase().equals("CA")) {
							p = new Point(a.getX(), a.getY(), a.getZ());
							serial = a.getPDBserial();
						}
					}
					ResidueId rid = new ResidueId(cid, index);
					Residue r = new Residue(rid, serial, p.x, p.y, p.z);
					sic.add(r);
					index++;
				}
				ss.addChain(cid, sic);
			}
		}
		return ss;
	}

	public SimpleStructure getStructure(String pdbCode, StructureDataInterface s, ChainId chainId) {
		SimpleStructure structure = new SimpleStructure(pdbCode);
		int[] chainsPerModel = s.getChainsPerModel();
		int mi = 0; // model index
		int ci = 0; // chain index
		int gi = 0; // group index
		int ai = 0; // atom index
		for (int mc_ = 0; mc_ < 1 /* chainsPerModel.length */; mc_++) { // ????????????????????????????
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
	}

	/*public static void main(String[] args) {
		MmtfStructureProvider p = new MmtfStructureProvider(Directories.createDefault().getHome().toPath());
		SimpleStructure s = p.getStructure("1cv2");
		for (Residue r : s.getFirstChain().getResidues()) {
			System.out.println(r.getIndex() + " " + r.getPosition());
		}

	}*/
}
