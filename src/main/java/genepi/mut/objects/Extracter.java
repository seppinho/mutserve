package genepi.mut.objects;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

public class Extracter {

	final static int BUFFER = 2048;

	public static void extract(String input, String output)  {
		/** create a TarArchiveInputStream object. **/

		FileInputStream fin;
		try {
			fin = new FileInputStream(input);
			BufferedInputStream in = new BufferedInputStream(fin);
			GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
			TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn);

			TarArchiveEntry entry = null;

			/** Read the tar entries using the getNextEntry method **/

			while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {

				System.out.println("Extracting: " + entry.getName());

				/** If the entry is a directory, create the directory. **/

				if (entry.isDirectory()) {

					File f = new File(output + entry.getName());
					f.mkdirs();
				}
				/**
				 * If the entry is a file,write the decompressed file to the disk
				 * and close destination stream.
				 **/
				else {
					int count;
					byte data[] = new byte[BUFFER];

					FileOutputStream fos = new FileOutputStream(output + entry.getName());
					BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
					while ((count = tarIn.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, count);
					}
					dest.close();
				}
				
				
			}
			tarIn.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	

		/** Close the input stream **/

		System.out.println("untar completed successfully!!");

	}
}
