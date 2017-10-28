package pdb;

import java.io.File;

/**
 *
 * @author Antonin Pavelka
 */
public class StructureReference {

	public static final int PDB_CODE = 1;
	public static final int FILE = 2;

	private File file;
	private String pdbCode;

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
		this.pdbCode = pdbCode;
	}

	public File getFile() {
		return file;
	}

	public String getPdbCode() {
		return pdbCode;
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

}
