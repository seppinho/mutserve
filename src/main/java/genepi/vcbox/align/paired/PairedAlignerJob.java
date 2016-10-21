package genepi.vcbox.align.paired;

import genepi.hadoop.CacheStore;
import genepi.hadoop.HadoopJob;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import genepi.vcbox.objects.SingleRead;
import genepi.vcbox.util.Helper;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.seqdoop.hadoop_bam.FastqInputFormat;

public class PairedAlignerJob extends HadoopJob {

	protected static final Log log = LogFactory.getLog(PairedAlignerJob.class);
	private String reference;
	private String folder;

	public PairedAlignerJob(String name) {

		super(name);
		set("mapred.map.tasks.speculative.execution", false);
		set("mapred.reduce.tasks.speculative.execution", false);
		
	}

	@Override
	public void setupJob(Job job) {

		job.setJarByClass(PairedAlignerJob.class);
		job.setInputFormatClass(FastqInputFormat.class);
		job.setMapperClass(PairedAlignerMap.class);
		job.setReducerClass(PairedAlignerReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(SingleRead.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

	}

	@Override
	protected void setupDistributedCache(CacheStore cache) {

		// distribute jbwa libraries
		String hdfsPath = HdfsUtil.path("jbwa-data", "jbwa.tar.gz");
		if (!HdfsUtil.exists(hdfsPath)) {
			String jbwa = FileUtil.path(folder,"jbwa.tar.gz");
			HdfsUtil.put(jbwa, hdfsPath);
		}
		
		String archive = FileUtil.path(folder,Helper.getSelectedReferenceArchive(reference));
		String hdfsPathRef = HdfsUtil.path("jbwa-data", archive);
		
		if (!HdfsUtil.exists(hdfsPathRef)) {
			HdfsUtil.put(archive, hdfsPathRef);
		}
		
		log.info("Archive path is: "+hdfsPath);
		log.info("Reference path is: "+hdfsPathRef);
		
		cache.addArchive("jbwaLib", hdfsPath);
		cache.addArchive("reference", hdfsPathRef);

	}

	@Override
	public void setOutput(String output) {

		super.setOutput(output);
	}


	public void setReference(String reference) {
		this.reference = reference;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

}