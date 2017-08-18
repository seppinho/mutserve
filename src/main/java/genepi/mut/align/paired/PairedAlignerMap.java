package genepi.mut.align.paired;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.seqdoop.hadoop_bam.SequencedFragment;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import genepi.mut.objects.SingleRead;

public class PairedAlignerMap extends Mapper<Text, SequencedFragment, Text, SingleRead> {

	String fileID;
	int chunkLength;
	// String seq;
	// String qual;

	/** adding fileID; added by Seb */

	protected void setup(Context context) throws IOException, InterruptedException {

		FileSplit fileSplit = (FileSplit) context.getInputSplit();

		fileID = fileSplit.getPath().getName();
		
		chunkLength = context.getConfiguration().getInt("chunkLength", 0);

	}

	/** building pairs as in SEAL */

	public void map(Text key, SequencedFragment value, Context context) throws IOException, InterruptedException {

		// reset builder
		String seq = value.getSequence().toString();
		String qual = value.getQuality().toString();
		int readNumber = value.getRead();

		Text outKey = new Text();

		if(chunkLength == 0){
			chunkLength = seq.length();
		}
		
		Iterable<String> seqs = Splitter.fixedLength(chunkLength).split(seq);
		Iterable<String> quals = Splitter.fixedLength(chunkLength).split(qual);

		String[] qualArray = Iterables.toArray(quals, String.class);

		int unique = 0;

		for (String subSeq : seqs) {

			SingleRead read = new SingleRead();

			// FASTQ format with /1 and /2 at the end
			// @SRR062634.1 HWI-EAS110_103327062:6:1:1092:8469/1

			// new FASTQ file format
			// @HWI-ST301L:236:C0EJ5ACXX:1:1101:1436:2180 1:N:0:ATCACG

			if (key.toString().charAt(key.getLength() - 2) == '/') {

				outKey.set(Text.decode(key.getBytes(), 0, key.getLength() - 1) + "_" + unique);

			} else {

				String _key = generateFastqKey(value, unique);
				outKey.set(_key);

			}

			read.setReadLength(subSeq.length());
			read.setName(outKey.toString());
			read.setBases(subSeq.getBytes());
			read.setQual(qualArray[unique].getBytes());
			read.setFilename(fileID);
			read.setReadNumber(readNumber);
			context.write(outKey, read);

			unique++;

		}
	}

	/** SEAL preprocessing */
	protected String generateFastqKey(SequencedFragment read, int unique) {

		StringBuilder builder = new StringBuilder();

		builder.append(read.getInstrument() == null ? "" : read.getInstrument());
		builder.append(":").append(read.getRunNumber() == null ? "" : read.getRunNumber());
		builder.append(":").append(read.getFlowcellId() == null ? "" : read.getFlowcellId());
		builder.append(":").append(read.getLane());
		builder.append(":").append(read.getTile());
		builder.append(":").append(read.getXpos());
		builder.append(":").append(read.getYpos());
		builder.append(":").append(unique);

		// builder.append("#")
		// .append(read.getIndexSequence() == null ? '0' : read
		// .getIndexSequence());

		return builder.toString();

	}
}
