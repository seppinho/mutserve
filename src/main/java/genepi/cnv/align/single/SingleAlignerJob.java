package genepi.cnv.align.single;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.seqdoop.hadoop_bam.FastqInputFormat;

import genepi.cnv.objects.BasePosition;
import genepi.cnv.util.ReferenceUtil;
import genepi.hadoop.CacheStore;
import genepi.hadoop.HadoopJob;
import genepi.hadoop.HdfsUtil;
import genepi.io.FileUtil;

public class SingleAlignerJob extends HadoopJob {
	
	protected static final Log log = LogFactory.getLog(SingleAlignerJob.class);
	private String reference;
	private String folder;
	
	public SingleAlignerJob(String name) {

		super(name);
		set("mapred.map.tasks.speculative.execution", false);
		set("mapred.reduce.tasks.speculative.execution", false);
	}

	@Override
	public void setupJob(Job job) {

		job.setJarByClass(SingleAlignerJob.class);
		job.setInputFormatClass(FastqInputFormat.class);
		job.setMapperClass(SingleAlignerMap.class);
		job.setNumReduceTasks(0);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(BasePosition.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		job.setOutputFormatClass(TextOutputFormat.class);

	}
	
	@Override
	protected void setupDistributedCache(CacheStore cache) {

		// distribute jbwa libraries
		String hdfsPath = HdfsUtil.path("jbwa-data", "jbwa.tar.gz");
		if (!HdfsUtil.exists(hdfsPath)) {
			String jbwa = FileUtil.path(folder,"jbwa.tar.gz");
			HdfsUtil.put(jbwa, hdfsPath);
		}
		
		String archive = FileUtil.path(folder,ReferenceUtil.getSelectedReferenceArchive(reference));
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

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}
	
	public void setFolder(String folder) {
		this.folder = folder;
	}
	
	
}