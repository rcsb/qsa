package benchmark;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.Directories;
import pdb.PdbChainId;
import scala.Tuple2;

/**
 * Generates specified number of pairs of PDB chains, based on the list
 * pdb_seqres.txt at
 * http://www.rcsb.org/pdb/static.do?p=general_information/about_pdb/summaries.html
 * 
 * TODO generate it by downloading structures, because there probably is not list of good chain ids.
 * 
 */
public class RandomPairsGenerator {

	private PdbChainId[] chainIds;
	private Random random;

	private List<Tuple2<PdbChainId, PdbChainId>> pairs = new ArrayList<>();

	public RandomPairsGenerator(File data, File out, int n, int seed) {
		random = new Random(seed);
		chainIds = PdbListParser.readPdbCodesFromFasta(data);
		generate(n);
		save(out);
	}

	private void generate(int n) {
		for (int i = 0; i < n; i++) {
			int xi = random.nextInt(chainIds.length);
			int yi;
			do {
				yi = random.nextInt(chainIds.length);
			} while (xi == yi);
			pairs.add(new Tuple2<>(chainIds[xi], chainIds[yi]));
		}
	}

	private void save(File out) {
		try (PrintWriter pw = new PrintWriter(out)) {
			pairs.stream().map(t -> t._1.toString() + " " + t._2.toString()).forEachOrdered(pw::println);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public int size() {
		return pairs.size();
	}

	public Tuple2<PdbChainId, PdbChainId> get(int i) {
		return pairs.get(i);
	}

	public static void main(String[] args) {
		Directories dirs = Directories.createDefault();
		RandomPairsGenerator g = new RandomPairsGenerator(dirs.getPdbFasta(), dirs.getPairs(), 100000, 1);
		for (int i = 0; i < g.size(); i++) {
			System.out.println(g.get(i)._1 + " " + g.get(i)._2);
		}
	}
}
