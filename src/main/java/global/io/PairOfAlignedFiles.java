package global.io;

import alignment.StructureSourcePair;
import java.io.File;

/**
 *
 * @author Antonin Pavelka
 *
 * Allows access to aligned PDB file names.
 *
 */
public class PairOfAlignedFiles {

	private final Directories dirs;
	private String sharedName;
	private String[] names;
	private PythonPath[] paths;
	private StructureSourcePair structureSourcePair;

	public PairOfAlignedFiles(Directories dirs, StructureSourcePair structureSourcePair) {
		this.dirs = dirs;
		this.structureSourcePair = structureSourcePair;
		initSharedName();
		init();
	}

	private void initSharedName() {
		sharedName = structureSourcePair.getFirst().toString() + "_" + structureSourcePair.getSecond().toString();
	}

	private String getAlignedPdbName(int index) {
		return sharedName + ((index == 0) ? "A" : "B");
	}

	private void init() {
		paths = new PythonPath[2];
		names = new String[2];
		for (int i = 0; i < 2; i++) {
			names[i] = getAlignedPdbName(i);
			String pdbFileName = names[i] + ".pdb";
			File file = FileOperations.safeSub(dirs.getAlignedPdbsDir(), pdbFileName);
			paths[i] = new PythonPath(file);
		}
	}

	public PythonPath getPdbPath(int i) {
		return paths[i];
	}

	public String getName(int i) {
		return names[i];
	}

	public PythonPath getFinalLines() {
		return new PythonPath(FileOperations.safeSub(dirs.getAlignedPdbsDir(), sharedName + "_Fin.pdb"));
	}

	public PythonPath getWordLines() {
		return new PythonPath(FileOperations.safeSub(dirs.getAlignedPdbsDir(), sharedName + "_Word.pdb"));
	}

	public PythonPath getInitialLines() {
		return new PythonPath(FileOperations.safeSub(dirs.getAlignedPdbsDir(), sharedName + "_Ini.pdb"));
	}

}
