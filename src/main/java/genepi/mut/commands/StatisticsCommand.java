package genepi.mut.commands;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

import genepi.io.text.LineWriter;
import genepi.mut.App;
import genepi.mut.objects.StatisticsFile;
import genepi.mut.util.StatisticsFileUtil;
import genepi.mut.util.report.OutputWriter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

@Command(name = "stats", version = App.VERSION, description = "summarize statistics.")
public class StatisticsCommand implements Callable<Integer> {

	List<String> allowed_contigs = new ArrayList<>(
			List.of("chrM", "MT", "chrMT", "rCRS", "NC_012920.1", "gi|251831106|ref|NC_012920.1|"));

	@Option(names = { "--input" }, description = "\"Input file", required = true)
	private String input;

	@Option(names = { "--mapping" }, description = "\"Mapping file", required = false)
	private String mapping = null;

	@Option(names = { "--output-excluded-samples" }, description = "\"Exclude file", required = true)
	private String output;

	@Option(names = { "--output-contig" }, description = "\"Exclude file", required = false)
	private String contigOut = "chrM";

	@Option(names = { "--tool" }, description = "\"Tool", required = false)
	private String tool = "mutserve";

	@Option(names = {
			"--baseQ" }, description = "Minimum Base Quality", required = false, showDefaultValue = Visibility.ALWAYS)
	private int baseQ = 20;

	@Option(names = {
			"--mapQ" }, description = "Minimum Map Quality", required = false, showDefaultValue = Visibility.ALWAYS)
	private int mapQ = 30;

	@Option(names = {
			"--alignQ" }, description = "Minimum Alignment Quality", required = false, showDefaultValue = Visibility.ALWAYS)
	private int alignQ = 30;

	@Option(names = {
			"--detection-limit" }, description = "Defined Detection Limit", required = false, showDefaultValue = Visibility.ALWAYS)
	private double detectionLimit = 0.01;

	@Option(names = {
			"--reference" }, description = "Reference for Variant Calling", required = false, showDefaultValue = Visibility.ALWAYS)
	private String reference = "rcrs";

	@Option(names = "--report", description = "Cloudgene Report Output", required = false)
	private String report = null;
	
	private OutputWriter outputWriter = null;
	
	@Option(names = {
	"--min-coverage-percentage" }, description = "Minimal Coverage (%)", required = false, showDefaultValue = Visibility.ALWAYS)
private int minCoveragePercentage = 50;	
	
	@Option(names = {
	"--min-mean-depth" }, description = "Minimal Mean Depth", required = false, showDefaultValue = Visibility.ALWAYS)
private int minMeanDepth = 50;	
	
	@Option(names = {
	"--min-mean-base-quality" }, description = "Minimal Mean Bas Quality", required = false, showDefaultValue = Visibility.ALWAYS)
private int minMeanBaseQ = 10;	
	
	
	@Override
	public Integer call() throws IOException {
		
		if (report != null) {
			outputWriter = new OutputWriter(report);
		} else {
			outputWriter = new OutputWriter();
		}

		StringBuffer excludedSamplesFile = new StringBuffer();
		LineWriter writer = new LineWriter(output);
		LineWriter writerContig = new LineWriter(contigOut);

		StatisticsFileUtil stats = new StatisticsFileUtil();
		List<StatisticsFile> samples = stats.load(input);

		int countLowCoveredPercentage = 0;
		int countMeanBaseQuality = 0;
		int countNoReadGroups = 0;
		int countMeanDepth = 0;
		int countTooLowBaseQuality = 0;
		int countTooLowMapQuality = 0;
		int countMissingContigs = 0;
		int excludedSamples = 0;
		double lowestMeanDepth = -1;
		double highgestMeanDepth = -1;
		double lowestMeanBaseQuality = -1;
		double highestMeanBaseQuality = -1;
		List<String> contigs = new ArrayList<String>();
		List<String> text = new Vector<String>();

		if (mapping != null) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(mapping));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return 1;
			}
			String line;
			ArrayList<String> sampleList = new ArrayList<>();

			// header
			reader.readLine();
			
		

			while ((line = reader.readLine()) != null) {
				String sampleName = line.split("\t")[0];
				String fileName = line.split("\t")[1];
				if (sampleList.contains(sampleName)) {
					text.add("\n<b>Error:</b> Duplicate sample name for sample '" + sampleName + "' (Filename: "
							+ fileName + ".<br>mtDNA analysis cannot be started!");
					outputWriter.error(text);
					reader.close();
					System.out.println("\n\nERROR: Duplicate sample name for sample '" + sampleName + "' (Filename: " + fileName+".");
					return 1;
				}

				sampleList.add(sampleName);
			}
			reader.close();
		}

		for (StatisticsFile sample : samples) {
			
			if (sample.getReadGroup() == null) {
				{
					countNoReadGroups++;
				}
			}

			if (sample.getSampleName() == null) {
				text.add("\n<b>Error:</b> Error in sample file name.<br>mtDNA analysis cannot be started!");
				outputWriter.error(text);
				System.out.println("\n\nERROR: No sample file name has been detected.");
				return 1;
			}

			if (sample.getContig() != null) {

				if (!contigs.contains(sample.getContig())) {
					contigs.add(sample.getContig());
				}
			} else {
				countMissingContigs++;
				excludedSamples++;
				excludedSamplesFile.append(sample.getSampleName() + "\n");
			}

			if (sample.getMeanDepth() != -1) {

				if (sample.getMeanDepth() <= lowestMeanDepth || lowestMeanDepth == -1) {
					lowestMeanDepth = sample.getMeanDepth();
				}

				if (sample.getMeanDepth() > highgestMeanDepth) {
					highgestMeanDepth = sample.getMeanDepth();
				}
			}

			if (sample.getMeanBaseQuality() != -1) {

				if (sample.getMeanBaseQuality() <= lowestMeanBaseQuality || lowestMeanBaseQuality == -1) {
					lowestMeanBaseQuality = sample.getMeanBaseQuality();
				}

				if (sample.getMeanBaseQuality() > highestMeanBaseQuality) {
					highestMeanBaseQuality = sample.getMeanBaseQuality();
				}
			}

			if (sample.getCoveredPercentage() != -1 && sample.getCoveredPercentage() < minCoveragePercentage) {
				countLowCoveredPercentage++;
				excludedSamples++;
				excludedSamplesFile.append(
						sample.getSampleName() + "\t" + "Mean Coverage Percentage <" + minCoveragePercentage + "\n");
			} else if (sample.getMeanBaseQuality() != -1 && sample.getMeanBaseQuality() < minMeanBaseQ) {
				countMeanBaseQuality++;
				excludedSamples++;
				excludedSamplesFile
						.append(sample.getSampleName() + "\t" + "Mean Base Quality < " + minMeanBaseQ + "\n");
			} else if (sample.getMeanDepth() != -1 && sample.getMeanDepth() < minMeanDepth) {
				countMeanDepth++;
				excludedSamples++;
				excludedSamplesFile.append(sample.getSampleName() + "\t" + "Mean Depth < " + minMeanDepth + "\n");
			} else if (sample.getMeanBaseQuality() != -1 && sample.getMeanBaseQuality() < baseQ) {
				countTooLowBaseQuality++;
				excludedSamples++;
				excludedSamplesFile
						.append(sample.getSampleName() + "\t" + "Sample Mean Base Quality < " + baseQ + "\n");
			} else if (sample.getMeanMapQuality() != -1 && sample.getMeanMapQuality() < mapQ) {
				countTooLowBaseQuality++;
				excludedSamples++;
				excludedSamplesFile.append(sample.getSampleName() + "\t" + "Mapping Quality < " + mapQ + "\n");
			}

		}

		writer.write(excludedSamplesFile.toString());
		writer.close();

		text = new Vector<String>();

		text.add("<b>Variant Calling Parameters:</b> \n");
		text.add("Mode: " + tool + "\n");
		text.add("Reference: " + reference + "\n");
		text.add("Heteroplasmic Detection Limit: " + detectionLimit + "\n");
		text.add("Min Mean Coverage: " + minMeanDepth + "\n");
		text.add("Min Base Quality: " + baseQ + "\n");
		text.add("Min Mapping Quality: " + mapQ + "\n");
		text.add("Min Alignment Quality: " + alignQ + "\n");
		outputWriter.message(text);

		text = new Vector<String>();
		int validFiles = samples.size() - excludedSamples;
		text.add("<b>Statistics:</b> \n");
		text.add("Input Samples: " + samples.size() + "\n");
		text.add("Passed Samples: " + validFiles + "\n");

		
		if (contigs.size() == 0) {
			outputWriter.error("No valid mtDNA contigs with length 16569 have been detected in your input files.");
			System.out.println("\n\nERROR: No valid mtDNA contigs detected.");
			return 1;
		}
	
		if (contigs.size() != 1) {
			outputWriter.error("Different mtDNA contig names have been detected in your input files.");
			System.out.println("\n\nERROR: Different contig names have been detected for your input samples. Please upload them in different batches.");
			return 1;
		}
		
		text.add("Detected mtDNA contig name: " + contigs.get(0) + "\n");

		if (lowestMeanDepth != -1) {
			text.add("Min Mean Depth: " + lowestMeanDepth + "\n");
		}
		if (highgestMeanDepth != -1 && validFiles > 1) {
			text.add("Max Mean Depth: " + highgestMeanDepth + "\n");
		}

		if (lowestMeanDepth != -1) {
			text.add("Min Mean Base Quality: " + lowestMeanBaseQuality + "\n");
		}
		if (highgestMeanDepth != -1 && validFiles > 1) {
			text.add("Max Mean Base Quality: " + highestMeanBaseQuality + "\n");
		}

		outputWriter.message(text);


		if (countMissingContigs > 0) {
			text.add(countMissingContigs + " sample(s) with missing contigs have been excluded.");
		}

		writerContig.write(contigs.get(0));
		writerContig.close();

		if (countLowCoveredPercentage > 0) {
			text.add(countLowCoveredPercentage + " sample(s) with a coverage percentage of < "
					+ minCoveragePercentage + " have been excluded.");
		}
		if (countMeanBaseQuality > 0) {
			text.add(countMeanBaseQuality + " sample(s) with a mean base quality of < " + minMeanBaseQ
					+ " have been excluded.");
		}

		if (countNoReadGroups > 0) {
			text.add("For " + countNoReadGroups + " sample(s) a readgroup tag (@RG) have been added");
		}

		if (countMeanDepth > 0) {
			text.add(countMeanDepth + " sample(s) with mean depth of < " + minMeanDepth + " have been excluded.");
		}

		if (countTooLowBaseQuality > 0) {
			text.add(countTooLowBaseQuality
					+ " sample(s) have been removed where the mean base quality is lower then configured base quality ("
					+ baseQ + ").");
		}
		if (countTooLowMapQuality > 0) {
			text.add(countTooLowMapQuality
					+ " sample(s) have been removed where the mean base quality is lower then configured base quality ("
					+ mapQ + ").");
		}

		if (text.size() > 0) {
			outputWriter.warning(text);
		}

		if (validFiles == 0) {
			outputWriter.error("No input samples passed the QC step.");
			System.out.println("\n\nERROR: No input samples passed the QC step.");
			return 1;
		} else {
			outputWriter.message("Input Validation finished successfully, mtDNA analysis can be started.");
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
	
	public void setReport(String report) {
		this.report = report;
	}

}
