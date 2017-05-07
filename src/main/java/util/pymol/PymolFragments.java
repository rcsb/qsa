package util.pymol;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fragments.Biword;
import pdb.Residue;

public class PymolFragments {

	private List<String> both = new ArrayList<>();
	private String pdbCodeA;
	private String pdbCodeB;

	public PymolFragments(String pdbCodeA, String pdbCodeB) {
		this.pdbCodeA = pdbCodeA;
		this.pdbCodeB = pdbCodeB;
	}

	public void add(Biword[] fs) {
		List<String> rs = new ArrayList<>();
		rs.addAll(PymolVisualizer.residuesToSelection(pdbCodeA, fs[0].getResidues()));
		rs.addAll(PymolVisualizer.residuesToSelection(pdbCodeB, fs[1].getResidues()));
		both.add(PymolVisualizer.listToSelection(rs));
	}

	public void save(File f) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for (int i = 0; i < Math.min(100, both.size()); i++) {
				bw.write(PymolVisualizer.select(Integer.toString(i), both.get(i)) + "\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
