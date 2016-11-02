package util.pymol;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.vecmath.Point3d;

import pdb.PdbLine;

@Deprecated
public class Chain {
	List<PdbLine> atoms = new ArrayList<>();

	public Chain(Point3d[] points, AtomicInteger atomSerialNumber, char chainId) {
		String atomName = "CA";
		String element = "C";
		String residueName = "GLY";
		int residueSequenceNumber = 1;
		for (Point3d p : points) {
			PdbLine pl = new PdbLine(atomSerialNumber.get(), atomName, element, residueName,
					Integer.toString(residueSequenceNumber), chainId, p.x, p.y, p.z);
			atoms.add(pl);
			residueSequenceNumber++;
			atomSerialNumber.incrementAndGet();
		}
	}

	public void add(PdbLine pl) {
		atoms.add(pl);
	}

	public void save(BufferedWriter bw) throws IOException {
		boolean later = false;
		for (PdbLine pl : atoms) {
			bw.write(pl.getPdbString());
			bw.newLine();
			if (later) {
				int a = pl.getAtomSerialNumber() - 1;
				int b = pl.getAtomSerialNumber();
				bw.write(PdbLine.getConnectString(a, b));
				bw.newLine();
			} else {
				later = true;
			}
		}
	}
}
