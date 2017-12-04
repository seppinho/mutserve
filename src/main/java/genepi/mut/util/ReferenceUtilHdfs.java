package genepi.mut.util;

import java.io.File;


public class ReferenceUtilHdfs {
	
	public enum Reference { hg19, rcrs, UNKNOWN, MISLEADING};

	public static String findFileinDir(File reference, String suffix) {
		String refPath = null;
		if (reference.isDirectory()) {
			File[] files = reference.listFiles();
			for (File i : files) {
				if (i.getName().endsWith(suffix)) {
					refPath = i.getAbsolutePath();
				}
			}
		} else{
			System.out.println(reference + " not a directory");
		}
		System.out.println("path " + refPath);
		return refPath;
	}

}
