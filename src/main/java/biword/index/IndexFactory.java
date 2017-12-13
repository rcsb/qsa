package biword.index;

import biword.serialization.BiwordSaver;
import algorithm.Biword;
import algorithm.BiwordedStructure;
import biword.BiwordsCreator;
import biword.serialization.BiwordLoader;
import global.FlexibleLogger;
import global.Parameters;
import global.io.Directories;
import pdb.Structures;
import util.Time;
import util.Timer;

/**
 * In memory index and biword database.
 */
public class IndexFactory {

	private final Parameters parameters;
	private final Directories dirs;
	private final double[] globalMin;
	private final double[] globalMax;
	private int biwordN = 0;
	private final float[] box;
	private Index index;
	private final String structureSetId;

	IndexFactory(Parameters parameters, Directories dirs, Structures structures) {
		this.parameters = parameters;
		this.dirs = dirs;
		this.structureSetId = structures.getId();
		float angleDiff = (float) parameters.getAngleDifference();
		float shift = (float) parameters.getCoordinateDifference();
		globalMin = new double[parameters.getIndexDimensions()];
		globalMax = new double[parameters.getIndexDimensions()];
		box = new float[parameters.getIndexDimensions()];
		for (int i = 0; i < 4; i++) {
			box[i] = angleDiff;
		}
		for (int i = 4; i < 10; i++) {
			box[i] = shift;
		}
		build(structures);
	}

	Index getIndex() {
		return index;
	}

	private BiwordLoader getBiwordLoader() {
		return new BiwordLoader(parameters, dirs, structureSetId);
	}

	private void build(Structures structureProvider) {
		if (!dirs.getBiwordsDir(structureSetId).toFile().exists()) {
			createAndSaveBiwords(structureProvider);
		}
		initializeBoundaries();
		createIndex();
	}

	private void createAndSaveBiwords(Structures structures) {
		Timer.start();
		BiwordsCreator biwordsProvider = new BiwordsCreator(parameters, dirs, structures, false);
		BiwordSaver biwordSaver = new BiwordSaver(parameters, dirs);
		for (BiwordedStructure bs : biwordsProvider) {
			try {
				System.out.println("Initialized structure " + bs.getStructure().getSource() + " "
					+ bs.getStructure().getId());
				biwordSaver.save(structureSetId, bs.getStructure().getId(), bs);
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
	}

	private void createIndex() {
		System.out.println("inserting...");
		Time.start("index insertions");
		index = new Index(parameters.getIndexDimensions(), parameters.getIndexBins(), biwordN, box,
			globalMin, globalMax);
		for (BiwordedStructure bs : getBiwordLoader()) {
			System.out.println("inserting index for structure "
				+ bs.getStructure().getId() + " "
				+ bs.getStructure().getSource().getPdbCode() + " size " + bs.getStructure().size());
			try {
				Timer.start();
				Biword[] biwords = bs.getBiwords();
				for (Biword bw : biwords) {
					index.insert(bw);

				}
				Timer.stop();
				long t = Timer.get();
				System.out.println("insert " + t + " per bw " + ((double) t / biwords.length));
			} catch (Exception ex) {
				FlexibleLogger.error(ex);
			}
		}
		Time.stop("index insertions");
		Time.print();
	}

}
