package genepi.mut.tools;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

import genepi.mut.App;
import genepi.mut.objects.BayesFrequencies;
import genepi.mut.util.FastaWriter;
import genepi.mut.util.VcfWriter;
import genepi.mut.vc.MergeTask;
import genepi.mut.vc.VariantCallingTask;
import htsjdk.samtools.util.StopWatch;
import lukfor.progress.TaskService;
import lukfor.progress.tasks.Task;
import lukfor.progress.tasks.TaskFailureStrategy;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = App.APP, version = App.VERSION)
public class VariantCallingCommand implements Callable<Integer> {

	@Parameters(description = "BAM/CRAM files")
	List<String> input;

	@Option(names = { "--output" }, description = "Output filename", required = true)
	String output;

	@Option(names = { "--reference" }, description = "Reference", required = true)
	String reference;

	@Option(names = { "--threads" }, description = "Number of threads", required = false)
	int threads = 1;

	@Option(names = { "--level" }, description = "Output filename", required = false)
	double level = 0.01;

	@Option(names = {
			"--baseQ" }, description = "Minimum Base Quality", required = false, showDefaultValue = Visibility.ALWAYS)
	int baseQ = 20;

	@Option(names = {
			"--mapQ" }, description = "Minimum Map Quality", required = false, showDefaultValue = Visibility.ALWAYS)
	int mapQ = 20;

	@Option(names = {
			"--alignQ" }, description = "Minimum Align Quality", required = false, showDefaultValue = Visibility.ALWAYS)
	int alignQ = 30;

	@Option(names = { "--no-baq" }, description = "Disable BAQ", required = false, showDefaultValue = Visibility.ALWAYS)
	boolean baq = true;

	@Option(names = {
			"--no-freq" }, description = "Use Frequency File", required = false, showDefaultValue = Visibility.ALWAYS)
	boolean freq = true;

	@Option(names = {
			"--deletions" }, description = "Call deletions (beta)", required = false, showDefaultValue = Visibility.ALWAYS)
	boolean deletions = false;

	@Option(names = {
			"--insertions" }, description = "Call insertions (beta)", required = false, showDefaultValue = Visibility.ALWAYS)
	boolean insertions = false;

	@Option(names = {
			"--write-raw" }, description = "Write raw file", required = false, showDefaultValue = Visibility.ALWAYS)
	boolean writeRaw = false;

	@Option(names = {
			"--write-fasta" }, description = "Write fasta file", required = false, showDefaultValue = Visibility.ALWAYS)
	boolean writeFasta = false;

	@Option(names = {
			"--contig-name" }, description = "Specifify mtDNA contig name", required = false, showDefaultValue = Visibility.ALWAYS)
	String contig;
	
	@Option(names = {
	"--mode" }, description = "Specifify mutserve mode", required = false, showDefaultValue = Visibility.ALWAYS)
	String mode = "mtdna";

	@Option(names = {
			"--no-ansi" }, description = "Disable ANSI support", required = false, showDefaultValue = Visibility.ALWAYS)
	boolean noAnsi = true;
	
	@Option(names = { "--version" }, versionHelp = true)
	boolean showVersion;
	
	@Option(names = { "--help" }, usageHelp = true)
	boolean showHelp;

	@Override
	public Integer call() {

		System.out.println("Parameters:");
		System.out.println("Input: " + input);
		System.out.println("Output: " + output);
		System.out.println("Detection limit: " + level);
		System.out.println("Base Quality: " + baseQ);
		System.out.println("Map Quality: " + mapQ);
		System.out.println("Alignment Quality: " + alignQ);
		System.out.println("BAQ: " + baq);
		System.out.println("1000G Frequency File: " + freq);
		System.out.println("Deletions: " + deletions);
		System.out.println("Insertions: " + insertions);
		System.out.println("Fasta: " + writeFasta);
		System.out.println("");
		
		HashMap<String, Double> freqFile = null;

		if (freq) {
			InputStream in = this.getClass().getClassLoader().getResourceAsStream("1000g.frq");
			freqFile = BayesFrequencies.instance(new DataInputStream(in));
		}

		if (noAnsi) {
			TaskService.setAnsiSupport(false);
		}

		String prefix = output;

		if (output.contains(".")) {
			prefix = output.substring(0, output.indexOf('.'));
		}

		String variantPath = prefix + ".txt";

		String rawPath = null;
		if (writeRaw) {
			rawPath = prefix + "_raw.txt";
		}

		StopWatch watch = new StopWatch();
		watch.start();

		List<VariantCallingTask> tasks = new Vector<VariantCallingTask>();
		int index = 0;

		for (String name : input) {

			String varName = variantPath + ".tmp." + index;

			String rawName = null;

			if (rawPath != null) {
				rawName = rawPath + ".tmp." + index;
			}

			VariantCallingTask vc = new VariantCallingTask();

			vc.setInput(name);
			vc.setVarName(varName);
			vc.setRawName(rawName);
			vc.setFreqFile(freqFile);
			vc.setLevel(level);
			vc.setBaseQ(baseQ);
			vc.setMapQ(mapQ);
			vc.setAlignQ(alignQ);
			vc.setBaq(baq);
			vc.setDeletions(deletions);
			vc.setInsertions(insertions);
			vc.setReference(reference);
			vc.setMode(mode);
			vc.setContig(contig);

			tasks.add(vc);
			index++;

		}
		TaskService.setFailureStrategy(TaskFailureStrategy.CANCEL_TASKS);
		TaskService.setThreads(threads);
		List<Task> taskList = TaskService.monitor(App.STYLE_LONG_TASK).run(tasks);

		for (Task task : taskList) {
			if (!task.getStatus().isSuccess()) {
				System.out.println();
				System.out.println("Variant Calling failed. Mutserve terminated.");
				return -1;
			}
		}

		MergeTask mergeTask = new MergeTask();
		mergeTask.setRawPath(rawPath);
		mergeTask.setInputs(tasks);
		mergeTask.setVariantPath(variantPath);
		TaskService.monitor(App.STYLE_SHORT_TASK).run(mergeTask);

		if (output.endsWith("vcf.gz") || output.endsWith("vcf")) {
			VcfWriter vcfWriter = new VcfWriter();
			vcfWriter.createVCF(variantPath, output, reference, "chrM", 16569, App.VERSION + ";" + "COMMAND");
		}

		if (writeFasta) {
			FastaWriter fastaWriter = new FastaWriter();
			fastaWriter.createFasta(variantPath, prefix + ".fasta", reference);
		}

		System.out.println();
		System.out.println("Execution Time: " + formatTime(watch.getElapsedTimeSecs()));
		System.out.println();

		watch.stop();

		return 0;

	}

	public String formatTime(long timeInSeconds) {
		return String.format("%d min, %d sec", (timeInSeconds / 60), (timeInSeconds % 60));
	}

}
