package alignment.score;

import fragments.Debugger;
import geometry.Point;
import io.Directories;
import io.LineFile;
import java.io.File;
import pdb.Residue;
import pdb.SimpleStructure;
import util.pymol.PymolVisualizer;

/**
 *
 * @author Antonin Pavelka
 */
public class EquivalenceOutput {

	private final Directories dirs;
	private final LineFile pyFile;
	private final LineFile tableFile;
	private Debugger debug;

	public EquivalenceOutput(Directories dirs) {
		this.dirs = dirs;
		pyFile = new LineFile(dirs.getPyFile());
		tableFile = new LineFile(dirs.getTableFile());
	}

	public void setDebugger(Debugger d) {
		this.debug = d;
	}

	public void saveResults(Equivalence eq) {
		StringBuilder sb = new StringBuilder();
		char s = ',';
		sb.append(eq.get(0).getPdbCode()).append(s);
		sb.append(eq.get(1).getPdbCode()).append(s);

		if (eq.empty()) {
			for (int i = 0; i < 3; i++) {
				sb.append("-").append(s);
			}
		} else {
			sb.append(eq.matchingResidues()).append(s);
			sb.append(eq.matchingResiduesRelative()).append(s);
			sb.append(eq.tmScore()).append(s);
		}
		tableFile.writeLine(sb.toString());
	}
	private static int hits = 0;

	public String nice(double d) {
		return String.format("%.2f", d);
		//return Math.round(d * 1000) / 1000.0;
	}

	public void visualize(Equivalence eq, Residue[][] superpositionAlignment, int alignmentNumber,
		int alignmentVersion) {
		boolean doDebug = true;
		// !!!!!!!!!!!!!!!!
		if (true || (eq.matchingResiduesRelative() >= 0.5
			&& eq.matchingResidues() >= 50
			&& eq.tmScore() >= 0.1)) {
			System.out.println("hit " + hits + " " + nice(eq.matchingResiduesRelative()) + " "
				+ eq.matchingResidues() + " " + nice(eq.tmScore()) + " " + nice(eq.tmScoreOld()));
			hits++;
			if (true) {
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

				if (doDebug) {
					SimpleStructure[] ss = {eq.get(0), eq.get(1)};
					debug.save(ss, shift, new File(dirs.getWordLines(name)));
					eq.save(shift, new File(dirs.getScoreLines(name)));
					eq.save(eq.orient(superpositionAlignment), shift, new File(dirs.getSuperpositionLines(name)));
				}
				pyFile.writeLine(PymolVisualizer.load(na, alignmentNumber));
				pyFile.writeLine(PymolVisualizer.load(nb, alignmentNumber));
				if (doDebug) {
					pyFile.writeLine(PymolVisualizer.load(dirs.getScoreLines(name), alignmentNumber));
					pyFile.writeLine(PymolVisualizer.load(dirs.getSuperpositionLines(name), alignmentNumber));
					pyFile.writeLine(PymolVisualizer.load(dirs.getWordLines(name), alignmentNumber));
				}
			}
		}
	}

}
