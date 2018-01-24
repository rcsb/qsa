package output;

import alignment.Alignments;
import alignment.Alignment;
import alignment.StructureSourcePair;
import global.FlexibleLogger;
import global.Parameters;
import global.io.Directories;
import global.io.PythonPath;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import structure.PdbLine;
import structure.StructureFactory;
import structure.StructureSource;
import util.pymol.PymolVisualizer;

/**
 *
 * @author Antonin Pavelka
 */
public class OutputVisualization {

	private final Alignments alignments;
	private final Directories dirs;
	private final StructureFactory structureFactory;
	private final Parameters parameters;

	public OutputVisualization(Parameters parameters, Directories dirs, Alignments alignments, StructureFactory structureFactory) {
		this.parameters = parameters;
		this.dirs = dirs;
		this.alignments = alignments;
		this.structureFactory = structureFactory;
	}

	public void generate() {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(dirs.getPyFile()))) {
			generate(bw);
		} catch (IOException ex) {
			FlexibleLogger.error(ex);
		}
	}

	private void generateScriptStart(File query, BufferedWriter bw) throws IOException {
		PythonPath p = new PythonPath(dirs.getScriptHome(), query);
		int frame = 1;
		bw.write(PymolVisualizer.load(p.getPath(), frame));
		bw.write("\n");
	}

	private void generateScript(int frame, File targetFile, BufferedWriter bw) throws IOException {
		PythonPath targetPythonPath = new PythonPath(dirs.getScriptHome(), targetFile);
		bw.write(PymolVisualizer.load(targetPythonPath.getPath(), frame));
		bw.write("\n");
	}

	private void generate(BufferedWriter scriptWriter) throws IOException {

		StructureSource query = null;
		int frame = 2;
		for (Alignment alignment : alignments.getBestSummariesSorted(parameters.getTmFilter())) {
			StructureSourcePair pair = alignment.getStructureSourcePair();
			assert query == null || pair.getFirst().equals(query);
			query = pair.getFirst();
			StructureSource target = pair.getSecond();
			File targetFile = dirs.getOutputStructureFile(target);
			File queryFile = dirs.getOutputStructureFile(query);
			if (frame == 2) {
				generateScriptStart(queryFile, scriptWriter);
			}
			Structure targetStructure = structureFactory.createBiojavaStructure(target);
			//Calc.transform(targetStructure, alignment.getMatrix());
			assert alignment.getMatrix() != null;

			saveBiojavaStructure(targetStructure, targetFile, alignment.getMatrix());
			generateScript(frame, targetFile, scriptWriter);
			frame++;
		}
		if (query != null) {
			File queryFile = dirs.getOutputStructureFile(query);
			Structure biojavaStructure = structureFactory.createBiojavaStructure(query);
			saveBiojavaStructure(biojavaStructure, queryFile, null);
		}
	}

	// SimpleSTructure db?
	// TODO check if residues are neighbors and not HEATM?
	private void saveBiojavaStructure(Structure structure, File file, Matrix4d matrix) {
		//System.out.println(structure.getPDBCode() + " "+matrix.toString());
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			for (Chain chain : structure.getModel(0)) {
				Atom last = null;
				for (Group group : chain.getAtomGroups()) {
					for (Atom atom : group.getAtoms()) {
						if (atom.getName().toUpperCase().equals("CA")) {
							if (matrix != null) {
								atom.setCoords(transform(atom.getCoords(), matrix));
							}
							bw.write(atom.toPDB().trim()); // no extra letters by Windows
							bw.write("\n");
							if (last != null) {
								bw.write(PdbLine.getConnectString(last.getPDBserial(), atom.getPDBserial()));
								bw.write("\n");
							}
							last = atom;
						}
					}
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private double[] transform(double[] coords, Matrix4d matrix) {
		Point3d x = new Point3d(coords);
		matrix.transform(x);
		double[] r = {x.x, x.y, x.z};
		return r;

	}

}
