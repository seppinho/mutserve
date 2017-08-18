package genepi.mut.sort;

import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import genepi.mut.objects.ReadKey;

import java.io.File;
import java.io.IOException;

import htsjdk.samtools.*;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class SortReducer extends Reducer<ReadKey, Text, Text, Text> {

	private String output;
	SAMFileHeader header;
	String reflength;
	String length;

	@Override
	protected void setup(Context context) throws IOException, InterruptedException {

		HdfsUtil.setDefaultConfiguration(context.getConfiguration());

		output = context.getConfiguration().get("OUTPUT");

		header = new SAMFileHeader();

		length = context.getConfiguration().get("LN");

	}

	protected void reduce(ReadKey key, java.lang.Iterable<Text> values, Context context) {

		if (header.getSequence(key.getSequence()) == null) {
			header.addSequence(new SAMSequenceRecord(key.getSequence(), Integer.valueOf(length)));
		}
		
		String name = key.getSample() + "_" + key.getSequence() + ".bam";
		
		name = name.replaceAll(":", "_");
		
		SAMFileWriter bamWriter = new SAMFileWriterFactory().makeBAMWriter(header, true, new File(name));
		
		SAMLineParser parser = new SAMLineParser(header);

		int i = 0;
		for (Text value : values) {
			i++;
			bamWriter.addAlignment(parser.parseLine(value.toString()));
		}

		bamWriter.close();

		HdfsUtil.put(name, HdfsUtil.path(output, name));
		FileUtil.deleteFile(name);

	}
}
