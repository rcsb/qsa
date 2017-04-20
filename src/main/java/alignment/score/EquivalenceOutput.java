package alignment.score;

import geometry.Point;
import io.Directories;
import io.LineFile;
import java.io.File;
import pdb.Residue;
import util.pymol.PymolVisualizer;

/**
 *
 * @author Antonin Pavelka
 */
public class EquivalenceOutput {

	private final Directories dirs;
	private final LineFile pyFile;
	private final LineFile tableFile;

	public EquivalenceOutput(Directories dirs) {
		this.dirs = dirs;
		pyFile = new LineFile(dirs.getPyFile());
		tableFile = new LineFile(dirs.getTableFile());
	}

	public void saveResults(Equivalence eq) {
		StringBuilder sb = new StringBuilder();
		char s = ',';
		sb.append(eq.get(0).getPdbCode()).append(s);
		sb.append(eq.get(1).getPdbCode()).append(s);
		sb.append(eq.matchingResidues()).append(s);
		sb.append(eq.matchingResiduesRelative()).append(s);
		sb.append(eq.tmScore()).append(s);
		tableFile.writeLine(sb.toString());
	}
	private static int hits = 0;

	public void visualize(Equivalence eq, Residue[][] origAln, int alignmentNumber, int alignmentVersion) {
		// !!!!!!!!!!!!!!!!
		if (true || (eq.matchingResiduesRelative() >= 0.5
			&& eq.matchingResidues() >= 50
			&& eq.tmScore() >= 0.1)) {
			System.out.println("hit " + hits + " " + eq.matchingResiduesRelative() + " "
				+ eq.matchingResidues() + " " + eq.tmScore() + " " + eq.tmScoreOld());
			hits++;

			String name = eq.get(0).getPdbCode() + "_" + eq.get(1).getPdbCode() + "_"
				+ alignmentNumber + "_" + alignmentVersion;
			
			String[] names = dirs.getNames(name);
			
			String na = dirs.getAligned(names[0] + ".pdb");
			String nb = dirs.getAligned(names[1] + ".pdb");
			Point shift = null;
			if (eq.size() > 0) {
				shift = eq.center().negative();
			}
			PymolVisualizer.save(eq.get(0), shift, new File(na));
			PymolVisualizer.save(eq.get(1), shift, new File(nb));
			eq.save(shift, new File(dirs.getMatchLines(name)));

			if (origAln != null) {
				Equivalence.saveSelections(origAln, name, new File(dirs.getMatchOrigLines(name)));
			}
			
			//TODO find new residues from orig, display as before

			pyFile.writeLine(PymolVisualizer.load(na, alignmentVersion));
			pyFile.writeLine(PymolVisualizer.load(nb, alignmentVersion));
			pyFile.writeLine(PymolVisualizer.load(dirs.getMatchLines(name), alignmentVersion));
			pyFile.writeLine(PymolVisualizer.run(dirs.getMatchOrigLines(name)));
		}
	}

}
