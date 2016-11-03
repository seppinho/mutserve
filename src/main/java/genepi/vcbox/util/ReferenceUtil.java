package genepi.vcbox.util;

import htsjdk.samtools.SAMRecord.SAMTagAndValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReferenceUtil {
	
	private static Set<Integer> hotSpots = new HashSet<Integer>(Arrays.asList(302, 303,
			304, 305, 306, 307, 308, 309, 310, 311, 315, 316, 3105, 3106,
			3107));

	public static String findFileinReferenceArchive(File reference, String suffix) {
		String refPath = null;
		if (reference.isDirectory()) {
			File[] files = reference.listFiles();
			for (File i : files) {
				if (i.getName().endsWith(suffix)) {

					refPath = i.getAbsolutePath();
				}
			}
		}
		System.out.println("path " + refPath);
		return refPath;
	}
	
	// for BAQ calculation only needed for mtDNA!
	public static String getValidReferenceNameForBaq (int length){
		String alteredRef = null;
		
		switch(length)
		{
			case 16569:
				alteredRef = "rCRS";
			   break; 
			case 16571:
				alteredRef = "gi|17981852|ref|NC_001807.4|";
		}
		
		return alteredRef;
	}

	public static int getTagFromSamRecord(List<SAMTagAndValue> attList, String att) {
		int value = 30;
		for (SAMTagAndValue member : attList) {
			if (member.tag.equals(att))
				value = (int) member.value;
		}
		return value;
	}
	
	public static String readInReference(String file)  {
		StringBuilder stringBuilder = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			stringBuilder = new StringBuilder();
			
			while ((line = reader.readLine()) != null) {

				if (!line.startsWith(">"))
					stringBuilder.append(line);

			}
			
			reader.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	

		return stringBuilder.toString();
	}
	
	public static String getSelectedReferenceArchive(String reference) {
		
		return reference+".tar.gz";
		
	}
	
	public static boolean ismtDNAHotSpot(int pos) {
		return hotSpots.contains(pos);
	}
	
}
