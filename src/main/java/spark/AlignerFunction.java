package spark;

import org.apache.spark.api.java.function.Function;

import alignment.AlignmentQuality;
import fragments.FragmentsAligner;
import fragments.Fragments;
import fragments.FragmentsFactory;
import pdb.CompactStructure;
import pdb.SimpleStructure;
import scala.Tuple2;

/**
 *
 * @author Antonin Pavelka
 */
public class AlignerFunction implements Function<Tuple2<CompactStructure, CompactStructure>, AlignmentQuality> {
	private FragmentsAligner aligner_;
	private FragmentsFactory ff_;

	public AlignerFunction(FragmentsAligner aligner, FragmentsFactory ff) {
		aligner_ = aligner;
		ff_ = ff;
	}

	@Override
	public AlignmentQuality call(Tuple2<CompactStructure, CompactStructure> t) {
		SimpleStructure ssa = new SimpleStructure(t._1);
		SimpleStructure ssb = new SimpleStructure(t._2);
		/*
		 * Printer.println("ssa " + ssa.size()); Printer.println("ssb " +
		 * ssb.size()); Visualization.visualize(ssa.getPoints(), 'A', new
		 * File("c:/tonik/rozbal/" + ssa.getId()));
		 * Visualization.visualize(ssb.getPoints(), 'B', new
		 * File("c:/tonik/rozbal/" + ssb.getId()));
		 */
		Fragments a = ff_.create(ssa, 1);
		Fragments b = ff_.create(ssb, 1);
		// Printer.println("a " + a.size());
		// Printer.println("b " + b.size());
		return aligner_.align(a, b);
	}
}
