package genepi.vcbox.pileup;

import genepi.hadoop.HdfsUtil;
import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.io.HdfsLineWriter;
import genepi.vcbox.util.HadoopJobStep;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class PileupTool extends HadoopJobStep {

	@Override
	public boolean run(WorkflowContext context) {

		String type = context.get("inType");

		String input;
		System.out.println("type is " + type);
		if (type.equals("se") || type.equals("pe")) {
			input = context.get("outputBam");
		} else {
			input = context.get("input");
		}
		String output = context.get("analyseOut");
		String mappingQual = context.get("mapQuality");
		String baseQual = context.get("baseQuality");
		String alignQual = context.get("alignQuality");
		String stats = context.get("statistics");
		String reference = context.get("reference");
		Boolean baq = Boolean.valueOf(context.get("baq"));
		
		PileupJob bamJob = new PileupJob("Analyse BAM");
		bamJob.setInput(input);
		bamJob.setOutput(output);
		bamJob.setMappingQuality(mappingQual);
		bamJob.setBaseQuality(baseQual);
		bamJob.setAlignmentQuality(alignQual);
		bamJob.setBAQ(baq);
		bamJob.setReference(reference);
		bamJob.setJarByClass(PileupTool.class);

		boolean successful = executeHadoopJob(bamJob, context);

		if (successful) {

			// print qc statistics
			DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.US);
			DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();

			StringBuffer text = new StringBuffer();

			text.append("<b> Statistics:</b> <br>");
			text.append("Overall Reads: " + df.format(bamJob.getOverall()) + "<br>");
			text.append("Filtered Reads: " + df.format(bamJob.getFiltered()) + "<br>");
			text.append("Passed Reads: " + df.format(bamJob.getUnfiltered()) + "<br>");
			text.append("Passed FWD Reads: " + df.format(bamJob.getFwdRead()) + "<br>");
			text.append("Passed REV Reads: " + df.format(bamJob.getRevRead()) + "<br>");
			text.append("<br>");
			text.append("Mapping Quality OK: " + df.format(bamJob.getGoodMapping()) + "<br>");
			text.append("Mapping Quality BAD: " + df.format(bamJob.getBadMapping()) + "<br>");
			text.append("Unmapped Reads: " + df.format(bamJob.getUnmapped()) + "<br>");
			text.append("Wrong Reference in BAM: " + df.format(bamJob.getWrongRef()) + "<br>");
			text.append("<br>");
			text.append("Base Read Quality OK: " + df.format(bamJob.getGoodQual()) + "<br>");
			text.append("Base Read Quality BAD: " + df.format(bamJob.getBadQual()) + "<br>");
			text.append("<br>");
			text.append("Bad Alignment: " + df.format(bamJob.getBadALigment()) + "<br>");
			text.append("Duplicates: " + df.format(bamJob.getDupl()) + "<br>");
			text.append("Short Reads (<25 bp): " + df.format(bamJob.getShortRead()) + "<br>");
			context.ok(text.toString());

			if (bamJob.getUnfiltered() == 0 || bamJob.getGoodQual() == 0) {
				context.error("No reads passed Quality Control!");
				return false;
			}
			
			if ((bamJob.getFwdRead() == 0 || bamJob.getRevRead() == 0)) {
				context.error("Reads from one strand are missing.");
				return false;
			}

			try {
				HdfsLineWriter logWriter = new HdfsLineWriter(HdfsUtil.path(stats));
				logWriter.write("BAM File Statistics ");
				logWriter.write("Overall Reads\t" + bamJob.getOverall());
				logWriter.write("Filtered Reads\t " + bamJob.getFiltered());
				logWriter.write("Passed Reads\t " + bamJob.getUnfiltered());
				logWriter.write("Mapping Quality\t " + bamJob.getGoodMapping());
				logWriter.write("Mapping Quality BAD\t " + bamJob.getBadMapping());
				logWriter.write("Unmapped Reads\t " + bamJob.getUnmapped());
				logWriter.write("Wrong Reference in BAM\t " + bamJob.getWrongRef());
				logWriter.write("Base Read Quality OK\t " + bamJob.getGoodQual());
				logWriter.write("Base Read Quality BAD\t " + bamJob.getBadQual());
				logWriter.write("Bad Alignment\t " + bamJob.getBadALigment());
				logWriter.write("Duplicates\t " + bamJob.getDupl());
				logWriter.write("Short Reads (<25 bp)\t " + bamJob.getShortRead());

				logWriter.write("");
				logWriter.close();
				

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {

			context.error("QC Quality Control failed!");
			return false;

		}
		return successful;

	}
	

}