package genepi.vcbox.sort;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import genepi.vcbox.objects.ReadKey;

import java.io.File;
import java.io.IOException;

import htsjdk.samtools.*;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class SortReducer extends Reducer<ReadKey, Text, Text, Text> {

	private String output;
	SAMFileHeader header;

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {

		output = context.getConfiguration().get("OUTPUT");

		header = new SAMFileHeader();

	}

	protected void reduce(ReadKey key, java.lang.Iterable<Text> values,
			Context context) {

		String name = key.getSample() + "_" + key.getSequence() + ".bam";

		if (header.getSequence(key.getSequence()) == null) {
			header.addSequence(new SAMSequenceRecord(key.getSequence(), 16569));
		}

		SAMLineParser parser = new SAMLineParser(header);
		SAMFileWriter bamWriter = new SAMFileWriterFactory().makeBAMWriter(
				header, true, new File(name));

		for (Text value : values) {
			SAMRecord recFromText = parser.parseLine(value.toString());
			bamWriter.addAlignment(recFromText);
		}

		bamWriter.close();

		HdfsUtil.put(name, HdfsUtil.path(output, name));
		FileUtil.deleteFile(name);

	}
}
