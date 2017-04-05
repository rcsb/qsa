package org.rcsb.mmtf.benchmark.io;

import org.rcsb.mmtf.benchmark.io.RetryableHttpStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.FileUtils;
import org.rcsb.mmtf.decoder.ReaderUtils;

public class MmtfFileUtils {

	public static void downloadMmtf(String code, Path path) throws IOException {
		download("http://mmtf.rcsb.org/v1.0/full/" + code + ".mmtf.gz", path);
	}

	public static void downloadMmtfReduced(String code, Path path) throws IOException {
		download("http://mmtf.rcsb.org/v1.0/reduced/" + code + ".mmtf.gz", path);
	}

	public static void downloadPdb(String code, Path path) throws IOException {
		download("https://files.rcsb.org/download/" + code + ".pdb.gz", path);
	}

	public static void downloadCif(String code, Path path) throws IOException {
		download("https://files.rcsb.org/download/" + code + ".cif.gz", path);
	}

	public static void download(String sourceUrl, Path targetFile)
		throws MalformedURLException, IOException {
		URL url = new URL(sourceUrl);
		try (BufferedInputStream is
			= new BufferedInputStream(url.openStream())) {
			Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * Able to reconnect in case of network problems, suitable for big files.
	 */
	public static void downloadRobust(String sourceUrl, Path targetFile)
		throws MalformedURLException, IOException {
		URL url = new URL(sourceUrl);
		try (BufferedInputStream is
			= new BufferedInputStream(new RetryableHttpStream(url))) {
			Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public static void downloadAndUnzip(String sourceUrl, Path targetPath)
		throws MalformedURLException, IOException {
		URL url = new URL(sourceUrl);
		try (InputStream is = url.openStream(); FileOutputStream fos
			= new FileOutputStream(targetPath.toString());) {
			GZIPInputStream gzis = new GZIPInputStream(is);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = gzis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
		}
	}

	public static void unzip(File in, File out) throws IOException {
		byte[] zipped = Files.readAllBytes(in.toPath());
		byte[] unzipped = ReaderUtils.deflateGzip(zipped);
		FileUtils.writeByteArrayToFile(out, unzipped);
	}

}
