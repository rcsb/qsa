package pdb;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 *
 * @author Antonin Pavelka
 */
public class PdbChainId implements Serializable {

	private static final long serialVersionUID = 1L;
	private String pdb;
	private ChainId chain;

	public PdbChainId(String s) {
		if (s.length() == 5 && !s.contains(":") && !s.contains(".")) {
			pdb = s.substring(0, 4);
			chain = new ChainId(s.charAt(4));
		} else {
			StringTokenizer st = new StringTokenizer(s, ".");
			pdb = st.nextToken();
			if (st.hasMoreTokens()) {
				chain = new ChainId(st.nextToken());
			}
		}
	}

	public PdbChainId(String pdb, ChainId chain) {
		this.pdb = pdb;
		this.chain = chain;
	}

	public String getPdb() {
		return pdb;
	}

	public ChainId getChain() {
		return chain;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		PdbChainId other = (PdbChainId) o;
		return toString().equals(other.toString());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + Objects.hashCode(this.pdb);
		hash = 79 * hash + Objects.hashCode(this.chain);
		return hash;
	}

	@Override
	public String toString() {
		if (chain == null) {
			return pdb + "_";
		} else {
			return pdb + chain;
		}
	}
}
