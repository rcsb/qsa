/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdb.cath;

import java.util.StringTokenizer;
import pdb.ChainId;
import pdb.ResidueId;
import pdb.cath.tree.Classification;
import pdb.cath.tree.Domain;

/**
 *
 * @author Antonin Pavelka
 */
public class DomainParser {

	public Domain parse(String line) {
		StringTokenizer st = new StringTokenizer(line, " \t");
		String domainId = st.nextToken();
		String version = st.nextToken();
		String classification = st.nextToken();
		String segments = st.nextToken();

		Classification parsedClassification = new Classification(classification);
		Domain domain = new Domain(domainId, parsedClassification);
		parseSegments(segments, domain);
		return domain;
	}

	private void parseSegments(String fragments, Domain domain) {
		StringTokenizer st = new StringTokenizer(fragments, ",");
		while (st.hasMoreTokens()) {
			String segment = st.nextToken();
			Segment parsedSegment = parseSegment(segment);
			domain.addSegment(parsedSegment);
		}
	}

	private Segment parseSegment(String segment) {
		ChainId chainId = getChain(segment);
		String range = getRange(segment);

		StringTokenizer st = new StringTokenizer(range, "-");
		String start = st.nextToken();
		String end = st.nextToken();

		ResidueId startResidue = createResidue(chainId, start);
		ResidueId endResidue = createResidue(chainId, end);
		Segment parsedSegment = new Segment(startResidue, endResidue);
		return parsedSegment;
	}

	private ChainId getChain(String segment) {
		String chain = segment.substring(segment.length() - 1); // probably just one char ...
		return new ChainId(chain);
	}

	public String getRange(String segmentAndChain) {
		int length = segmentAndChain.length();
		assert segmentAndChain.charAt(length - 2) == ':'; // ... which is tested here
		String range = segmentAndChain.substring(0, length - 2);
		return range;
	}

	private ResidueId createResidue(ChainId chain, String residue) {
		return ResidueId.createFromString(chain, residue);
	}
}
