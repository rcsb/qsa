package biword;

import global.io.Directories;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author Antonin Pavelka
 */
public class BiwordPairReader {

	private final Directories dirs;
	public long counter;
	private final File[] files;
	private DataInputStream dis;
	private int queryStructureId;
	private int queryBiwordId;
	private int targetStructureId;
	private int targetBiwordId;
	private double rmsd;

	public BiwordPairReader(Directories dirs) {
		this.dirs = dirs;
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

	// TODO dis.available
	public boolean loadNext(int index) {
		try {
			queryBiwordId = dis.readInt();
			targetBiwordId = dis.readInt();
		} catch (EOFException ex) {
			return false;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
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
