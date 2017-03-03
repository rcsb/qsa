package structure;

import java.util.HashSet;
import java.util.Set;
import org.rcsb.mmtf.api.StructureDataInterface;
import org.rcsb.mmtf.dataholders.MmtfStructure;

/**
 *
 * @author Antonin Pavelka
 */
public class StructureLiteFactory {

    public StructureLite parseMmtf(String pdbCode, StructureDataInterface s) {
        StructureLite structure = new StructureLite(pdbCode);
        int[] chainsPerModel = s.getChainsPerModel();
        int mi = 0; // model index
        int ci = 0; // chain index
        int gi = 0; // group index
        int ai = 0; // atom index
        Set<ResidueId> added = new HashSet<>(); // to detect micro-heterogenity
        for (int mc_ = 0; mc_ < s.getNumModels(); mc_++) {
            ModelLite model = new ModelLite();
            added.clear();
            for (int cc_ = 0; cc_ < chainsPerModel[mi]; cc_++) {
                ChainId cid = new ChainId(s.getChainIds()[ci], s.getChainNames()[ci]);
                ChainLite chain = new ChainLite(cid);
                int chainGroupCount = s.getGroupsPerChain()[ci];
                for (int gc_ = 0; gc_ < chainGroupCount; gc_++) {
                    int group = s.getGroupTypeIndices()[gi];
					char ins = s.getInsCodes()[gi];					
                    ResidueId rid = new ResidueId(cid, s.getGroupIds()[gi],
                            s.getInsCodes()[gi], s.getGroupName(group));
                    int groupAtomCount = s.getGroupAtomNames(group).length;                    
                    ResidueLite r = new ResidueLite(rid);
                    for (int i = 0; i < groupAtomCount; i++) {
                        String element = s.getGroupElementNames(group)[i];
                        String name = s.getGroupAtomNames(group)[i];
                        int serial = s.getAtomIds()[ai];
                        float x = s.getxCoords()[ai];
                        float y = s.getyCoords()[ai];
                        float z = s.getzCoords()[ai];
                        float b = s.getbFactors()[ai];
						char alt = s.getAltLocIds()[ai];
						
						if (alt != MmtfStructure.UNAVAILABLE_CHAR_VALUE) {
							
						}
						
                        AtomLite a = new AtomLite(serial, name, alt, r, x, y, z,
							b, element);
                        r.addAtom(a);
                        ai++;
                    }
                    if (!added.contains(rid)) { // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!                                            
                        chain.add(r);
                        added.add(rid);
                    } else {
						/*ResidueLite a = chain.getResidue(rid);
						System.out.println(a.getAtomInfo());
						System.out.println("");
						System.out.println(r.getAtomInfo());
						System.out.println("---");*/
					}
                    gi++;
                }
                model.add(chain);
                ci++;
            }
            structure.add(model);
            mi++;
        }
        return structure;
    }
}
