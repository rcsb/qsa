package spark.interfaces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import analysis.MySerializer;
import pdb.SimpleStructure;

public class MassAligner implements Serializable {

	private static final long serialVersionUID = 1L;
	private StructureAlignmentAlgorithm saa;
	private SimpleStructure[] structures;
	private MySerializer serializer;

	public MassAligner(StructureAlignmentAlgorithm saa, SimpleStructure[] structures, MySerializer serializer) {
		this.saa = saa;
		this.structures = structures;
		this.serializer = serializer;
	}

	public void generateSmall(List<AlignablePair> pairs) {
		for (int xi = 0; xi < 10; xi++) {
			for (int yi = 0; yi < 1; yi++) {
				pairs.add(new AlignablePair(structures[xi], structures[yi]));
			}
		}
	}
	
	public void generateFull(List<AlignablePair> pairs) {
		for (int xi = 0; xi < structures.length; xi++) {
			for (int yi = 0; yi < xi; yi++) {
				pairs.add(new AlignablePair(structures[xi], structures[yi]));
			}
		}
	}
	
	public void run() {
		List<AlignablePair> pairs = new ArrayList<>();
		generateFull(pairs);		
		for (AlignablePair p : pairs) {
			Alignment a = saa.align(p);
			serializer.serialize(a);
		}
	}
}
