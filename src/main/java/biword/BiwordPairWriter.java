package biword;

import io.Directories;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;

/**
 *
 * @author Antonin Pavelka
 *
 * A file for biword matches found in each structure. Ids of biwords are added to that file. The idea here is to collect
 * all matches and store them without wasting RAM. Later, each alignment can be built separatelly for each structure, so
 * there is no need to store all matching biwords in RAM.
 *
 */
public class BiwordPairWriter {

	private Directories dirs = Directories.createDefault();
	public long counter;
	private DataOutputStream[] doss;
	//TODO try to store all information first in memory, or just count it first
	//then access this storage to build alignment for each file sep, one full flow for each

	public BiwordPairWriter(int structureN) {
		doss = new DataOutputStream[structureN];
	}

	public void add(int queryBiwordId, int targetStructureId, int targetBiwordId) {
		counter++;
		DataOutputStream dos = doss[targetStructureId];
		if (dos == null) {
			try {
				dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(
					dirs.getBiwordHits(targetStructureId))));
				doss[targetStructureId] = dos;
			} catch (Exception ex) {
				throw new RuntimeException(ex);

			}
		}
		try {
			dos.writeInt(queryBiwordId);
			dos.writeInt(targetBiwordId);
			//dos.writeDouble(rmsd);
		} catch (Exception ex) {
			throw new RuntimeException(ex);

		}
	}

	public void close() {
		for (DataOutputStream dos : doss) {
			if (dos != null) {
				try {
					dos.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

}
