package vectorization;

import algorithm.Biword;
import fragment.Word;
import structure.VectorizationException;
import structure.Residue;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordVectorizer {

	ObjectPairVectorizer objectPairVectorizer;

	public BiwordVectorizer(ObjectPairVectorizer objectPairVectorizer) {
		this.objectPairVectorizer = objectPairVectorizer;
	}

	public float[] vectorize(Biword biword) throws VectorizationException {
		RigidBody b1 = createRigidBody(biword.getFirstWord());
		RigidBody b2 = createRigidBody(biword.getSecondWord());
		float[] orientation = objectPairVectorizer.vectorize(b1, b2);
		float[] vector = new float[orientation.length + 4];
		try {
			Word word1 = biword.getFirstWord();
			Word word2 = biword.getSecondWord();
			Residue r1 = word1.getCentralResidue();
			Residue r2 = word2.getCentralResidue();
			vector[0] = r1.getPhi().floatValue();
			vector[1] = r2.getPhi().floatValue();
			vector[2] = r1.getPsi().floatValue();
			vector[3] = r2.getPsi().floatValue();
		} catch (Exception ex) {
			throw new VectorizationException(ex);
		}
		for (int i = 0; i < orientation.length; i++) {
			vector[i + 4] = orientation[i];
		}
		return vector;
	}

	private RigidBody createRigidBody(Word word) throws VectorizationException {
		Residue residue = word.getCentralResidue();
		throw new RuntimeException();
		//return new RigidBody(residue.getCa(), residue.getC(), residue.getN());
	}

}
