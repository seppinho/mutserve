package genepi.mut.commands;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

import genepi.io.text.LineWriter;
import genepi.mut.App;
import genepi.mut.objects.StatisticsFile;
import genepi.mut.util.StatisticsFileUtil;
import genepi.mut.util.report.CloudgeneReport;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

@Command(name = "stats", version = App.VERSION, description = "summarize statistics.")
public class StatisticsCommand implements Callable<Integer> {

	private static final int MIN_COVERAGE_PERCENTAGE = 50;
	private static final int MIN_MEAN_BASE_QUALITY = 10;
	private static final int MIN_MEAN_DEPTH = 50;

	@Option(names = { "--input" }, description = "\"Input file", required = true)
	private String input;

	@Option(names = { "--output" }, description = "\"Exclude file", required = true)
	private String output;

	@Option(names = {
			"--baseQ" }, description = "Minimum Base Quality", required = false, showDefaultValue = Visibility.ALWAYS)
	private int baseQ = 20;

	@Option(names = {
			"--mapQ" }, description = "Minimum Map Quality", required = false, showDefaultValue = Visibility.ALWAYS)
	private int mapQ = 30;

	@Option(names = "--report", description = "Cloudgene Report Output", required = false)
	private String report = "cloudgene.report.json";

	private CloudgeneReport context = new CloudgeneReport();

	@Override
	public Integer call() throws IOException {

		context.setFilename(report);

		context.beginTask("Analyze files ");
		
		StringBuffer text = new StringBuffer();
		LineWriter writer = new LineWriter(output);
		

		StatisticsFileUtil stats = new StatisticsFileUtil();
		List<StatisticsFile> samples = stats.load(input);

		int countLowCoveredPercentage = 0;
		int countMeanBaseQuality = 0;
		int countMeanDepth = 0;
		int countTooLowBaseQuality = 0;
		int countTooLowMapQuality = 0;
		int countMissingContigs = 0;
		int excludedSamples = 0;
		HashSet<String> contigs = new HashSet<String>();

		for (StatisticsFile sample : samples) {

			if (sample.getContig() != null) {
				contigs.add(sample.getContig());
			} else {
				countMissingContigs++;
				excludedSamples++;
				text.append(sample.getSampleName()+",");
			}
			if (sample.getCoveredPercentage() != -1 && sample.getCoveredPercentage() < MIN_COVERAGE_PERCENTAGE) {
				countLowCoveredPercentage++;
				excludedSamples++;
				text.append(sample.getSampleName()+",");
			} else if (sample.getMeanBaseQuality() != -1 && sample.getMeanBaseQuality() < MIN_MEAN_BASE_QUALITY) {
				countMeanBaseQuality++;
				excludedSamples++;
				text.append(sample.getSampleName()+",");
			} else if (sample.getMeanDepth() != -1 && sample.getMeanDepth() < MIN_MEAN_DEPTH) {
				countMeanDepth++;
				excludedSamples++;
				text.append(sample.getSampleName()+",");
			} else if (sample.getMeanBaseQuality() != -1 && sample.getMeanBaseQuality() < baseQ) {
				countTooLowBaseQuality++;
				excludedSamples++;
				text.append(sample.getSampleName()+",");
			} else if (sample.getMeanMapQuality() != -1 && sample.getMeanMapQuality() < mapQ) {
				countTooLowBaseQuality++;
				excludedSamples++;
				text.append(sample.getSampleName()+",");
			}

		}

		writer.write(text.toString());
		writer.close();
		text = new StringBuffer();

		if (contigs.size() > 1) {
			context.endTask("Different contigs have been detected", CloudgeneReport.ERROR);
			return -1;
		} else {
			context.ok("Detected contig name: " + contigs);
		}

		if (countMissingContigs > 0) {
			text.append(countMissingContigs + " sample(s) with missing contigs have been excluded");
		}

		if (countLowCoveredPercentage > 0) {
			text.append(countLowCoveredPercentage + " sample(s) with a coverage percentage of < "
					+ MIN_COVERAGE_PERCENTAGE + " have been excluded");
		}
		if (countMeanBaseQuality > 0) {
			text.append(countMeanBaseQuality + " sample(s) with a mean base quality of < " + MIN_MEAN_BASE_QUALITY
					+ " have been excluded");
		}

		if (countMeanDepth > 0) {
			text.append(countMeanDepth + " sample(s) with mean depth of < " + MIN_MEAN_DEPTH
					+ " have been excluded. This affects the level of heteroplasmy which can be detected.");
		}

		if (countTooLowBaseQuality > 0) {
			text.append(countTooLowBaseQuality
					+ " sample(s) have been removed where the mean base quality is lower then configured base quality ("
					+ baseQ + ")");
		}
		if (countTooLowMapQuality > 0) {
			text.append(countTooLowMapQuality
					+ " sample(s) have been removed where the mean base quality is lower then configured base quality ("
					+ mapQ + ")");
		}

		int validFiles = samples.size() - excludedSamples;

		context.warning(text.toString());
		if (validFiles == 0) {
			context.endTask("No input samples passed the QC step", CloudgeneReport.ERROR);
			return -1;
		} else {

			context.endTask("Detected and validated files:" + validFiles, CloudgeneReport.OK);

			return 0;
		}
	}

	public String formatTime(long timeInSeconds) {
		return String.format("%d min, %d sec", (timeInSeconds / 60), (timeInSeconds % 60));
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

}
