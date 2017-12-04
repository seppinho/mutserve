package genepi.mut.util;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class ReferenceUtilHdfs {
	
	public enum Reference { hg19, rcrs, UNKNOWN, MISLEADING};

	private static Set<Integer> hotSpots = new HashSet<Integer>(
			Arrays.asList(302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 315, 316, 3105, 3106, 3107));

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
