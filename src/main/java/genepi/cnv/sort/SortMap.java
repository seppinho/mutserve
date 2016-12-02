package genepi.cnv.sort;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMLineParser;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceRecord;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import genepi.cnv.objects.ReadKey;

public class SortMap extends Mapper<Object, Text, ReadKey, Text> {

	private ReadKey outKey = new ReadKey();

	private SAMLineParser parser = null;

	private SAMFileHeader header;

	private Text text;

	// SAMRecordWritable sam = new SAMRecordWritable();

	// private SAMRecordWritable out = new SAMRecordWritable();

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		header = new SAMFileHeader();
		parser = new SAMLineParser(header);
		text = new Text();
		// sam = new SAMRecordWritable();

	}

	public void map(Object key, Text value, Context context)
			throws IOException, InterruptedException {

		if (!value.toString().trim().equals("")) {
			String tilesValue[] = value.toString().split("\t", 2);
			String sample = tilesValue[0].replaceAll(".fastq", "").replaceAll(
					".fq", "");

			String[] tiles = tilesValue[1].split("\t");
			String contig = tiles[2].trim();

			if (header.getSequence(contig) == null) {
				header.addSequence(new SAMSequenceRecord(contig, 16569));
			}

			SAMRecord samRecord = parser.parseLine(tilesValue[1]);

			// only add it if its mapped
			if (!samRecord.getReadUnmappedFlag()) {

				// }

				outKey.setSample(sample);
				outKey.setPosition(samRecord.getAlignmentStart());
				outKey.setSequence(contig);
				outKey.setReadName(samRecord.getReadName());
				text.set(tilesValue[1]);
				context.write(outKey, text);

				// sam.set(samRecord);
				// context.write(outKey, sam);
				// generateCounters(context, samRecord);

			}
		} else {
			System.out.println("text is null ");
		}

	}

	private void generateCounters(Context context, SAMRecord recFromText) {
		context.getCounter("BAM_STATS", outKey.getSample() + "\tREADS")
				.increment(1);

		if (recFromText.isValid() == null) {
			context.getCounter("BAM_STATS", outKey.getSample() + "\tVALID")
					.increment(1);
		}

		if (recFromText.getReadUnmappedFlag()) {
			context.getCounter("BAM_STATS", outKey.getSample() + "\tUNMAPPED")
					.increment(1);
		} else {
			context.getCounter("BAM_STATS", outKey.getSample() + "\tMAPPED")
					.increment(1);
		}

		if (recFromText.getReadPairedFlag()) {
			if (recFromText.getProperPairFlag()) {
				context.getCounter("BAM_STATS",
						outKey.getSample() + "\tPROPER_PAIR").increment(1);
			}
		}

		if (recFromText.getReadFailsVendorQualityCheckFlag()) {
			context.getCounter("BAM_STATS",
					outKey.getSample() + "\tQC_FAILURE_READS").increment(1);
		}

		if (recFromText.getMappingQuality() < 30) {
			context.getCounter("BAM_STATS", outKey.getSample() + "\tLQ30")
					.increment(1);
		} else {
			context.getCounter("BAM_STATS", outKey.getSample() + "\tHQ30")
					.increment(1);
		}
	}

}
