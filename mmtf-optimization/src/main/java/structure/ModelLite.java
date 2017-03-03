package structure;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author Antonin Pavelka
 */
public class ModelLite implements Serializable {

    private static final long serialVersionUID = 1L;
    private SortedMap<ChainId, ChainLite> chains = new TreeMap<>();    

    public ModelLite() {        
    }
    
    public int numberOfChains() {
        return chains.size();
    }
    
    public void add(ChainLite c) {    
        chains.put(c.getId(), c);
    }
    
    public ChainLite getChain(ChainId cid) {
        return chains.get(cid);
    }

    public Set<ChainId> getChainIds() {
        return chains.keySet();
    }
    
    public Collection<ChainLite> getChains() {
        return chains.values();
    }
}
