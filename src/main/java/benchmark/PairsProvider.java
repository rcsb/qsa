package benchmark;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.Directories;
import pdb.MmtfStructureProvider;
import pdb.PdbChainId;
import pdb.SimpleStructure;
import scala.Tuple2;
import spark.interfaces.AlignablePair;

/**
 * Loads ids of pairs of structures from text file and provides corresponding
 * structure objects one by one at request. Currently supports only single
 * chains in this format: AAAAB CCCCD where AAAA, CCCC are PDB codes and B, D
 * are chains.
 */
public class PairsProvider {
	private Random random = new Random(1);
	private MmtfStructureProvider provider;
	private List<Tuple2<PdbChainId, PdbChainId>> pairs = new ArrayList<>();

	public PairsProvider(Directories dirs) {
		provider = new MmtfStructureProvider(dirs.getMmtf().toPath());
		load(dirs.getPairs());
	}

	private void load(File f) {
		try (Stream<String> stream = Files.lines(f.toPath())) {
			pairs = stream.map(t -> new Tuple2<>(new PdbChainId(t.substring(0, 5)), new PdbChainId(t.substring(6, 11))))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public int size() {
		return pairs.size();
	}

	public AlignablePair get(int i) {
		Tuple2<PdbChainId, PdbChainId> t = pairs.get(i);
		return new AlignablePair(getChain(t._1), getChain(t._2));
	}

	private SimpleStructure getChain(PdbChainId id) {
		try {
			SimpleStructure s = provider.getStructure(id.getPdb());
			s.removeChainsExcept(s.getRandomChain(random));
			return s;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
