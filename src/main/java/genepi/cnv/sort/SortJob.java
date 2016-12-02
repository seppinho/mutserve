package genepi.cnv.sort;

import genepi.cnv.objects.ReadKey;
import genepi.cnv.objects.ReadKeyComparator;
import genepi.cnv.objects.ReadKeyGroupingComparator;
import genepi.cnv.objects.ReadKeyPartitioner;
import genepi.hadoop.HadoopJob;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

public class SortJob extends HadoopJob {

	private String statistics;

	private String outputSplits;

	private String localOutput;

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
		this.outputSplits = output;
	}
	
	public void setRefLength(String length) {
		getConfiguration().set("LN", length);
	}

	public void setLocalOutput(String localOutput) {
		this.localOutput = localOutput;
	}

	public void setStatistics(String statistics) {
		this.statistics = statistics;
	}

	public String getStatistics() {
		return statistics;
	}

}