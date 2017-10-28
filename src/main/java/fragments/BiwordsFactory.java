package fragments;

import geometry.Coordinates;
import geometry.GridRangeSearch;
import geometry.Point;
import grid.sparse.Buffer;
import io.Directories;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pdb.ChainId;
import pdb.PdbLine;
import pdb.Residue;
import pdb.SimpleChain;

import pdb.SimpleStructure;
import util.Counter;
import util.Timer;

/**
 *
 * @author Antonin Pavelka
 *
 * Generator of all biwords from a single (quaternary) structure.
 *
 */
public final class BiwordsFactory implements Serializable {

	private static final long serialVersionUID = 1L;
	private final Parameters params_ = Parameters.create();
	private static final boolean print = false;
	private Directories dirs = Directories.createDefault();

	public BiwordsFactory() {
	}
	static long time = 0;

	public Biwords create(SimpleStructure ss, int wordLength, int sparsity, boolean permute) {
		System.out.println("------------");
		Timer.start();
		Counter idWithinStructure = new Counter();
		WordsFactory wf = new WordsFactory(ss, wordLength);
		wf.setSparsity(sparsity);
		WordImpl[] words = wf.create().toArray();
		List<Biword> fl = new ArrayList<>();

		GridRangeSearch<AtomToWord> grid = new GridRangeSearch<>(5);
		List<AtomToWord> atoms = new ArrayList<>();

		Map<Residue, WordImpl> residueToWord = new HashMap<>();

		int count = 0;
		// each atom of the residue in the middle of a word will map to the word
		for (WordImpl w : words) {
			Residue r = w.getCentralResidue();
			residueToWord.put(r, w);
			for (double[] x : r.getAtoms()) {
				AtomToWord central = new AtomToWord(x, w);
				count++;
				atoms.add(central);
			}
		}
		grid.buildGrid(atoms);

		// try contacts just with 1 - almost unique perpendicular, and it is atoms
		// lets hope for lot less neighbors
		// easier implementation of vectors, but possibly also more hits, but we can always add another turn, if search stops to be a bottleneck
		Buffer<AtomToWord> ys = new Buffer<>(count);
		System.out.println("---");
		for (WordImpl x : words) {
			
			//Timer.start();
			
			//List<AtomToWord> ys = new ArrayList<>();
			for (double[] atom : x.getAtoms()) {
				ys.clear();
				Point p = new Point(atom);
				//ys = grid.nearest(p, params_.getAtomContactDistance());
				grid.nearest(p, params_.getAtomContactDistance(), ys);
			}
			// locate residues leading to overlap, remove them from results
			// function: iterate over residue array, return in distance

			// remove repeated hits
			Map<Residue, WordImpl> hits = new HashMap<>();
			for (int i = 0; i < ys.size(); i++) {
				//for (AtomToWord aw : ys) {
				AtomToWord aw = ys.get(i);
				WordImpl y = aw.getWord();
				Residue a = x.getCentralResidue();
				Residue b = y.getCentralResidue();
				if (!permute) {                                     // think it through
					if (a.getId().compareTo(b.getId()) >= 0) {
						continue;
					}
				}
				if (a.isWithin(b, 4)) {
					continue;
				}
				//if (!x.overlaps(y)) { // +-2
				hits.put(y.getCentralResidue(), y);
				//}
			}
			// organize residues in contact by chain
			Map<ChainId, List<WordImpl>> byChain = new HashMap<>();
			for (WordImpl w : hits.values()) {
				ChainId c = w.getCentralResidue().getId().getChain();
				List<WordImpl> l = byChain.get(c);
				if (l == null) {
					l = new ArrayList<>();
					byChain.put(c, l);
				}
				l.add(w);
			}
			
			// identify connected residues among contacts
			List<List<WordImpl>> connected = new ArrayList<>();
			for (List<WordImpl> l : byChain.values()) {
				Collections.sort(l);
				List<WordImpl> active = new ArrayList<>(); // active connected chain of residues
				//System.out.println("***");
				for (WordImpl r : l) {
					if (!active.isEmpty()) {
						WordImpl p = active.get(active.size() - 1);
						if (r.getCentralResidue().follows(p.getCentralResidue())) {
							active.add(r);
						} else {
							connected.add(active);
							active = new ArrayList<>();
							//System.out.println("---");
						}
					} else {
						active.add(r);
					}
					//System.out.println(r.getId());
				}
			}
			System.out.println("conn " + connected.size());

			//Timer.stop();
			//time += Timer.getNano();

			List<WordImpl> chosen = new ArrayList<>();
			// choose only the nearest residues from each strand
			// if ambiguous, use more
			for (List<WordImpl> strand : connected) {
				if (strand.size() == 1) {
					continue;
				}
				double min = Double.POSITIVE_INFINITY;
				Point xp = x.getCentralResidue().getCa();
				System.out.println("s "+strand.size());
				for (int i = 0; i < strand.size(); i++) {
					WordImpl r = strand.get(i);
					Point yp = r.getCentralResidue().getCa();
					double d = xp.distance(yp);
					if (d < min) {
						min = d;
					}
					//Residue q = strand.get(i + 1);
					//System.out.println(r.getId() + " " + q.follows(r) + " " + q.getId());
				}
				for (int i = 0; i < strand.size(); i++) {
					WordImpl r = strand.get(i);
					Point yp = r.getCentralResidue().getCa();
					double d = xp.distance(yp);					
					if (d <= min + 0.5) {
						chosen.add(r);
						
					}
				}
			}
			for (WordImpl y : chosen) {
				if (y.getCentralResidue().getId().follows(x.getCentralResidue().getId())) {
					continue;
				}
				if (x.getCentralResidue().equals(y.getCentralResidue())) {
					continue;
				}
				double dist = x.getCentralResidue().distance(y.getCentralResidue());
				if (dist >= 2 || dist <= 20) {
					Biword f = new Biword(ss.getId(), idWithinStructure, x, y);
					fl.add(f);
					//fl.add(f.switchWords(idWithinStructure)); // will be entered anyway?
				}
				// TODO: switch only if order cannot be derived from length of word, angles and other, alphabetical order
			}

		}
		//System.out.println("TTT " + Timer.get() + " total " + (time / 1000000));
		for (SimpleChain c : ss.getChains()) {
			Residue[] rs = c.getResidues();
			int l = params_.getWordLength();
			for (int i = l / 2; i < rs.length - l / 2 - wordLength; i++) {
				WordImpl x = residueToWord.get(rs[i]);
				WordImpl y = residueToWord.get(rs[i + 1]); // + wordLength

				if (x != null && y != null) { // word might be missing because of missing residue nearby
					double dist = x.getCentralResidue().distance(y.getCentralResidue());
					if (dist < 5 && dist > 2) {
						Biword f = new Biword(ss.getId(), idWithinStructure, x, y);
						fl.add(f);
						// no switching, sequence order is good here
						//fl.add(f.switchWords());				
					}
				}
			}
		}
		Biword[] fa = new Biword[fl.size()];
		fl.toArray(fa);
		for (int i = 0; i < fa.length; i++) {
			if (i != fa[i].getIdWithingStructure()) {
				throw new RuntimeException(i + " " + fa[i].getIdWithingStructure());
			}
		}
		Biwords fs = new Biwords(ss, fa, words);
		//if (false) { // visualizing biwords
			save(fs, dirs.getWordConnections(ss.getPdbCode()));
		//}
		return fs;
	}

	private void save(Biwords bws, File f) {
		int serial = 1;
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
			for (Biword x : bws.getBiwords()) {
				for (int i = 0; i < 2; i++) {
					double[] p = x.getWords()[i].getCentralResidue().getAtom("CA");
					PdbLine pl = new PdbLine(serial + i, "CA", "C", "GLY",
						Integer.toString(serial + i), 'A', p[0], p[1], p[2]);
					bw.write(pl.toString());
					bw.write("\n");
				}
				bw.write(PdbLine.getConnectString(serial, serial + 1));
				bw.write("\n");
				serial += 2;
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

	}

	/*public Biwords createBigWords(SimpleStructure ss, int wordLength, int sparsity) {
		WordsFactory wf = new WordsFactory(ss, wordLength);
		wf.setSparsity(sparsity);
		Words words = wf.create();
		if (print) {
			System.out.println("***** " + ss.size());
			for (WordImpl w : words) {
				w.print();
			}
		}
		WordImpl[] wa = words.toArray();
		List<Biword> fl = new ArrayList<>();
		for (int xi = 0; xi < wa.length; xi++) {
			for (int yi = 0; yi < xi; yi++) {
				if (xi == yi) {
					continue;
				}
				WordImpl x = wa[xi];
				WordImpl y = wa[yi];
				if (x.isInContactAndNotOverlapping(y, params_.getResidueContactDistance())) {
					Biword f = new Biword(ss.getId(), x, y);
					fl.add(f);
					fl.add(f.switchWords());
				}
			}
		}
		Biword[] fa = new Biword[fl.size()];
		fl.toArray(fa);
		Biwords fs = new Biwords(ss, fa, wa);
		return fs;
	}*/
}

class AtomToWord implements Coordinates {

	private final double[] point;
	private final WordImpl word;

	public AtomToWord(double[] point, WordImpl word) {
		this.point = point;
		this.word = word;
	}

	@Override
	public double[] getCoords() {
		return point;
	}

	public WordImpl getWord() {
		return word;
	}
}
