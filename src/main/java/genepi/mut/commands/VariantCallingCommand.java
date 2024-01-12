package genepi.mut.commands;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;

import genepi.mut.App;
import genepi.mut.objects.BayesFrequencies;
import genepi.mut.pileup.VcfWriter;
import genepi.mut.tasks.MergeTask;
import genepi.mut.tasks.VariantCallingTask;
import genepi.mut.util.FastaWriter;
import htsjdk.samtools.reference.FastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.util.StopWatch;
import lukfor.progress.TaskService;
import lukfor.progress.tasks.Task;
import lukfor.progress.tasks.TaskFailureStrategy;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "call", version = App.VERSION, description = "Call homoplasmic and heteroplasmic positions.")
public class VariantCallingCommand implements Callable<Integer> {

	@Parameters(description = "BAM/CRAM files")
	List<String> input;

	@Option(names = { "--output" }, description = "\"Output (txt or vcf)", required = true)
	String output;

	@Option(names = { "--reference" }, description = "Reference", required = true)
	String reference;

	@Option(names = { "--threads" }, description = "Number of threads", required = false)
	int threads = 1;

	@Option(names = { "--level" }, description = "Minimum Heteroplasmy Level", required = false)
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

	@Option(names = { "--baq" }, description = "Enable BAQ", required = false, showDefaultValue = Visibility.ALWAYS)
	boolean baq = false;

	@Option(names = {
			"--no-freq" }, description = "Use Frequency File", required = false, showDefaultValue = Visibility.ALWAYS)
	boolean noFreq = false;

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
			"--excluded-samples" }, description = "Specifify mutserve mode", required = false, showDefaultValue = Visibility.ALWAYS)
	String excludedSamples = null;

	@Option(names = {
			"--no-ansi" }, description = "Disable ANSI support", required = false, showDefaultValue = Visibility.ALWAYS)
	boolean noAnsi = false;

	@Option(names = { "--version" }, versionHelp = true)
	boolean showVersion;

	@Option(names = { "--help" }, usageHelp = true)
	boolean showHelp;

	@Override
	public Integer call() throws IOException {

		if (input.size() == 1 && new File(input.get(0)).isDirectory()) {
			int count = 0;
			for (File f : new File(input.get(0)).listFiles()) {
				if (f.getName().endsWith("bam") || f.getName().endsWith("cram")) {
					input.add(f.getAbsolutePath());
					count++;
				}
			}
			System.out.println(count + " files added.");
			input.remove(0);
		}

		if (excludedSamples != null) {
			List<String> inputIncluded = new ArrayList<String>();
			String content = FileUtils.readFileToString(new File(excludedSamples), StandardCharsets.UTF_8);
			String[] excluded = content.split("\n");

			for (String file : input) {
				boolean exclude = false;
				for (String e : excluded) {
					if (e.split("\t")[0].trim().equals(file)) {
						System.out.println("File " + file + " removed");
						exclude = true;
						continue;
					}
				}
				if (!exclude) {
					inputIncluded.add(file);
				}
			}

			input = inputIncluded;
		}

		if (input == null || input.isEmpty()) {
			System.out.println();
			System.out.println("Please provide at least one indexed CRAM or BAM file.");
			System.out.println();
			System.exit(-1);
		}

		HashMap<String, Double> freqFile = null;

		if (!noFreq) {
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
				task.getStatus().getThrowable().printStackTrace();
				System.out.println("Variant Calling failed. Mutserve terminated.");
				System.exit(-1);
			}
		}

		MergeTask mergeTask = new MergeTask();
		mergeTask.setRawPath(rawPath);
		mergeTask.setInputs(tasks);
		mergeTask.setVariantPath(variantPath);
		TaskService.monitor(App.STYLE_SHORT_TASK).run(mergeTask);

		if (output.endsWith("vcf.gz") || output.endsWith("vcf")) {

			FastaSequenceFile fastaFile = new FastaSequenceFile(new File(reference), false);
			final ReferenceSequence referenceSequence = fastaFile.nextSequence();

			if (referenceSequence == null) {
				System.out.println("Can not reference fasta file");
				System.exit(-1);
			}

			VcfWriter vcfWriter = new VcfWriter();
			vcfWriter.createVCF(variantPath, output, reference, referenceSequence.getName(),
					referenceSequence.getBaseString().length(), App.VERSION);
			fastaFile.close();
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
