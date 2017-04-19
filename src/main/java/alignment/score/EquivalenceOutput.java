package alignment.score;

import geometry.Point;
import io.Directories;
import io.LineFile;
import java.io.File;
import util.pymol.PymolVisualizer;

/**
 *
 * @author Antonin Pavelka
 */
public class EquivalenceOutput {

	private final Directories dirs;
	private final LineFile pyFile;
	private final LineFile tableFile;
	private int counter = 1;

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

	public void visualize(Equivalence eq) {
		// !!!!!!!!!!!!!!!!
		if (true || (eq.matchingResiduesRelative() >= 0.5
			&& eq.matchingResidues() >= 50
			&& eq.tmScore() >= 0.1)) {
			System.out.println("hit " + hits + " " + eq.matchingResiduesRelative() + " "
				+ eq.matchingResidues());
			hits++;

			String name = eq.get(0).getPdbCode() + "_" + eq.get(1).getPdbCode() + "_" + counter;
			String na = dirs.getAligned(name + "_A.pdb");
			String nb = dirs.getAligned(name + "_B.pdb");
			Point shift = null;
			if (eq.size() > 0) {
				shift = eq.center().negative();
			}
			PymolVisualizer.save(eq.get(0), shift, new File(na));
			PymolVisualizer.save(eq.get(1), shift, new File(nb));
			eq.save(shift, new File(dirs.getMatchLines(name)));
			pyFile.writeLine(PymolVisualizer.load(na, counter));
			pyFile.writeLine(PymolVisualizer.load(nb, counter));
			pyFile.writeLine(PymolVisualizer.load(dirs.getMatchLines(name), counter));
			counter++;
		}
	}

}
