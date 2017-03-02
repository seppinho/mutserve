package genepi.cnv.sort;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMLineParser;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceRecord;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.seqdoop.hadoop_bam.SAMRecordWritable;

import genepi.cnv.objects.ReadKey;
import genepi.hadoop.HdfsUtil;

public class SortMap extends Mapper<Object, Text, ReadKey, SAMRecordWritable> {

	private ReadKey outKey = new ReadKey();

	private SAMLineParser parser = null;

	private SAMFileHeader header;

	SAMRecordWritable samRecordWritable = new SAMRecordWritable();

	private String length;


	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		header = new SAMFileHeader();
		parser = new SAMLineParser(header);
		//text = new Text();
		length = context.getConfiguration().get("LN");
		

		HdfsUtil.setDefaultConfiguration(context.getConfiguration());

	}

	public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

		if (!value.toString().trim().equals("")) {
			String tilesValue[] = value.toString().split("\t", 2);
			String sample = tilesValue[0].replaceAll(".fastq", "").replaceAll(".fq", "");

			String[] tiles = tilesValue[1].split("\t");
			String contig = tiles[2].trim();

			if (header.getSequence(contig) == null) {
				header.addSequence(new SAMSequenceRecord(contig, Integer.valueOf(length)));
			}

			SAMRecord samRecord = parser.parseLine(tilesValue[1]);

			// only add it if its mapped
			if (!samRecord.getReadUnmappedFlag()) {

				outKey.setSample(sample);
				outKey.setPosition(samRecord.getAlignmentStart());
				outKey.setSequence(contig);
				outKey.setReadName(samRecord.getReadName());

				samRecordWritable.set(samRecord);
				context.write(outKey, samRecordWritable);

			}
		}

	}

}
