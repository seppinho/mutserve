package genepi.mut.pileup;

import java.io.File;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.CounterGroup;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.seqdoop.hadoop_bam.AnySAMInputFormat;

import genepi.hadoop.CacheStore;
import genepi.hadoop.HadoopJob;
import genepi.hadoop.HdfsUtil;
import genepi.mut.align.AlignStep;
import genepi.mut.objects.BasePositionHadoop;

public class PileupJob extends HadoopJob {

	private String refArchive;
	private String folder;
	private String pathRawLocal;
	private String pathVariantsHDFS;
	private String pathVariantsLocal;
	private long overall;
	private long goodQual;
	private long goodMapping;
	private long badQual;
	private long badALigment;
	private long shortRead;
	private long dupl;
	private long unmapped;
	private long badMapping;
	private long wrongRef;
	private long filtered;
	private long unfiltered;
	private long fwdRead, revRead;
	
	public PileupJob(String name) {
		super(name);
		set("mapred.map.tasks.speculative.execution", false);
		set("mapred.reduce.tasks.speculative.execution", false);
		set("mapreduce.map.java.opts", "-Xmx4000M");
		set("mapred.child.java.opts", "-Xmx4000M");
		//TODO check effect on reads. this happens when circular reads are split
		set("hadoopbam.samheaderreader.validation-stringency", "LENIENT");
	}

	@Override
	public void setupJob(Job job) {
		job.setInputFormatClass(AnySAMInputFormat.class);
		job.setMapperClass(PileupMapper.class);
		job.setCombinerClass(PileupCombiner.class);
		job.setReducerClass(PileupReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(BasePositionHadoop.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setOutputFormatClass(TextOutputFormat.class);
	}

	@Override
	protected void setupDistributedCache(CacheStore cache) {

		String hdfsPathRef = HdfsUtil.path(AlignStep.REF_DIRECTORY, refArchive.substring(refArchive.lastIndexOf("/") + 1));

		if (!HdfsUtil.exists(hdfsPathRef)) {
			HdfsUtil.put(refArchive, hdfsPathRef);
		}

		log.info("Reference path is: " + hdfsPathRef);
		cache.addArchive("reference", hdfsPathRef);
	}

	@Override
	public void cleanupJob(Job job) {

		try {
			// write raw file
			HdfsUtil.mergeFolderBinary(pathRawLocal, super.getOutput(), BamAnalyser.headerRaw);

			// write variants file
			HdfsUtil.mergeFolderBinary(pathVariantsLocal, pathVariantsHDFS, BamAnalyser.headerVariants);

			CounterGroup counters = job.getCounters().getGroup("mtdna");
			overall = counters.findCounter("OVERALL-READS").getValue();
			goodQual = counters.findCounter("GOOD-QUAL").getValue();
			badQual = counters.findCounter("BAD-QUAL").getValue();
			badALigment = counters.findCounter("BAD-ALIGNMENT").getValue();
			shortRead = counters.findCounter("SHORT-READ").getValue();
			dupl = counters.findCounter("DUPLICATE").getValue();
			unmapped = counters.findCounter("UNMAPPED").getValue();
			badMapping = counters.findCounter("BAD-MAPPING").getValue();
			wrongRef = counters.findCounter("WRONG-REF").getValue();
			goodMapping = counters.findCounter("GOOD-MAPPING").getValue();
			filtered = counters.findCounter("FILTERED").getValue();
			unfiltered = counters.findCounter("UNFILTERED").getValue();
			fwdRead = counters.findCounter("FWD-READ").getValue();
			revRead = counters.findCounter("REV-READ").getValue();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void setOutput(String output) {

		super.setOutput(output);
	}

	public void setRawLocal(String localPath) {

		this.pathRawLocal = localPath;
	}

	public void setVariantsPathHdfs(String hdfsPath) {

		set("variantsHdfs", hdfsPath);
		this.pathVariantsHDFS = hdfsPath;
	}

	public void setVariantsPathLocal(String localPath) {

		this.pathVariantsLocal = localPath;
	}

	public void setMappingQuality(String mapQual) {
		set("mapQual", mapQual);
	}

	public void setBaseQuality(String baseQual) {
		set("baseQual", baseQual);
	}

	public void setAlignmentQuality(String alignQual) {
		set("alignQual", alignQual);
	}

	public void setBAQ(boolean baq) {
		set("baq", baq);
	}
	
	public void setLevel(String level) {
		set("level", level);
	}

	public void setCallDel(boolean callDel) {
		set("callDel", callDel);
	}

	public void setArchive(String refArchive) {
		this.refArchive = refArchive;
	}

	public long getGoodQual() {
		return goodQual;
	}

	public long getBadQual() {
		return badQual;
	}

	public long getBadALigment() {
		return badALigment;
	}

	public void setBadALigment(long badALigment) {
		this.badALigment = badALigment;
	}

	public long getShortRead() {
		return shortRead;
	}

	public long getFiltered() {
		return filtered;
	}

	public void setFiltered(long filtered) {
		this.filtered = filtered;
	}

	public long getUnfiltered() {
		return unfiltered;
	}

	public void setUnfiltered(long unfiltered) {
		this.unfiltered = unfiltered;
	}

	public void setShortRead(long shortRead) {
		this.shortRead = shortRead;
	}

	public long getDupl() {
		return dupl;
	}

	public void setDupl(long dupl) {
		this.dupl = dupl;
	}

	public long getUnmapped() {
		return unmapped;
	}

	public void setUnmapped(long unmapped) {
		this.unmapped = unmapped;
	}

	public long getBadMapping() {
		return badMapping;
	}

	public void setBadMapping(long badMapping) {
		this.badMapping = badMapping;
	}

	public long getWrongRef() {
		return wrongRef;
	}

	public void setWrongRef(long wrongRef) {
		this.wrongRef = wrongRef;
	}

	public void setGoodQual(long goodQual) {
		this.goodQual = goodQual;
	}

	public void setBadQual(long badQual) {
		this.badQual = badQual;
	}

	public long getOverall() {
		return overall;
	}

	public void setOverall(long overall) {
		this.overall = overall;
	}

	public void setGoodMapping(long goodMapping) {
		this.goodMapping = goodMapping;
	}

	private String getFolder(Class<PileupJob> clazz) {
		return new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
	}

	public long getFwdRead() {
		return fwdRead;
	}

	public void setFwdRead(long fwdRead) {
		this.fwdRead = fwdRead;
	}

	public long getRevRead() {
		return revRead;
	}

	public void setRevRead(long revRead) {
		this.revRead = revRead;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

}