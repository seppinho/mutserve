package genepi.vcbox.sort;

import genepi.base.Timer;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileHeader.SortOrder;
import htsjdk.samtools.SAMFileReader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

public class BamUtils {

	public static void mergeBam(List<InputStream> streams, String output)
			throws IOException {

		System.out.println("Merging Header....");

		// merge headers
		SAMFileHeader header = new SAMFileHeader();
		header.setSortOrder(SortOrder.coordinate);

		if (header.getSequence("gi|251831106|ref|NC_012920.1|") == null) {
			header.addSequence(new SAMSequenceRecord("gi|251831106|ref|NC_012920.1|", 16569));
		}

		System.out.println("Merging Bam Files....");

		// write merged bam file
		SAMFileWriter outputSam = new SAMFileWriterFactory()
				.makeSAMOrBAMWriter(header, true, new File(output));

		List<SAMRecord> duplicates = new Vector<SAMRecord>();

		int mark = 0;
		int count = 0;

		for (InputStream stream : streams) {
			SAMFileReader inputSam = new SAMFileReader(stream);

			duplicates.clear();

			for (SAMRecord samRecord : inputSam) {
				// System.out.println(samRecord.getReferenceName());
				samRecord.setHeader(header);
				if (!samRecord.getReferenceName().equals("*")) {
					// System.out.println(samRecord.getReferenceName()+":"+samRecord.getAlignmentStart());
					
					int index = header.getSequenceIndex(samRecord
							.getReferenceName());
					samRecord.setReferenceIndex(index);
					int indexMate = header.getSequenceIndex(samRecord
							.getMateReferenceName());
					samRecord.setMateReferenceIndex(indexMate);

					count++;

					// dedup
					if (isDuplicate(duplicates, samRecord)) {
						duplicates.add(samRecord);
					} else {

						if (duplicates.size() > 1) {
							SAMRecord bestMapping = findBestMapping(duplicates);
							for (SAMRecord record : duplicates) {
								if (record != bestMapping) {
									record.setDuplicateReadFlag(true);
									mark++;
								} else {
									record.setDuplicateReadFlag(false);
								}
							}
						}

						for (SAMRecord record : duplicates) {
							outputSam.addAlignment(record);
						}

						duplicates.clear();
						duplicates.add(samRecord);
					}

				}
			}

			if (duplicates.size() > 1) {
				SAMRecord bestMapping = findBestMapping(duplicates);
				for (SAMRecord record : duplicates) {
					if (record != bestMapping) {
						record.setDuplicateReadFlag(true);
					} else {
						record.setDuplicateReadFlag(false);
					}
				}
			}

			for (SAMRecord record : duplicates) {
				outputSam.addAlignment(record);
			}
			inputSam.close();
		}

		System.out.println("As dup marked: " + mark / (float) count + " ("
				+ mark + " duplicates)");

		outputSam.close();

	}

	private static boolean isDuplicate(List<SAMRecord> duplicates, SAMRecord b) {

		if (duplicates.isEmpty()) {
			return false;
		} else {

			SAMRecord a = duplicates.get(0);

			if (a.getReadPairedFlag() && b.getReadPairedFlag() && !b.getMateUnmappedFlag() && !a.getMateUnmappedFlag()
					&& !a.getMateUnmappedFlag() && !b.getMateUnmappedFlag()) {

				return (a.getReferenceName().equals(b.getReferenceName()))
						&& (a.getUnclippedStart() == b.getUnclippedStart())
						&& (a.getCigarString().equals(b.getCigarString()))
						&& (a.getReadNegativeStrandFlag() == b
								.getReadNegativeStrandFlag());
			} else {
				return false;
			}

		}
	}

	private static SAMRecord findBestMapping(List<SAMRecord> duplicates) {
		int bestMapping = 0;

		for (int j = 1; j < duplicates.size(); j++) {
			if (duplicates.get(j).getMappingQuality() > duplicates.get(
					bestMapping).getMappingQuality()) {
				bestMapping = j;
			}
		}

		return duplicates.get(bestMapping);
	}

	public static void main(String[] args) throws IOException {

		Timer timer = new Timer();

		timer.start();
		String output = "output.bam";


		List<InputStream> streams = new Vector<InputStream>();
			streams.add(new FileInputStream("test2.bam"));

		BamUtils.mergeBam(streams, output);
		timer.stop();

		timer.println("merging " + streams.size() + " bam files");
	}

}
