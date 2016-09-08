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
	
	/*public static Path deflateGzip(Path input, Path output) throws IOException {
		
		ByteArrayInputStream byteInputStream = new ByteArrayInputStream(FileUtils.readFileToByteArray(input.toFile()));
		GZIPInputStream gzipInputStream;
		ByteArrayOutputStream byteArrayOutputStream = null;
		try {
			gzipInputStream = new GZIPInputStream(byteInputStream);

			byteArrayOutputStream = new ByteArrayOutputStream();
			// Make a buffer
			byte[] buffer = new byte[BYTE_BUFFER_CHUNK_SIZE];

			while (gzipInputStream.available() == 1) {
				int size = gzipInputStream.read(buffer);
				if(size==-1){
					break;
				}
				byteArrayOutputStream.write(buffer, 0, size);
			}

		} finally {
			if (byteArrayOutputStream != null) {
				byteArrayOutputStream.close();
			}			
		}
		return  byteArrayOutputStream.toByteArray();
	}*/
}
