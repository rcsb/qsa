package pdb;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.rcsb.mmtf.api.StructureDataInterface;
import org.rcsb.mmtf.dataholders.MmtfStructure;
import org.rcsb.mmtf.decoder.GenericDecoder;

import io.Directories;
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

	public SimpleStructure getStructure(String pdbCode) {
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
			return getStructure(pdbCode, s);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			out.close();
		}
	}

	public SimpleStructure getStructure(String pdbCode, StructureDataInterface s) {
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
					//System.out.println(s.getGroupName(group));
					int groupAtomCount = s.getGroupAtomNames(group).length;
					for (int i = 0; i < groupAtomCount; i++) {
						//System.out.println("  " + s.getGroupIds()[gi]  + " " + s.getxCoords()[ai]);
						if (s.getGroupAtomNames(group)[i].toUpperCase().equals("CA")) {							
							Residue r = new Residue(s.getGroupIds()[gi], s.getxCoords()[ai], s.getyCoords()[ai],
									s.getzCoords()[ai]);							
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

	public static void main(String[] args) {
		MmtfStructureProvider p = new MmtfStructureProvider(Directories.createDefault().getHome().toPath());
		SimpleStructure s = p.getStructure("1cv2");
		for (Residue r : s.getFirstChain().getResidues()) {
			System.out.println(r.getIndex() + " " + r.getPosition());
		}

	}
}