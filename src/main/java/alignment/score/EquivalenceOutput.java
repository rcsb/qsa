package alignment.score;

import fragments.Debugger;
import fragments.Parameters;
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

	public void saveResults(ResidueAlignment eq, double initialTmScore, int maxComponentSize) {
		StringBuilder sb = new StringBuilder();
		char s = ',';
		sb.append(eq.get(0).getId()).append(s);
		sb.append(eq.get(1).getId()).append(s);

		if (eq.empty()) {
			for (int i = 0; i < 3; i++) {
				sb.append("-").append(s);
			}
		} else {
			sb.append(eq.matchingResidues()).append(s);
			sb.append(eq.matchingResiduesRelative()).append(s);
			sb.append(eq.tmScore()).append(s);
			sb.append(initialTmScore).append(s);
			sb.append((double) maxComponentSize / eq.getMinStrLength());

		}
		tableFile.writeLine(sb.toString());
	}
	private static int hits = 0;

	public String nice(double d) {
		return String.format("%.2f", d);
		//return Math.round(d * 1000) / 1000.0;
	}

	public void visualize(ResidueAlignment eq, Residue[][] superpositionAlignment, double bestInitialTmScore, int alignmentNumber,
		int alignmentVersion) {
		//System.out.println("hit " + hits + " " + nice(eq.matchingResiduesRelative()) + " "
		//	+ eq.matchingResidues() + " " + nice(eq.tmScore()) + " " + nice(bestInitialTmScore));
		hits++;
		if (true) {
			String name = eq.get(0).getId() + "_" + eq.get(1).getId() + "_"
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

			if (Parameters.create().debug()) {
				SimpleStructure[] ss = {eq.get(0), eq.get(1)};
				if (debug != null) {
					debug.save(ss, shift, new File(dirs.getWordLines(name)));
				}
				eq.save(shift, new File(dirs.getScoreLines(name)));
				eq.save(eq.orient(superpositionAlignment), shift, new File(dirs.getSuperpositionLines(name)));
			}
			pyFile.writeLine(PymolVisualizer.load(na, alignmentNumber));
			pyFile.writeLine(PymolVisualizer.load(nb, alignmentNumber));
			if (Parameters.create().debug()) {
				//pyFile.writeLine(PymolVisualizer.load(dirs.getScoreLines(name), alignmentNumber));
				pyFile.writeLine(PymolVisualizer.load(dirs.getSuperpositionLines(name), alignmentNumber));
				//pyFile.writeLine(PymolVisualizer.load(dirs.getWordLines(name), alignmentNumber));
			}
		}
	}

}
