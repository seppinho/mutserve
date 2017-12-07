package genepi.mut.align.paired;

import genepi.hadoop.CacheStore;
import genepi.hadoop.HadoopJob;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;
import genepi.mut.Server;
import genepi.mut.align.AlignStep;
import genepi.mut.objects.SingleRead;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.seqdoop.hadoop_bam.FastqInputFormat;

public class PairedAlignerJob extends HadoopJob {

	protected static final Log log = LogFactory.getLog(PairedAlignerJob.class);
	private String refArchive;
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
		String hdfsPath = HdfsUtil.path(AlignStep.REF_DIRECTORY, "jbwa-native.tar");
		if (!HdfsUtil.exists(hdfsPath)) {
			String jbwa = FileUtil.path(folder,"jbwa-native.tar");
			HdfsUtil.put(jbwa, hdfsPath);
		}
		
		String hdfsPathRef = HdfsUtil.path(AlignStep.REF_DIRECTORY,refArchive.substring(refArchive.lastIndexOf("/")+1));
		
		if (!HdfsUtil.exists(hdfsPathRef)) {
			HdfsUtil.put(refArchive, hdfsPathRef);
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


	public void setReferenceArchive(String refArchive) {
		this.refArchive = refArchive;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public void setChunkLength(String chunkLength) {
		set("chunkLength", chunkLength);
		
	}

}