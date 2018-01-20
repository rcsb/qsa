package util;

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

public class MyFileUtils {

	public static void download(String sourceUrl, Path targetFile) throws MalformedURLException, IOException {
		URL url = new URL(sourceUrl);
		Files.copy(url.openStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
	}

	public static void decompress(Path sourcePath, Path destinationPath) throws IOException, DataFormatException {
		FileInputStream fis = new FileInputStream(sourcePath.toString());
		FileOutputStream fos = new FileOutputStream(destinationPath.toString());
		GZIPInputStream gzis = new GZIPInputStream(fis);
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = gzis.read(buffer)) > 0) {
			fos.write(buffer, 0, len);
		}
		fos.close();
		fis.close();
		gzis.close();
		buffer = null;
	}

}
