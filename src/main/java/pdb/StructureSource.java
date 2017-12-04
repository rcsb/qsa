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
	public static final int CATH_DOMAIN = 4;

	private String file; // String for serializetion by Kryo
	private String pdbCode;
	private ChainId chain; // if null, whole structure is used, otherwise only single chain
	private String cathDomainId;

	public StructureSource() {
		// for Kryo
	}

	public int getType() {
		if (file != null) {
			return FILE;
		} else if (cathDomainId != null) {
			return CATH_DOMAIN;
		} else if (chain == null) {
			return PDB_CODE;
		} else {
			return PDB_CODE_CHAIN;
		}
	}

	public StructureSource(File file) {
		this.file = file.getAbsolutePath();
	}

	public StructureSource(String id) {
		switch (id.length()) {
			case 4:
				this.pdbCode = id;
				break;
			case 5:
				this.pdbCode = pdbCode.substring(0, 4);
				this.chain = new ChainId(id.charAt(4));
				break;
			case 7:
				this.cathDomainId = id;
				break;
			default:
				throw new RuntimeException(id + " not a PDB code");
		}
	}

	public File getFile() {
		return new File(file);
	}

	public boolean hasPdbCode() {
		return pdbCode != null;
	}

	public String getPdbCode() {
		if (getType() == CATH_DOMAIN) {
			return cathDomainId.substring(0, 4);
		} else {
			if (pdbCode == null) {
				throw new RuntimeException();
			}
			return pdbCode;
		}
	}

	public String getCathDomainId() {
		return cathDomainId;
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
			case CATH_DOMAIN:
				return cathDomainId;
			default:
				throw new RuntimeException();
		}
	}

}
