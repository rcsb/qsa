package output;

import alignment.Alignments;
import alignment.Alignment;
import alignment.StructureSourcePair;
import global.FlexibleLogger;
import global.io.Directories;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import pdb.PdbModifier;
import pdb.StructureFactory;
import pdb.StructureSource;
import util.pymol.PymolVisualizer;

/**
 *
 * @author Antonin Pavelka
 */
public class OutputVisualization {

	private final Alignments alignments;
	private final Directories dirs;

	public OutputVisualization(Directories dirs, Alignments alignments) {
		this.dirs = dirs;
		this.alignments = alignments;
	}

	public void generate() {
		try {
			generateAlignedPdbs();
			generatePymolScript(alignments.getBestSummariesSorted());
		} catch (IOException ex) {
			FlexibleLogger.error(ex);
		}
	}

	private void generatePymolScript(List<Alignment> list) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(dirs.getPyFile()))) {
			if (list.isEmpty()) {
				return;
			}
			String queryPath = list.get(0).getPairOfAlignedFiles().getPdbPath(0).getPath();
			bw.write(PymolVisualizer.load(queryPath, 1));
			bw.write("\n");
			int frame = 2;
			for (Alignment aln : list) {
				String targetPath = aln.getPairOfAlignedFiles().getPdbPath(1).getPath();
				bw.write(PymolVisualizer.load(targetPath, frame));
				bw.write("\n");
				/*if (parameters.isDebug()) {
				pyFile.writeLine(PymolVisualizer.load(dirs.getFinalLines(name), frame));
				pyFile.writeLine(PymolVisualizer.load(dirs.getInitialLines(name), frame));
				pyFile.writeLine(PymolVisualizer.load(dirs.getWordLines(name), frame));
			    }*/
				frame++;
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	// TODO custom PDB files, copy?
	private void generateAlignedPdbs() throws IOException {
		StructureSource query = null;
		for (Alignment alignment : alignments.getBestSummariesSorted()) {
			StructureSourcePair pair = alignment.getStructureSourcePair();
			assert query == null || pair.getFirst().equals(query);
			query = pair.getFirst();
			File targetFile = getOutputStructureFile(pair.getSecond().getPdbCode() + ".pdb");
			File targetFileTransformed = getOutputStructureFile(pair.getSecond().getPdbCode() + "_t.pdb");
			createPdbFile(pair.getSecond(), targetFile);
			PdbModifier pdbModifier = new PdbModifier(targetFile, targetFileTransformed);
			pdbModifier.setMatrix(alignment.getMatrix());
			pdbModifier.modify();

		}
		if (query != null) {
			File queryFile = getOutputStructureFile(query.getPdbCode() + ".pdb");
			createPdbFile(query, queryFile);
		}
	}

	private File getOutputStructureFile(String filename) {
		return dirs.getOutputStrucureFile(filename);
	}

	private void createPdbFile(StructureSource source, File file) throws IOException {
		StructureFactory.downloadPdbFile(source, file);
	}
}
