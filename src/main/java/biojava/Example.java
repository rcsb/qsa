package biojava;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.PDBFileReader;

public class Example {
	public static void main(String[] args) {
		try {
			PDBFileReader reader = new PDBFileReader();

			// the path to the local PDB installation
			reader.setPath("/tmp");

			// are all files in one directory, or are the files split,
			// as on the PDB ftp servers?
			//reader.setPdbDirectorySplit(true);

			// should a missing PDB id be fetched automatically from the FTP
			// servers?
			//reader.setAutoFetch(true);

			// should the ATOM and SEQRES residues be aligned when creating the
			// internal data model?
			//reader.setAlignSeqRes(false);

			// should secondary structure get parsed from the file
			//reader.setParseSecStruc(false);

			Structure structure = reader.getStructureById("4hhb");

			System.out.println(structure);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
