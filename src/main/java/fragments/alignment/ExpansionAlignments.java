package fragments.alignment;

import algorithm.graph.AwpGraph;
import algorithm.graph.AwpNode;
import global.Parameters;
import java.util.ArrayList;
import java.util.List;

public class ExpansionAlignments {

	private List<ExpansionAlignment> list = new ArrayList<>();
	private int normalizationLength;
	private boolean[] covered;
	private Parameters parameters;

	public static ExpansionAlignments createExpansionAlignments(Parameters parameters, AwpGraph graph,
		int queryLength, int targetLength) {

		ExpansionAlignments alignments = new ExpansionAlignments();

		alignments.parameters = parameters;
		alignments.covered = new boolean[graph.size()];
		alignments.normalizationLength = parameters.getReferenceLength(queryLength, targetLength);

		alignments.addNodes(graph);

		alignments.filter();

		return alignments;
	}

	public List<ExpansionAlignment> getAlignments() {
		return list;
	}

	private void addNodes(AwpGraph graph) {
		for (AwpNode origin : graph.getNodes()) {
			double componentSize = ((double) origin.getComponent().sizeInResidues()) / normalizationLength;
			if (componentSize < parameters.getMinComponentSize()) {
				continue;
			}
			if (!covers(origin)) {
				ExpansionAlignment aln = new ExpansionAlignment(parameters, origin, graph, normalizationLength);
				add(aln);
			}
		}
	}

	private void add(ExpansionAlignment alignment) {
		list.add(alignment);
		for (AwpNode n : alignment.getNodes()) {
			covered[n.getId()] = true;
		}
	}

	private void filter() {		
		int max = 0;
		for (ExpansionAlignment c : list) {
			int r = c.sizeInResidues();
			if (max < r) {
				max = r;
			}
		}
		List<ExpansionAlignment> good = new ArrayList<>();
		for (ExpansionAlignment c : list) {
			int n = c.sizeInResidues();

			// TODO custom!!
			
			//System.out.println("SSSSSSSSSSSSSSSSS " + n + " " + minStrSize + " " + max);
			if (n >= 15 && (n >= normalizationLength / 5) && n >= (max / 5)) {
				good.add(c);
			}
		}
		
		list = good;
	}

	public boolean covers(AwpNode node) {
		return covered[node.getId()];
	}

}
