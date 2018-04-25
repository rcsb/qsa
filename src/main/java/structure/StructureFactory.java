package structure;

import global.io.Directories;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.biojava.nbio.structure.Atom;
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
import cath.Cath;
import cath.CathDomainResidueFilter;
import cath.Domain;
import testing.TestResources;

import util.MyFileUtils;

/**
 * Creates SimpleStructure object from any StructureSource.
 *
 * @author Antonin Pavelka
 */
public class StructureFactory {

	private final Directories dirs;
	private final PDBFileReader pdbReader = new PDBFileReader();
	private final Cath cath;

	public StructureFactory(Directories dirs, Cath cath) {
		this.dirs = dirs;
		this.cath = cath;
	}

	public SimpleStructure getStructure(int id, StructureSource source) throws IOException, StructureParsingException {

		Structure biojavaStructure = createBiojavaStructure(source);
		assert biojavaStructure.size() > 0;

		ResidueFilter filter = getResidueFilter(source);

		assert biojavaStructure.getModel(0).size() > 0 : biojavaStructure.getModel(0).size();

		SimpleStructure ss = convertProteinChains(biojavaStructure.getModel(0), id, source, filter);
		if (source.specifiesChain()) {
			ss.removeChainsByNameExcept(source.getChain());
		}
		assert ss.size() > 0;
		return ss;
	}

	public Structure createBiojavaStructure(StructureSource source) throws IOException {
		switch (source.getType()) {
			case StructureSource.PDB_CODE:
			case StructureSource.PDB_CODE_CHAIN:
			case StructureSource.CATH_DOMAIN:
				return createBiojavaStructureFromId(source);
			case StructureSource.FILE:
				return createBiojavaStructureFromFile(source);
			default:
				throw new RuntimeException();
		}
	}

	private Structure createBiojavaStructureFromId(StructureSource source) throws IOException {
		Structure biojavaStructure = null;
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
				biojavaStructure = parseMmtfToBiojava(mmtfPath);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (biojavaStructure == null) {
			Path pdbPath = dirs.getPdb(source.getPdbCode());
			if (!Files.exists(pdbPath)) {
				MyFileUtils.download("https://files.rcsb.org/download/" + source.getPdbCode() + ".pdb.gz",
					pdbPath);
			}
			biojavaStructure = pdbReader.getStructure(pdbPath.toFile());
		}
		return biojavaStructure;
	}

	private Structure createBiojavaStructureFromFile(StructureSource source) throws IOException {
		Structure biojavaStructure = null;
		if (source.isMmtf()) {
			biojavaStructure = parseMmtfToBiojava(source.getFile().toPath());
		} else if (source.isPdb()) {
			biojavaStructure = pdbReader.getStructure(source.getFile());
		} else {
			throw new IOException("Unknown structure file ending: " + source.getFile().getAbsolutePath());
		}
		return biojavaStructure;
	}

	private ResidueFilter getResidueFilter(StructureSource source) {
		ResidueFilter filter;
		if (source.getType() == StructureSource.CATH_DOMAIN) {
			Domain domain = cath.getDomain(source.getCathDomainId());
			filter = new CathDomainResidueFilter(domain);
		} else {
			filter = new EmptyResidueFilter();
		}
		return filter;
	}

	public String createPdbFile(StructureSource source) throws IOException {
		Structure structure = createBiojavaStructure(source);
		return structure.toPDB();
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

	private SimpleStructure convertProteinChains(List<Chain> chains, int id,
		StructureSource source, ResidueFilter filter) throws StructureParsingException {

		int residueIndex = 0;
		SimpleStructure structure = new SimpleStructure(id, source);
		assert chains.size() > 0;
		boolean hasProteinChain = false;
		for (Chain chain : chains) {
			ChainId chainId = new ChainId(chain.getId(), chain.getName());
			if (chain.isProtein()) {
				hasProteinChain = true;
			} else {
				continue;
			}
			List<Residue> residues = new ArrayList<>();
			List<Group> groups = chain.getAtomGroups();
			assert groups.size() > 0;
			assert groups.size() > 0;
			for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
				Group group = chain.getAtomGroup(groupIndex);
				ResidueNumber residueNumber = group.getResidueNumber();
				ResidueId pdbResidueId = new ResidueId(chainId, residueNumber.getSeqNum(), residueNumber.getInsCode());
				if (source.hasPdbCode()) {
					if (filter.reject(source.getPdbCode(), pdbResidueId)) {
						continue;
					}
				}
				int atomCounter = 0;
				for (Atom a : group.getAtoms()) {
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
				for (Atom a : group.getAtoms()) {
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
					PhiPsi torsionAngles = new PhiPsi(groups, groupIndex);
					Residue r = new Residue(residueIndex++, pdbResidueId, serial, carbonAlpha, atoms,
						atomNames, torsionAngles, group.getPDBName());
					residues.add(r);
				}
			}
			Residue[] a = new Residue[residues.size()];
			residues.toArray(a);
			SimpleChain simpleChain = new SimpleChain(chainId, a);
			if (simpleChain.size() > 0) {
				structure.addChain(simpleChain);
			}
		}
		if (!hasProteinChain) {
			throw new StructureParsingException("No protein chains found in " + source, false);
		} else if (structure.size() == 0) {
			throw new StructureParsingException("No suitable residues found in: " + source + ":" + chains.size(), true);
		}
		return structure;
	}

}
