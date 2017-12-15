package pdb;

import global.FlexibleLogger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

/**
 *
 * Reads PDB file and performs local modifications, such as transformation of coordinates or possibly removal of atoms.
 *
 * @author Antonin Pavelka
 *
 *
 */
public class PdbModifier {

	private final File pdbIn, pdbOut;
	private Matrix4d matrix;

	public PdbModifier(File pdbIn, File pdbOut) {
		this.pdbIn = pdbIn;
		this.pdbOut = pdbOut;
	}

	public void setMatrix(Matrix4d matrix) {
		this.matrix = matrix;
	}

	public void modify() {
		try (BufferedReader br = new BufferedReader(new FileReader(pdbIn));
			BufferedWriter bw = new BufferedWriter(new FileWriter(pdbOut))) {
			String line;
			while ((line = br.readLine()) != null) {
				modifyLine(line, bw);
			}
		} catch (IOException ex) {
			FlexibleLogger.error(ex);
		}
	}

	private void modifyLine(String line, BufferedWriter bw) throws IOException {
		if (PdbLine.isCoordinateLine(line)) {
			double x = PdbLine.getX(line);
			double y = PdbLine.getY(line);
			double z = PdbLine.getZ(line);
			Point3d point = new Point3d(x, y, z);
			matrix.transform(point);
			StringBuilder sb = new StringBuilder(line);
			PdbLine.printCoords(point.x, point.y, point.z, sb);
			bw.write(sb.toString());
		} else {
			bw.write(line);
		}
	}

}
