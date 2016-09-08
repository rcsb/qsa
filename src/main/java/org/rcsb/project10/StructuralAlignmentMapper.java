package org.rcsb.project10;

import java.util.List;

import javax.vecmath.Point3d;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;

import scala.Tuple2;

/**
 * This class maps a pair of chains, specified by two indices into the broadcasted data
 * to a vector of alignment scores
 * 
 * @author  Peter Rose
 */
public class StructuralAlignmentMapper implements Function<Tuple2<Integer,Integer>,String> {
	private static final long serialVersionUID = 1L;
	private Broadcast<List<Tuple2<String, WritableSegment>>> data = null;

	public StructuralAlignmentMapper(Broadcast<List<Tuple2<String,WritableSegment>>> data) {
		this.data = data;
	}

	/**
	 * Returns a CSV-formated string with chainId pair and alignment metrics
	 */
	public String call(Tuple2<Integer, Integer> tuple) throws Exception {
		Tuple2<String,WritableSegment> t1 = this.data.getValue().get(tuple._1);
		Tuple2<String,WritableSegment> t2 = this.data.getValue().get(tuple._2);
		
		StringBuilder result = new StringBuilder();
		
		// add chainIds
		result.append(t1._1);
		result.append(",");
		result.append(t2._1);
		
		// run the structural alignment
		Point3d[] points1 = t1._2.getCoordinates();
		Point3d[] points2 = t2._2.getCoordinates();
		Float[] scores = TmScorer.getFatCatTmScore(points1, points2);
		
		// add alignment scores
		for (Float score: scores) {
			result.append(",");
			result.append(score);
		}
		
        return result.toString();
    }
}