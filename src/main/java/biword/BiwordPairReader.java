package biword;

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

	private final File file;
	private DataInputStream dis;
	private int targetStructureId;
	private int queryStructureId;
	private int queryBiwordId;
	private int targetBiwordId;

	public BiwordPairReader(File file) throws IOException {
		this.file = file;
		open();
	}

	private void open() throws IOException {
		dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		targetStructureId = Integer.parseInt(file.getName());
	}

	public void close() throws IOException {
		System.out.println("xxx");
		dis.close();
	}

	public int getTargetStructureId() {
		return targetStructureId;
	}

	public int getQueryStructureId() {
		return queryStructureId;
	}

	public int getQueryBiwordId() {
		return queryBiwordId;
	}

	public int getTargetBiwordId() {
		return targetBiwordId;
	}

	public boolean readNextBiwordPair() {
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
}
