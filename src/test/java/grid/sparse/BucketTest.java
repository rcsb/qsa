/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grid.sparse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author kepler
 */
public class BucketTest extends TestCase {

	public BucketTest(String testName) {
		super(testName);
	}

	public void testAdd() {
		List<Long> correct = new ArrayList<>();
		Bucket bucket = new Bucket(1);
		correct.add((long) 1);
		for (int i = 2; i < 100; i++) {
			bucket.add(i);
			correct.add((long) i);
		}
		List<Long> fromBucket = new ArrayList<>();
		for (int i = 0; i < bucket.size(); i++) {
			//System.out.println(bucket.get(i));
			fromBucket.add(bucket.get(i));
		}
		Collections.sort(correct);
		Collections.sort(fromBucket);
		assert correct.size() == fromBucket.size();
		assert correct.size() == bucket.size();
		for (int i = 0; i < correct.size(); i++) {
			assert correct.get(i).equals(fromBucket.get(i));
		}
	}

}
