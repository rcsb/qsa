package embedding;

import algorithm.Biword;
import algorithm.BiwordedStructure;
import fragment.serialization.BiwordLoader;
import global.Parameters;
import probability.sampling.ReservoirSample;

/**
 *
 * @author Antonin Pavelka
 */
public class Vectorizers {

	private Parameters parameters;

	public Vectorizers(Parameters parameters) {
		this.parameters = parameters;
	}

	public Vectorizer create(BiwordLoader biwordLoader) {
		ReservoirSample<Biword> sample = new ReservoirSample(parameters.getLipschitzFragmentSampleSize(), 1);
		for (BiwordedStructure biwordedStructure : biwordLoader) {
			Biword[] biwords = biwordedStructure.getBiwords();
			for (Biword biword : biwords) {
				sample.add(biword);
			}
				
		}
	}

}
