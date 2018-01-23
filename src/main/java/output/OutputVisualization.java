package output;

import alignment.Alignments;
import alignment.Alignment;
import alignment.StructureSourcePair;
import global.FlexibleLogger;
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
import pdb.PdbLine;
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
	private final StructureFactory structureFactory;

	public OutputVisualization(Directories dirs, Alignments alignments, StructureFactory structureFactory) {
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
		for (Alignment alignment : alignments.getBestSummariesSorted()) {
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
			
			
			System.out.println(alignment.getMatrix().m21 + " *");
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

	// TODO SimpleStructure instead, either full or filtered
	// see if it helps
	// prefered anyway, filter
	
	// TODO check if residues are neighbors and not HEATM?
	private void saveBiojavaStructure(Structure structure, File file, Matrix4d matrix) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			for (Chain chain : structure.getModel(0)) {
				Atom last = null;
				for (Group group : chain.getAtomGroups()) {
					for (Atom atom : group.getAtoms()) {
						if (atom.getName().toUpperCase().equals("CA")) {
							System.out.println(atom.getCoords()[0]);
							if (matrix != null) {
								atom.setCoords(transform(atom.getCoords(), matrix));
							}
							System.out.println(atom.getCoords()[0]);
						   System.out.println("---");
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
