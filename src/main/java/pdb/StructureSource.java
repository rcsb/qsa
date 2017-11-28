package pdb;

import java.io.File;

/**
 *
 * @author Antonin Pavelka
 *
 * Points to a source of macromolecular structure - a file or PDB web service.
 *
 */
public class StructureSource {

	public static final int FILE = 1;
	public static final int PDB_CODE = 2;
	public static final int PDB_CODE_CHAIN = 3;

	private String file; // String for serializetion by Kryo
	private String pdbCode;
	private ChainId chain; // if null, whole structure is used, otherwise only single chain

	public StructureSource() {
		// for Kryo
	}

	public int getType() {
		if (file != null) {
			return FILE;
		} else if (chain == null) {
			return PDB_CODE;
		} else {
			return PDB_CODE_CHAIN;
		}
	}

	public StructureSource(File file) {
		this.file = file.getAbsolutePath();
	}

	public StructureSource(String pdbCode) {
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
		return new File(file);
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
		return file != null && getFile().getName().toLowerCase().endsWith(".mmtf");
	}

	public boolean isPdb() {
		return file != null && getFile().getName().toLowerCase().endsWith(".pdb");
	}

	@Override
	public String toString() {
		switch (getType()) {
			case FILE:
				return getFile().getName().replace(".pdb", "");
			case PDB_CODE:
				return pdbCode;
			case PDB_CODE_CHAIN:
				return pdbCode + chain.getName();
			default:
				throw new RuntimeException();
		}
	}

}
