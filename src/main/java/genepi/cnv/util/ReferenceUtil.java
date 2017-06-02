package genepi.cnv.util;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SAMFileReader;
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

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import genepi.hadoop.HdfsUtil;

public class ReferenceUtil {
	
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
			System.out.println(reference + "not a directory");
		}
		System.out.println("path " + refPath);
		return refPath;
	}

	// for BAQ calculation only needed for mtDNA!
	public static String getValidReferenceNameForBaq(int length) {
		String alteredRef = null;

		switch (length) {
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

	public static String readInReference(String file) {
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

		return reference + ".tar.gz";

	}

	public static boolean ismtDNAHotSpot(int pos) {
		return hotSpots.contains(pos);
	}
	
	
	
	public static Reference determineReference(String input) {

		FileStatus[] fileList = null;
		FileSystem fileSystem = null;
		Reference ref = Reference.UNKNOWN;
		int rcrs = 0, hg19 = 0;

		try {

			fileSystem = FileSystem.get(HdfsUtil.getConfiguration());
			fileList = fileSystem.listStatus(new Path(input));

			for (FileStatus file : fileList) {

				FSDataInputStream in = fileSystem.open(file.getPath());

				// check for length
				SAMFileReader reader = new SAMFileReader(in);
				SAMSequenceDictionary dict = reader.getFileHeader()
						.getSequenceDictionary();

				for (SAMSequenceRecord record : dict.getSequences()) {
					if (record.getSequenceLength() == 16571) {
						ref = Reference.hg19;
						hg19++;
						in.close();
						reader.close();
					}
					if (record.getSequenceLength() == 16569) {
						ref = Reference.rcrs;
						rcrs++;
						in.close();
						reader.close();
					}
				}
				
				in.close();
				reader.close();

			}
			
			if(rcrs > 0 && hg19 > 0){
				ref = Reference.MISLEADING;
			}
			
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		return ref;
	}

}
