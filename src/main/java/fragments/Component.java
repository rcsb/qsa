package fragments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Component {

	private List<AwpNode> nodes = new ArrayList<>();
	public static Set<Component> all;

	public static void clear() {
		all = new HashSet<>();
	}

	private Component() {
	}

	public static Component create(AwpNode firstNode) {
		Component c = new Component();
		c.nodes.add(firstNode);
		all.add(c);
		return c;
	}

	public int size() {
		return nodes.size();
	}

	public void eat(Component other) {
		for (AwpNode o : other.nodes) {
			o.setComponent(other);
		}
		all.remove(other);
		nodes.addAll(other.nodes);
	}
}
