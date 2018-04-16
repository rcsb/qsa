package embedding.lipschitz.object;

/**
 *
 * @author Antonin Pavelka
 */
public interface AlternativePointTuples {

	public PointTuple getCanonicalTuple();

	public PointTuple getAlternative(int index, AlternativeMode alternativeMode);

}
