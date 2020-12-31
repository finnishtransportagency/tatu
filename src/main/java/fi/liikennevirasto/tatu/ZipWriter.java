package fi.liikennevirasto.tatu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipWriter {

	public static void createZipFile(File zipFile, List<File> filesToAdd, 
			List<String> originalFilenames, List<File> filesToRemove) throws IOException {

		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
		byte[] buffer = new byte[1024];		
		
		for (int i = 0; i < filesToAdd.size(); i++) {
			File file = filesToAdd.get(i);
			String name = originalFilenames.get(i);
			FileInputStream fis = new FileInputStream(file);
               out.putNextEntry(new ZipEntry(name));
               int len;
               while ((len = fis.read(buffer)) > 0)
                  out.write(buffer, 0, len);
               fis.close();
               out.closeEntry();
		}
		out.close();
		
		// This needs to be called before removing files.
		System.gc();
		
		// remove files
		for (File file : filesToAdd) {
			file.delete();
		}
		// remove files
		for (File file : filesToRemove) {
			file.delete();
		}
	}
}
