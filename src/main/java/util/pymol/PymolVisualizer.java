package util.pymol;

import algorithm.graph.AwpNode;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import algorithm.Biword;
import algorithm.Word;
import algorithm.WordImpl;
import geometry.Point;
import geometry.SmartTransformation;
import java.util.Collection;
import javax.vecmath.Point3d;
import pdb.PdbLine;
import pdb.Residue;
import pdb.ResidueId;
import pdb.SimpleChain;
import pdb.SimpleStructure;

public class PymolVisualizer {

	private List<Chain> chains = new ArrayList<>();
	private List<String> selectionNames = new ArrayList<>();
	private List<Residue[]> selectionResidues = new ArrayList<>();

	public void add(Chain c) {
		chains.add(c);
	}

	public void addSelection(String name, Residue[] residues) {
		selectionNames.add(name);
		selectionResidues.add(residues);
	}

	public void saveSelections(File f) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for (int i = 0; i < selectionNames.size(); i++) {
				Residue[] rs = selectionResidues.get(i);
				String name = selectionNames.get(i);
				bw.write(select(name, getSelection(rs)));
				bw.newLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void saveAwpNodes(Collection<AwpNode> list, SimpleStructure[] ss, Point shift, File f) {
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

	public static void save(Residue[][] pairs, Point shift, File f) {
		try {
			int serial = 1;
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
				for (int i = 0; i < pairs[0].length; i++) {
					for (int k = 0; k < 2; k++) {
						Point p = pairs[k][i].getPosition();
						if (shift != null) {
							p = p.plus(shift);
						}
						PdbLine pl = new PdbLine(serial + k, "CA", "C", "GLY",
							Integer.toString(serial + k), 'A', p.x, p.y, p.z);
						bw.write(pl.toString());
						bw.newLine();
					}
					bw.write(PdbLine.getConnectString(serial, serial + 1));
					bw.newLine();
					serial += 2;
				}
			}

		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void save(SimpleStructure s, Point shift, File f) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for (SimpleChain sc : s.getChains()) {
				List<PdbLine> atoms = new ArrayList<>();
				for (Residue r : sc.getResidues()) {
					Point p = r.getPosition();
					if (shift != null) {
						p = p.plus(shift);
					}
					PdbLine pl = new PdbLine(r.getAtomSerial(), "CA", "C", "GLY",
						Integer.toString(r.getId().getSequenceNumber()),
						r.getId().getChain().getId().charAt(0),
						p.x, p.y, p.z);
					atoms.add(pl);
				}
				for (int i = 0; i < atoms.size(); i++) {
					PdbLine pl = atoms.get(i);
					bw.write(pl.getPdbString());
					bw.newLine();
					if (i > 0) {
						int a = atoms.get(i - 1).getAtomSerialNumber();
						int b = atoms.get(i).getAtomSerialNumber();
						bw.write(PdbLine.getConnectString(a, b));
						bw.newLine();
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void save(Point3d[] points, File f, int index) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, true))) {
			int serial = 1;
			bw.write("MODEL " + index + "\n");
			for (Point3d p : points) {
				PdbLine pl = new PdbLine(serial, "CA", "C", "GLY",
					Integer.toString(serial), 'A', p.x, p.y, p.z);
				bw.write(pl + "\n");
			}
			bw.write("ENDMDL\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// TODO align and save
	public static void save(Biword rep, List<Biword> fragments, File file) {
		int serial = 1;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			for (Biword f : fragments) {
				SmartTransformation st = new SmartTransformation(rep.getPoints3d(), f.getPoints3d());
				bw.write("MODEL\n");
				for (WordImpl w : f.getWords()) {
					List<PdbLine> atoms = new ArrayList<>();
					for (Residue r : w.getResidues()) {
						Point3d x = r.getPosition3d();
						st.transform(x);
						//Point p = r.getPosition();
						PdbLine pl = new PdbLine(serial++, "CA", "C", "GLY",
							Integer.toString(r.getId().getSequenceNumber()),
							r.getId().getChain().getId().charAt(0),
							x.x, x.y, x.z);
						atoms.add(pl);
					}
					for (int i = 0; i < atoms.size(); i++) {
						PdbLine pl = atoms.get(i);
						bw.write(pl.getPdbString() + "\n");
						if (i > 0) {
							int a = atoms.get(i - 1).getAtomSerialNumber();
							int b = atoms.get(i).getAtomSerialNumber();
							bw.write(PdbLine.getConnectString(a, b) + "\n");

						}
					}
				}
				bw.write("ENDMDL\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String getSelection(Residue[] rs) {
		StringBuilder sb = new StringBuilder("");
		for (Residue r : rs) {
			sb.append("(resi " + r.getId().getPdbString() + " and chain " + r.getId().getChain() + ") + ");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public static List<String> residuesToSelection(String structureId, Residue[] rs) {
		List<String> l = new ArrayList<>();
		for (Residue r : rs) {
			StringBuilder sb = new StringBuilder();
			sb.append("resi ").append(r.getId().getSequenceNumber()).append(" and chain ")
				.append(r.getId().getChain().getId() + " and " + structureId);
			l.add(sb.toString());
		}
		return l;
	}

	public static String listToSelection(List<String> list) {
		//return String.join(" + ", list);
		throw new RuntimeException("needs java 8");
	}

	// cmd.select('a', 'resi 10 and chain B')
	public static String select(String name, String selection) {
		return "cmd.select('" + name + "', '" + selection + "')";
	}

	public static String load(String what, int state) {
		return "cmd.load('" + what + "', state=" + state + ")";
	}

	public static String run(String what) {
		return "cmd.do('" + what + "')";
	}
}
