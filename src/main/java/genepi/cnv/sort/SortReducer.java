package genepi.cnv.sort;

import genepi.cnv.objects.ReadKey;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

import java.io.File;
import java.io.IOException;

import htsjdk.samtools.*;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.seqdoop.hadoop_bam.SAMRecordWritable;

public class SortReducer extends Reducer<ReadKey, SAMRecordWritable, Text, Text> {

	private String output;
	SAMFileHeader header;
	String reflength;
	String length;

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {

		HdfsUtil.setDefaultConfiguration(context.getConfiguration());
		
		output = context.getConfiguration().get("OUTPUT");

		header = new SAMFileHeader();
		
		length = context.getConfiguration().get("LN");

	}

	protected void reduce(ReadKey key, java.lang.Iterable<SAMRecordWritable> values,
			Context context) {

		String name = key.getSample() + "_" + key.getSequence() + ".bam";
		
		name = name.replaceAll(":", "_");
		
		if (header.getSequence(key.getSequence()) == null) {
			header.addSequence(new SAMSequenceRecord(key.getSequence(), Integer.valueOf(length)));
		}

		//S//AMLineParser parser = new SAMLineParser(header);
		SAMFileWriter bamWriter = new SAMFileWriterFactory().makeBAMWriter(
				header, true, new File(name));

		for (SAMRecordWritable value : values) {
			bamWriter.addAlignment(value.get());
		}

		bamWriter.close();

		HdfsUtil.put(name, HdfsUtil.path(output, name));
		FileUtil.deleteFile(name);

	}
}
