package fragments;

import geometry.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import pdb.PdbLine;
import pdb.Residue;
import pdb.ResidueId;
import pdb.SimpleStructure;

public class Debugger {

	List<AwpNode> list = new ArrayList<>();

	public void add(AwpNode n) {
		list.add(n);
	}

	public void add(Debugger d) {
		list.addAll(d.list);
	}

	public void save(SimpleStructure[] ss, Point shift, File f) {
		int serial = 1;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for (AwpNode n : list) {
				Word[] ws = n.getWords();
				Residue[][] pairing = new Residue[2][ws[0].size()];
				for (int i = 0; i < 2; i++) {
					Residue[] rs = ws[i].getResidues();
					for (int k = 0; k < rs.length; k++) {
						ResidueId ri = rs[k].getId();
						pairing[i][k] = ss[i].getResidue(ri);
					}
				}
				for (int k = 0; k < pairing[0].length; k++) {
					for (int i = 0; i < 2; i++) {
						Point p = pairing[i][k].getPosition();
						if (shift != null) {
							p = p.plus(shift);
						}
						PdbLine pl = new PdbLine(serial + i, "CA", "C", "GLY",
							Integer.toString(serial + i), 'A', p.x, p.y, p.z);
						bw.write(pl.toString());
						bw.newLine();
					}
					bw.write(PdbLine.getConnectString(serial, serial + 1));
					bw.newLine();
					serial += 2;
				}

			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
