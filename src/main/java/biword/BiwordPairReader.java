package biword;

import io.Directories;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordPairReader {

	private Directories dirs = Directories.createDefault();
	public long counter;
	//TODO try to store all information first in memory, or just count it first
	//then access this storage to build alignment for each file sep, one full flow for each
	private File[] files;
	private DataInputStream dis;
	private int queryStructureId;
	private int queryBiwordId;
	private int targetStructureId;
	private int targetBiwordId;
	private double rmsd;

	public BiwordPairReader() {
		files = dirs.getBiwordHitsDir().toFile().listFiles();
	}

	public int size() {
		return files.length;
	}

	public void open(int index) {
		try {
			if (dis != null) {
				dis.close();
			}
			dis = new DataInputStream(new BufferedInputStream(new FileInputStream(files[index])));
			targetStructureId = Integer.parseInt(files[index].getName());
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void close() {
		try {
			dis.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public boolean loadNext(int index) {
		try {
			queryBiwordId = dis.readInt();
			targetBiwordId = dis.readInt();
			rmsd = dis.readDouble();
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	public int getQueryStructureId() {
		return queryStructureId;
	}

	public int getQueryBiwordId() {
		return queryBiwordId;
	}

	public int getTargetStructureId() {
		return targetStructureId;
	}

	public int getTargetBiwordId() {
		return targetBiwordId;
	}

	public double getRmsd() {
		return rmsd;
	}

}
