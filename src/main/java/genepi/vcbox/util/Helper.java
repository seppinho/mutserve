package genepi.vcbox.util;

import htsjdk.samtools.SAMRecord.SAMTagAndValue;

import java.io.File;
import java.util.List;

public class Helper {

	public static String findFileinReferenceArchive(File reference, String suffix) {
		String refPath = null;
		System.out.println(reference);
		System.out.println(suffix);
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
	
	// for BAQ calculation to get a correct reference
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
	
	public static String getSelectedReferenceArchive(String reference) {
		String archive;
		switch(reference)
		{
			case "rcrs": 
				archive = "rcrs.tar.gz";
			   break; 
			case "rsrs": 
				archive = "rsrs.tar.gz";
			   break; 
			case "hg19": 
				archive = "hg19.tar.gz";
			   break; 
			case "kiv2_6": 
				archive = "kiv2_6.tar.gz";
			   break; 
			default:
				archive = "notfound.tar.gz";
		}
		return archive;
	}
}
