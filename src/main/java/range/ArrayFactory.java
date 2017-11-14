package range;

import global.Parameters;

/**
 *
 * @author Antonin Pavelka
 *
 */
public class ArrayFactory {

	private final Parameters parameters = Parameters.create();

	/**
	 * Creates a degenerate array with one object.
	 */
	public Array create(byte index, Object content) {
		return new SingleItemArray(index, content);
	}

	/**
	 * Expands if it is necessary for insertion at index.
	 */
	private Array expandIfNeeded(Array node, int index) {
		if (node instanceof SingleItemArray) {
			SingleItemArray singleNode = (SingleItemArray) node;
			if (singleNode.getIndex() == index) {
				return node;
			} else { // upgrade node
				FullArray fullNode = new FullArray(parameters.getIndexBrackets());
				// copy content
				fullNode.put(singleNode.getIndex(), singleNode.getContent());
				return fullNode;
			}
		} else if (node instanceof FullArray) {
			return node;
		} else {
			throw new RuntimeException();
		}
	}

	/**
	 * Returns the children node of a tree
	 */
	/*public Array createAndBindNextLevel(Array current, int currentIndex, int nextIndex) {
		assert current != null;
		
		if (current instanceof SingleItemArray) {
			SingleItemArray currentLevel = (SingleItemArray) current;
			if (currentLevel.isEmpty()) {
				SingleItemArray newLevel = new SingleItemArray();
				currentLevel.put(index, newLevel);
				return newLevel;
			} else if (currentLevel.getIndex() == index) {
				if (currentLevel.getContent() == null) { 
					assert false;
					SingleItemArray newLevel = new SingleItemArray();
					currentLevel.put(index, newLevel); // add second child
					return newLevel;
				} else {
					return (Array) currentLevel.getContent(); // return the only path down
				}
			} else { // upgrade to full length array
				FullArray fa = new FullArray(parameters.getIndexBrackets());
				fa.put(currentLevel.getIndex(), currentLevel.getContent()); // copy degenerate array into full array
				SingleItemArray newLevel = new SingleItemArray();
				fa.put(index, newLevel); // add second child
				assert newLevel != null;
				return newLevel;
			}
		} else if (current instanceof FullArray) {
			Array newLevelMaybe = (Array) current.get(index);
			if (newLevelMaybe == null) {
				SingleItemArray newLevel = new SingleItemArray();
				current.put(index, newLevel);
				assert newLevel != null;
				return newLevel;
			} else {
				assert newLevelMaybe != null;
				return newLevelMaybe;
			}
		} else {
			throw new RuntimeException();
		}

		//test if both are connected, etc
		//current size is +1
	}*/
}
