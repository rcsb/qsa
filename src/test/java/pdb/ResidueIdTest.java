package pdb;

import structure.ResidueId;
import structure.ChainId;
import java.util.HashSet;
import java.util.Random;
import junit.framework.TestCase;

/**
 *
 * @author Antonin Pavelka
 */
public class ResidueIdTest extends TestCase {

	private Random random = new Random(1);

	public ResidueIdTest(String testName) {
		super(testName);
	}

	// without insertion
	private ResidueId r(int index) {
		return ResidueId.createWithoutInsertion(new ChainId('A'), index);
	}

	// with insertion
	private ResidueId ri(int index, char insertion) {
		return new ResidueId(new ChainId('A'), index, insertion);
	}

	public void testCreateFromString() {
		ChainId chain = new ChainId('a');
		ResidueId a = ResidueId.createFromString(chain, "12B");
		assert a.getInsertionCode() == 'B';
		assert a.getSequenceNumber() == 12;
		ResidueId b = ResidueId.createFromString(chain, "34");
		assert b.getInsertionCode() == null;
		assert b.getSequenceNumber() == 34;
	}

	public void testIsFollowedBy() {
		isFollowedByWithoutInsertions();
		isFollowedByWithInsertions();
	}

	public void isFollowedByWithoutInsertions() {
		ResidueId a = r(5);
		ResidueId b = r(6);
		ResidueId c = r(7);

		assert a.isFollowedBy(b);
		assert b.isFollowedBy(c);

		assert !b.isFollowedBy(b);
		assert !b.isFollowedBy(a);
	}

	public void isFollowedByWithInsertions() {
		ResidueId a = r(5);
		ResidueId b = ri(5, 'A');
		ResidueId c = r(6);

		assert a.isFollowedBy(b);
		assert !b.isFollowedBy(a);
		assert b.isFollowedBy(c);

		assert b.isFollowedBy(c); // not ideal, but not resolvable without full sequence
	}

	public void testHashCode() {
		HashSet<ResidueId> set = new HashSet<>();
		int n = 1000;
		for (int i = 0; i < n; i++) {
			ResidueId id = ResidueId.createWithoutInsertion(new ChainId('A'), i);
			set.add(id);
		}
		set.add(ResidueId.createWithoutInsertion(new ChainId('A'), 56));
		assert set.size() == n;
		// test retrieval
		for (int i = 0; i < n; i++) {
			ResidueId id = ResidueId.createWithoutInsertion(new ChainId('A'), i);
			assert set.contains(id);
		}
	}

	public void testCompareTo() {
		ResidueId a = ResidueId.createWithoutInsertion(new ChainId('A'), 5);
		ResidueId b = ResidueId.createWithoutInsertion(new ChainId('A'), 6);

		assert a.compareTo(b) < 0;
		assert b.compareTo(a) > 0;

		assert a.compareTo(a) == 0;
		assert b.compareTo(b) == 0;

		assert !a.equals(b);
		assert !b.equals(a);
		assert a.equals(a);
		assert b.equals(b);

		ResidueId c = new ResidueId(new ChainId('A'), 6, 'A');
		ResidueId d = new ResidueId(new ChainId('A'), 6, 'B');

		assert b.compareTo(c) < 0;
		assert c.compareTo(b) > 0;
		assert c.compareTo(d) < 0;
		assert d.compareTo(c) > 0;
		assert c.compareTo(c) == 0;

		assert !b.equals(c);
	}

}
