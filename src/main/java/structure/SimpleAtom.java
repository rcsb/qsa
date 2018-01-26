package structure;

import geometry.Point;

/**
 *
 * @author Antonin Pavelka
 */
public class SimpleAtom {

	private int serial;
	private Point position;
	private ResidueId residueId;
	private String name;
	private String element;

	public SimpleAtom(int serial, Point position, ResidueId residueId, String name, String element) {
		this.serial = serial;
		this.position = position;
		this.residueId = residueId;
		this.name = name;
		this.element = element;
	}

	public int getSerial() {
		return serial;
	}

	public Point getPosition() {
		return position;
	}

	public ResidueId getResidueId() {
		return residueId;
	}

	public String getName() {
		return name;
	}

	public String getElement() {
		return element;
	}
}
