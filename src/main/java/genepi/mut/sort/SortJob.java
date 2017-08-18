package genepi.mut.sort;

import genepi.hadoop.HadoopJob;
import genepi.mut.objects.ReadKey;
import genepi.mut.objects.ReadKeyComparator;
import genepi.mut.objects.ReadKeyGroupingComparator;
import genepi.mut.objects.ReadKeyPartitioner;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.seqdoop.hadoop_bam.SAMRecordWritable;

public class SortJob extends HadoopJob {

	public SortJob(String name) {

		super(name);
		set("mapred.map.tasks.speculative.execution", false);
		set("mapred.reduce.tasks.speculative.execution", false);
	}

	@Override
	public void setupJob(Job job) {

		job.setJarByClass(SortJob.class);
		job.setInputFormatClass(TextInputFormat.class);

		job.setMapperClass(SortMap.class);
		job.setReducerClass(SortReducer.class);

		job.setMapOutputKeyClass(ReadKey.class);
		job.setMapOutputValueClass(Text.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setSortComparatorClass(ReadKeyComparator.class);
		job.setPartitionerClass(ReadKeyPartitioner.class);
		job.setGroupingComparatorClass(ReadKeyGroupingComparator.class);

	}

	@Override
	public void setOutput(String output) {
		getConfiguration().set("OUTPUT", output);
		super.setOutput(output + "-temp");
	}
	
	public void setRefLength(String length) {
		getConfiguration().set("LN", length);
	}
}