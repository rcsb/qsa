package pdb;

import java.io.File;

/**
 *
 * @author Antonin Pavelka
 *
 * Points to a source of macromolecular structure - a file or PDB web service.
 *
 */
public class StructureReference {

	public static final int FILE = 1;
	public static final int PDB_CODE = 2;
	public static final int PDB_CODE_CHAIN = 3;

	private File file;
	private String pdbCode;
	private ChainId chain; // if null, whole structure is used, otherwise only single chain

	public int getType() {
		if (file != null) {
			return FILE;
		} else {
			return PDB_CODE;
		}
	}

	public StructureReference(File file) {
		this.file = file;
	}

	public StructureReference(String pdbCode) {
		switch (pdbCode.length()) {
			case 4:
				this.pdbCode = pdbCode;
				break;
			case 5:
				this.pdbCode = pdbCode.substring(0, 4);
				this.chain = new ChainId(pdbCode.charAt(4));
				break;
			default:
				throw new RuntimeException(pdbCode + " not a PDB code");
		}
	}

	public File getFile() {
		return file;
	}

	public String getPdbCode() {
		return pdbCode;
	}

	public boolean specifiesChain() {
		return chain != null;
	}

	public ChainId getChain() {
		return chain;
	}

	public boolean isFileSupported(File f) {
		String s = f.getName().toLowerCase();
		return s.endsWith(".pdb") || s.endsWith(".mmtf");
	}

	public boolean isMmtf() {
		return file != null && file.getName().toLowerCase().endsWith(".mmtf");
	}

	public boolean isPdb() {
		return file != null && file.getName().toLowerCase().endsWith(".pdb");
	}

	public String toString() {
		if (chain == null) {
			return pdbCode;
		} else {
			return pdbCode + chain.getName();
		}
	}

}
