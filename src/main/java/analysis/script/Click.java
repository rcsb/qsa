/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.script;

import analysis.PairLoader;
import java.io.IOException;
import util.Pair;

/**
 *
 * @author kepler
 */
public class Click {

	public void saveStructures(Pair<String> pair) throws IOException {
		throw new UnsupportedOperationException();
		/*	String[] ids = {pair.x, pair.y};
		for (String id : ids) {
			Path p = dirs.getClickInput(pair, id);
			List<Chain> chains = provider.getSingleChain(id);
			assert chains.size() == 1 : pair.x + " " + pair.y + " " + chains.size();
			LineFile lf = new LineFile(p.toFile());
			lf.write(chains.get(0).toPDB());
		}
		 */
	}

	private void clickEvaluation(Pair<String> pair, int alignmentNumber) throws IOException {
		/*System.out.println(dirs.getClickOutput(pair, pair.x, pair.y).toString());
		System.out.println(dirs.getClickOutput(pair, pair.x, pair.y).toString());
		Structure sa = provider.getStructurePdb(dirs.getClickOutput(pair, pair.x, pair.y).toString());
		Structure sb = provider.getStructurePdb(dirs.getClickOutput(pair, pair.y, pair.x).toString());
		SimpleStructure a = StructureFactory.convertProteinChains(sa.getModel(0), pair.x);
		SimpleStructure b = StructureFactory.convertProteinChains(sb.getModel(0), pair.y);
		ResidueAlignment eq = WordAlignmentFactory.create(a, b);
		eo.saveResults(eq, 0, 0);
		eo.visualize(eq, null, 0, alignmentNumber, 1);*/
	}

	public static void main(String[] args) {
		/*PairLoader pg = new PairLoader(dirs.getTopologyIndependentPairs(), false);
		for (int i = 0; i < Math.min(pairNumber, pg.size()); i++) {
			try {
				Pair<String> pair = pg.getNext();
				System.out.println(i + " " + pair.x + " " + pair.y);
				switch (mode) {
					case CLICK_SAVE:
						saveStructures(pair);
						break;
					case CLICK_EVAL:
						clickEvaluation(pair, i + 1);
						break;
				}
				long time2 = System.nanoTime();
				double ms = ((double) (time2 - time1)) / 1000000;
			} catch (Error ex) {
				ex.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}*/
	}
}
