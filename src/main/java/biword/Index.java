package biword;

import biword.serialization.BiwordSaver;
import algorithm.Biword;
import algorithm.BiwordedStructure;
import biword.serialization.BiwordLoader;
import global.FlexibleLogger;
import global.Parameters;
import grid.sparse.Buffer;
import grid.sparse.MultidimensionalArray;
import global.io.Directories;
import grid.sparse.BufferOfLong;
import pdb.Structures;
import util.Distribution;
import util.Time;
import util.Timer;

/**
 * In memory index and biword database.
 */
public class Index {

	private final Parameters parameters;
	private final Directories dirs;
	private final double[] globalMin = new double[10];
	private final double[] globalMax = new double[10];
	private final int bracketN;
	private int biwordN = 0;
	private MultidimensionalArray grid;
	private BufferOfLong out;
	private final float[] box;

	public Index(Parameters parameters, Directories dirs, Structures structureProvider) {
		this.parameters = parameters;
		this.bracketN = this.parameters.getIndexBins();
		this.dirs = dirs;
		float angleDiff = (float) parameters.getAngleDifference();
		float shift = (float) parameters.getCoordinateDifference();
		box = new float[10];
		for (int i = 0; i < 4; i++) {
			box[i] = angleDiff;
		}
		for (int i = 4; i < 10; i++) {
			box[i] = shift;
		}
		build(structureProvider);
	}

	private BiwordLoader getBiwordLoader() {
		return new BiwordLoader(parameters, dirs);
	}

	private void build(Structures structureProvider) {
		if (!parameters.hasExternalBiwordSource()) {
			createAndSaveBiwords(structureProvider);
		}
		initializeBoundaries();
		createIndex();
		//analyze();
	}

	private void analyze() {
		System.out.println("Buckets");
		Distribution a = new Distribution();
		//for (Bucket bucket : Bucket.list) {
		//	a.add(bucket.size());
		//}
		a.print();
		System.out.println("TinyMaps");
		Distribution b = new Distribution();
		//for (TinyMap tinyMap : TinyMap.list) {
		//		b.add(tinyMap.size());
		//	}
		b.print();
	}

	private void createAndSaveBiwords(Structures structures) {
		Timer.start();
		BiwordsCreator biwordsProvider = new BiwordsCreator(parameters, dirs, structures, false);
		BiwordSaver biwordSaver = new BiwordSaver(parameters, dirs);
		for (BiwordedStructure bs : biwordsProvider) {
			try {
				System.out.println("Initialized structure " + bs.getStructure().getSource() + " " + bs.getStructure().getId());
				biwordSaver.save(bs.getStructure().getId(), bs);
			} catch (Exception ex) {
				FlexibleLogger.error(ex);
			}
		}
		Timer.stop();
		System.out.println("creating structure, biwords and boundaries " + Timer.get());
	}

	private void initializeBoundaries() {
		Timer.start();
		int counter = 0;
		for (BiwordedStructure bs : getBiwordLoader()) {
			counter++;
			if (counter % 1000 == 0) {
				System.out.println("boundaries " + counter);
			}
			try {
				for (Biword bw : bs.getBiwords()) {
					float[] v = bw.getSmartVector();
					if (v == null) {
						continue;
					}
					biwordN++;
					for (int d = 0; d < v.length; d++) {
						if (v[d] < globalMin[d]) {
							globalMin[d] = v[d];
						}
						if (v[d] > globalMax[d]) {
							globalMax[d] = v[d];
						}
					}
				}
			} catch (Exception ex) {
				FlexibleLogger.error(ex);
			}
		}
		Timer.stop();
		System.out.println("creating structure, biwords and boundaries " + Timer.get());
		// now build the index tree using loading from HDD for each structure
		out = new BufferOfLong(biwordN);
		if (true) {
			printBoundaries();
		}
	}

	private void createIndex() {
		System.out.println("inserting...");
		Time.start("index insertions");
		grid = new MultidimensionalArray(parameters.getIndexDimensions(), parameters.getIndexBins(), biwordN);
		for (int i = 0; i < 4; i++) { // angles are cyclic - min and max values are neighbors
			grid.setCycle(i);
		}
		for (BiwordedStructure bs : getBiwordLoader()) {
			System.out.println("inserting index for structure "
				+ bs.getStructure().getId() + " "
				+ bs.getStructure().getSource().getPdbCode() + " size " + bs.getStructure().size());
			try {
				Timer.start();
				Biword[] biwords = bs.getBiwords();
				for (Biword bw : biwords) {
					float[] v = bw.getSmartVector();
					if (v != null) {
						grid.insert(discretize(v), bw.getId().endcode());
					}
				}
				Timer.stop();
				long t = Timer.get();
				System.out.println("insert " + t + " per bw " + ((double) t / biwords.length));
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		Time.stop("index insertions");
		Time.print();
	}

	private void printBoundaries() {
		System.out.println("BOUNDARIES");
		for (int d = 0; d < globalMin.length; d++) {
			System.out.println(globalMin[d] + " - " + globalMax[d] + " | ");
		}
		System.out.println("----");
	}

	public BufferOfLong query(Biword bw) {
		float[] vector = bw.getSmartVector();
		int dim = vector.length;
		float[] min = new float[dim];
		float[] max = new float[dim];
		for (int i = 0; i < dim; i++) {
			min[i] = vector[i] - box[i];
			max[i] = vector[i] + box[i];
		}
		out.clear();
		grid.getRange(discretize(min), discretize(max), out);
		return out;
	}

	private byte[] discretize(float[] x) {
		byte[] indexes = new byte[x.length];
		for (int i = 0; i < x.length; i++) {
			float v = x[i];
			int index = (int) Math.floor((v - globalMin[i]) / (globalMax[i] - globalMin[i]) * bracketN);
			if (index < Byte.MIN_VALUE || index > Byte.MAX_VALUE) {
				throw new RuntimeException();
			}
			indexes[i] = (byte) index;
		}
		return indexes;
	}
}
