package genepi.vcbox.align.paired;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.seqdoop.hadoop_bam.SequencedFragment;

import genepi.vcbox.objects.SingleRead;


public class PairedAlignerMap extends
		Mapper<Text, SequencedFragment, Text, SingleRead> {

	StringBuilder builder = new StringBuilder();
	Text outKey = new Text();
	String fileID;
	FileSplit fileSplit;
	SingleRead read = new SingleRead();
	String seq;
	String qual;
	int amountReads;

	/** adding fileID; added by Seb */

	protected void setup(Context context) throws IOException,
			InterruptedException {

		FileSplit fileSplit = (FileSplit) context.getInputSplit();

		fileID = fileSplit.getPath().getName();

	}

	/** building pairs as in SEAL */
	
	public void map(Text key, SequencedFragment value, Context context)
			throws IOException, InterruptedException {

		// reset builder
		builder.delete(0, builder.length());
		seq = value.getSequence().toString();
		qual = value.getQuality().toString();


		/**
		 * FASTQ format with /1 and /2 at the end
		 * 
		 * @SRR062634.1 HWI-EAS110_103327062:6:1:1092:8469/1
		 */
		if (key.toString().charAt(key.getLength() - 2) == '/') {

			outKey.set(Text.decode(key.getBytes(), 0, key.getLength() - 1));

		}

		/**
		 * new FASTQ file format
		 * 
		 * @HWI-ST301L:236:C0EJ5ACXX:1:1101:1436:2180 1:N:0:ATCACG
		 */
		else {

			builder = generateFastqKey(builder, value);
			outKey.set(builder.toString());

		}

		read.setReadLength(seq.length());
		read.setName(outKey.toString());
		read.setBases(seq.getBytes());
		read.setQual(qual.getBytes());
		read.setFilename(fileID);
		read.setReadNumber(value.getRead());
		context.write(outKey, read);

	}

	/** SEAL preprocessing */
	protected StringBuilder generateFastqKey(StringBuilder builder,
			SequencedFragment read) {

		builder.append(read.getInstrument() == null ? "" : read.getInstrument());
		builder.append(":").append(
				read.getRunNumber() == null ? "" : read.getRunNumber());
		builder.append(":").append(
				read.getFlowcellId() == null ? "" : read.getFlowcellId());
		builder.append(":").append(read.getLane());
		builder.append(":").append(read.getTile());
		builder.append(":").append(read.getXpos());
		builder.append(":").append(read.getYpos());

		//builder.append("#")
		//		.append(read.getIndexSequence() == null ? '0' : read
		//				.getIndexSequence());

		return builder;

	}
}
