package pdb;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 *
 * @author Antonin Pavelka
 */
public class PdbChain implements Serializable {

	String pdb;
	ChainId chain;

	public PdbChain(String s) {
		StringTokenizer st = new StringTokenizer(s, ".");
		pdb = st.nextToken();
		if (st.hasMoreTokens()) {
			chain = new ChainId(st.nextToken());
		}
	}

	public PdbChain(String pdb, ChainId chain) {
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
		PdbChain other = (PdbChain) o;
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
			return pdb;
		} else {
			return pdb + "." + chain;
		}
	}
}
