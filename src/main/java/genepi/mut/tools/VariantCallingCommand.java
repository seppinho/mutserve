package genepi.mut.tools;

import java.io.DataInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import genepi.base.Tool;
import genepi.mut.objects.BayesFrequencies;
import genepi.mut.util.FastaWriter;
import genepi.mut.util.VcfWriter;
import genepi.mut.vc.MergeTask;
import genepi.mut.vc.VariantCallingTask;
import htsjdk.samtools.util.StopWatch;
import lukfor.progress.TaskService;
import lukfor.progress.renderer.ProgressIndicatorGroup;
import lukfor.progress.tasks.Task;
import lukfor.progress.tasks.TaskFailureStrategy;

import static lukfor.progress.Components.PROGRESS_BAR;
import static lukfor.progress.Components.SPACE;
import static lukfor.progress.Components.SPINNER;
import static lukfor.progress.Components.TASK_NAME;
import static lukfor.progress.Components.TIME;

public class VariantCallingCommand extends Tool {

	public static ProgressIndicatorGroup STYLE_LONG_TASK = new ProgressIndicatorGroup(SPACE, SPINNER, SPACE, TASK_NAME,
			PROGRESS_BAR, TIME);

	public static ProgressIndicatorGroup STYLE_SHORT_TASK = new ProgressIndicatorGroup(SPACE, SPINNER, SPACE,
			TASK_NAME);

	public static final String URL = "https://github.com/seppinho/mutserve";

	public static final String APP = "mtDNA Variant Detection";

	public static final String VERSION = "v2.0.0";

	public static final String COPYRIGHT = "(c) Sebastian Schoenherr, Hansi Weissensteiner, Lukas Forer";

	String mode = "mtdna";
	String command;

	public VariantCallingCommand(String[] args) {
		super(args);
		command = Arrays.toString(args);
		System.out.println("Command " + command);
	}

	@Override
	public void createParameters() {

		addParameter("input", "input cram/bam file or folder", Tool.STRING);
		addParameter("output", "output file", Tool.STRING);
		addOptionalParameter("level", "detection level", Tool.STRING);
		addParameter("reference", "reference as fasta", Tool.STRING);
		addOptionalParameter("baseQ", "base quality", Tool.STRING);
		addOptionalParameter("mapQ", "mapping quality", Tool.STRING);
		addOptionalParameter("alignQ", "alignment quality", Tool.STRING);
		addOptionalParameter("threads", "amount of threads", Tool.STRING);
		addOptionalParameter("contig", "chrM contig name", Tool.STRING);
		addFlag("noBaq", "turn off BAQ");
		addFlag("noFreq", "turn off 1000G frequency file");
		addFlag("no-ansi", "Disable ANSI output");
		addFlag("deletions", "Call deletions");
		addFlag("insertions", "Call insertions (beta)");
		addFlag("writeFasta", "Write fasta");
	}

	@Override
	public void init() {
		System.out.println(APP + " " + VERSION);

		if (URL != null && !URL.isEmpty()) {
			System.out.println(URL);
		}
		if (COPYRIGHT != null && !COPYRIGHT.isEmpty()) {
			System.out.println(COPYRIGHT);
		}

		System.out.println();
	}

	@Override
	public int run() {

		String input = (String) getValue("input");

		String output = (String) getValue("output");

		boolean baq = !isFlagSet("noBaq");

		boolean freq = !isFlagSet("noFreq");

		boolean deletions = isFlagSet("deletions");

		boolean insertions = isFlagSet("insertions");

		boolean writeFasta = isFlagSet("writeFasta");

		boolean noAnsi = isFlagSet("no-ansi");

		double level;

		if (getValue("level") == null) {
			level = 0.01;
		} else {
			level = Double.parseDouble((String) getValue("level"));
		}

		int baseQ;
		if (getValue("baseQ") == null) {
			baseQ = 20;
		} else {
			baseQ = Integer.parseInt((String) getValue("baseQ"));
		}

		int mapQ;
		if (getValue("mapQ") == null) {
			mapQ = 20;
		} else {
			mapQ = Integer.parseInt((String) getValue("mapQ"));
		}

		int alignQ;
		if (getValue("alignQ") == null) {
			alignQ = 30;
		} else {
			alignQ = Integer.parseInt((String) getValue("alignQ"));
		}

		int threads;
		if (getValue("threads") == null) {
			threads = 1;
		} else {
			threads = Integer.parseInt((String) getValue("threads"));
		}

		String contig = (String) getValue("contig");

		String reference = (String) getValue("reference");

		System.out.println("Parameters:");
		System.out.println("Input: " + new File(input).getAbsolutePath());
		System.out.println("Output: " + new File(output).getAbsolutePath());
		System.out.println("Detection limit: " + level);
		System.out.println("Threads: " + threads);
		System.out.println("Base Quality: " + baseQ);
		System.out.println("Map Quality: " + mapQ);
		System.out.println("Alignment Quality: " + alignQ);
		System.out.println("BAQ: " + baq);
		System.out.println("1000G Frequency File: " + freq);
		System.out.println("Deletions: " + deletions);
		System.out.println("Insertions: " + insertions);
		System.out.println("Fasta: " + writeFasta);
		System.out.println("");

		File[] files = createPath(input, output);

		// load frequency file
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
		String rawPath = prefix + "_raw.txt";

		StopWatch watch = new StopWatch();
		watch.start();

		List<VariantCallingTask> tasks = new Vector<VariantCallingTask>();
		int index = 0;

		for (File file : files) {
			
			String varName = variantPath + ".tmp." + index;
			String rawName = rawPath + ".tmp." + index;

			VariantCallingTask vc = new VariantCallingTask();

			vc.setFile(file);
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
		List<Task> d = TaskService.monitor(STYLE_LONG_TASK).run(tasks);

		for (Task d1 : d) {
			if (!d1.getStatus().isSuccess()) {
				System.out.println();
				System.out.println("Variant Calling failed. Mutserve terminated.");
				return -1;
			}
		}

		MergeTask mergeTask = new MergeTask();
		mergeTask.setInputs(tasks);
		mergeTask.setRawPath(rawPath);
		mergeTask.setVariantPath(variantPath);
		TaskService.monitor(STYLE_SHORT_TASK).run(mergeTask);

		if (output.endsWith("vcf.gz") || output.endsWith("vcf")) {
			VcfWriter vcfWriter = new VcfWriter();
			vcfWriter.createVCF(variantPath, output, reference, "chrM", 16569, VERSION + ";" + command);
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

	private File[] createPath(String input, String output) {
		File folderIn = new File(input);

		File[] files = null;

		if (folderIn.exists()) {
			if (folderIn.isFile()) {
				files = new File[1];
				files[0] = new File(folderIn.getAbsolutePath());
				if (!files[0].getName().toLowerCase().endsWith(".cram")
						&& !files[0].getName().toLowerCase().endsWith(".bam")) {
					System.out.println("Please upload a CRAM/BAM file");
				}

			} else {
				files = folderIn.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".bam") || name.toLowerCase().endsWith(".cram");
					}
				});
			}
		} else {
			System.out.println("Please check input path.");
		}

		File out = new File(output);

		if (out.isDirectory()) {
			System.out.println("Error. Please specify an output file not a directory.");
		}
		return files;
	}

	public static void main(String[] args) {

		String input = "test-data/mtdna/mixtures/input/s4.bam";

		String output = "test-data/tmp.vcf";

		String ref = "test-data/mtdna/reference/rCRS.fasta";

		VariantCallingCommand pileup = new VariantCallingCommand(
				new String[] { "--input", input, "--reference", ref, "--output", output, "--level", "0.01", "--noBaq",
						"--noFreq", "--no-ansi", "--threads", "2"});

		pileup.start();

	}

	public String formatTime(long timeInSeconds) {
		return String.format("%d min, %d sec", (timeInSeconds / 60), (timeInSeconds % 60));
	}

}
