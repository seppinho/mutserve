package genepi.mut.pileup;

import java.io.File;
import java.io.IOException;

import genepi.mut.objects.BasePosition;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;

public class ReadBamTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final SamReader reader = SamReaderFactory.makeDefault()
				.open(new File("test-data/mtdna/bam/input/HG00096.mapped.ILLUMINA.bwa.GBR.low_coverage.20101123.bam"));

		SAMRecordIterator d = reader.iterator();

		while (d.hasNext()) {

			SAMRecord samRecord = d.next();

			if (samRecord.getReadName().equals("SRR062635.20477607")) {

				System.out.println(samRecord.getSAMString());

				Integer currentReferencePos = samRecord.getAlignmentStart();

				int currentPos = 0;

				for (CigarElement cigarElement : samRecord.getCigar().getCigarElements()) {

					Integer cigarElementLength = cigarElement.getLength();

					if (cigarElement.getOperator() == CigarOperator.D) {
						
						Integer cigarElementStart = currentReferencePos;

						int i = 0;
						while (i < cigarElementLength) {
							System.out.println("D " + cigarElementStart);
							i++;
						}

						//to get correct position for insertions
						if (currentPos > 0) {
							currentPos = currentPos - cigarElement.getLength();
						}
					}

					if (cigarElement.getOperator() == CigarOperator.I) {

						int i = 0;
						while (i < cigarElementLength) {
							char insBase = samRecord.getReadString().charAt(currentPos + i);
							byte quality = samRecord.getBaseQualities()[currentPos + i];
							i++;
						}

						currentPos = currentPos + cigarElement.getLength();

					}

					if (cigarElement.getOperator().consumesReferenceBases()
							|| cigarElement.getOperator() == CigarOperator.SOFT_CLIP) {
						currentReferencePos = currentReferencePos + cigarElement.getLength();
						currentPos = currentPos + cigarElement.getLength();
					}

				}

				System.exit(0);
			}

		}

		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
