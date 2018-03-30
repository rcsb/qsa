package fragment;

import fragment.word.WordsFactory;
import fragment.word.Words;
import java.util.ArrayList;
import java.util.List;
import testing.TestResources;
import junit.framework.TestCase;
import structure.PdbEntries;
import structure.Residue;
import structure.SimpleChain;
import structure.SimpleStructure;
import structure.StructureParsingException;
import structure.StructureSource;

/**
 *
 * @author Antonin Pavelka
 */
public class WordsFactoryTest extends TestCase {

	private TestResources resources = new TestResources();
	private SimpleStructure structure;
	private WordsFactory factory;

	public WordsFactoryTest(String testName) throws Exception {
		super(testName);
		structure = resources.create(new StructureSource("1cv2"));
		factory = new WordsFactory(resources.getParameters(), structure, 1);
	}

	public void testCreate() throws Exception {
		Words words = factory.create();
		check(structure, words);
	}

	private void check(SimpleStructure structure, Words words) {
		assert structure.size() == 293 : structure.size();
		assert words.size() == 288 : words.size();
	}

	public void testIsContinuous() {
		testIsContinuous(structure);
	}

	private void testIsContinuous(SimpleStructure structure) {
		for (SimpleChain chain : structure.getChains()) {
			Residue[] residues = chain.getResidues();
			for (int i = 0; i < residues.length - 1; i++) {
				Residue a = residues[i];
				Residue b = residues[i + 1];
				if (a.isFollowedBy(b)) {
					assert factory.areConnected(a, b) : a + " " + b;
				}
			}
		}
	}

	public void testIsContinuousFull() throws Exception {
		PdbEntries entries = resources.getPdbEntries();
		int shortChains = 0;
		int mysterious = 0;
		int other = 0;
		List<StructureSource> problems = new ArrayList<>();
		if (resources.doFullTest()) {
			for (int i = 0; i < entries.size(); i++) {
				StructureSource source = null;
				try {
					source = entries.getNextRandom();
					System.out.println("testing " + source);
					testIsContinuous(resources.create(source));
				} catch (StructureParsingException ex) {
					problems.add(source);
					if (ex.isMysterious()) {
						ex.printStackTrace();
						mysterious++;
					} else {
						shortChains++;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					other++;
				}
				System.out.println("short chains: " + shortChains);
				System.out.println("mysterious: " + mysterious);
				System.out.println("other: " + other);
				if (i % 1000 == 0) {
					for (StructureSource s : problems) {
						System.out.println(s);
					}
				}
			}

		}
	}

}
