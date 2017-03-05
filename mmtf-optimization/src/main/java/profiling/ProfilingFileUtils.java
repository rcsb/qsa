package profiling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.FileUtils;
import org.rcsb.mmtf.decoder.ReaderUtils;

public class ProfilingFileUtils {

	public static void downloadMmtf(String code, Path path) {
		try {
			if (Files.notExists(path)) {
				ProfilingFileUtils.download("http://mmtf.rcsb.org/v1.0/full/"
					+ code + ".mmtf.gz", path);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void downloadPdb(String code, Path path) {
		try {
			String mid = code.substring(1, 3);
			download("ftp://ftp.wwpdb.org/pub/pdb/data/structures/divided/pdb/"
				+ mid + "/pdb" + code + ".ent.gz", path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void downloadCif(String code, Path path) {
		try {
			String mid = code.substring(1, 3);
			download("ftp://ftp.rcsb.org/pub/pdb/data/structures/divided/mmCIF/"
				+ mid + "/pdb" + code + ".ent.gz", path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void download(String sourceUrl, Path targetFile) throws MalformedURLException, IOException {
		URL url = new URL(sourceUrl);
		Files.copy(url.openStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
	}

	public static void decompressData(Path sourcePath, Path destinationPath)
		throws IOException, DataFormatException {
		//Allocate resources.
		FileInputStream fis = new FileInputStream(sourcePath.toString());
		FileOutputStream fos = new FileOutputStream(destinationPath.toString());
		GZIPInputStream gzis = new GZIPInputStream(fis);
		byte[] buffer = new byte[1024];
		int len = 0;

		//Extract compressed content.
		while ((len = gzis.read(buffer)) > 0) {
			fos.write(buffer, 0, len);
		}

		//Release resources.
		fos.close();
		fis.close();
		gzis.close();
		buffer = null;
	}

	public static void unzip(File in, File out) throws IOException {
		byte[] zipped = Files.readAllBytes(in.toPath());
		byte[] unzipped = ReaderUtils.deflateGzip(zipped);
		FileUtils.writeByteArrayToFile(out, unzipped);
	}

}
