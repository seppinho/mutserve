package genepi.mut.pileup;

import genepi.hadoop.PreferenceStore;
import genepi.hadoop.common.WorkflowContext;
import genepi.mut.util.HadoopJobStep;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class PileupStep extends HadoopJobStep {

	@Override
	public boolean run(WorkflowContext context) {

		String type = context.get("inType");

		final String folder = getFolder(PileupStep.class);

		String input;
		if (type.equals("se") || type.equals("pe")) {
			input = context.get("outputBam");
		} else {
			input = context.get("input");
		}
		String rawHdfs = context.get("rawHdfs");
		String rawLocal = context.get("rawLocal");
		String variantsHdfs = context.get("variantsHdfs");
		String variantsLocal = context.get("variantsLocal");
		String mappingQual = context.get("mapQuality");
		String baseQual = context.get("baseQuality");
		String alignQual = context.get("alignQuality");
		String archive = context.get("archive");
		String level = context.get("level");
		Boolean baq = Boolean.valueOf(context.get("baq"));
		Boolean callDel = Boolean.valueOf(context.get("callDel"));

		PileupJob bamJob = new PileupJob("Generate Pileup") {
			@Override
			protected void readConfigFile() {
				File file = new File(folder + "/" + CONFIG_FILE);
				if (file.exists()) {
					log.info("Loading distributed configuration file " + folder + "/" + CONFIG_FILE + "...");
					PreferenceStore preferenceStore = new PreferenceStore(file);
					preferenceStore.write(getConfiguration());
					for (Object key : preferenceStore.getKeys()) {
						log.info("  " + key + ": " + preferenceStore.getString(key.toString()));
					}

				} else {

					log.info("No distributed configuration file (" + CONFIG_FILE + ") available.");

				}
			}
		};
		bamJob.setInput(input);
		bamJob.setOutput(rawHdfs);
		bamJob.setRawLocal(rawLocal);
		bamJob.setVariantsPathHdfs(variantsHdfs);
		bamJob.setVariantsPathLocal(variantsLocal);
		
		bamJob.setMappingQuality(mappingQual);
		bamJob.setBaseQuality(baseQual);
		bamJob.setAlignmentQuality(alignQual);
		bamJob.setBAQ(baq);
		bamJob.setLevel(level);
		bamJob.setCallDel(callDel);
		bamJob.setArchive(archive);
		bamJob.setJarByClass(PileupStep.class);
		bamJob.setFolder(folder);

		boolean successful = executeHadoopJob(bamJob, context);

		if (successful) {

			// print qc statistics
			DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.US);

			StringBuffer text = new StringBuffer();

			text.append("<b> Statistics:</b> <br>");
			text.append("Overall Reads: " + df.format(bamJob.getOverall()) + "<br>");
			text.append("Filtered Reads: " + df.format(bamJob.getFiltered()) + "<br>");
			text.append("Passed Reads: " + df.format(bamJob.getUnfiltered()) + "<br>");
			text.append("<br>");
			text.append("Filtered Reads:" + "<br>");
			text.append("Read Mapping Quality BAD: " + df.format(bamJob.getBadMapping()) + "<br>");
			text.append("Unmapped Reads: " + df.format(bamJob.getUnmapped()) + "<br>");
			text.append("Wrong Reference in BAM: " + df.format(bamJob.getWrongRef()) + "<br>");
			text.append("Bad Alignment: " + df.format(bamJob.getBadALigment()) + "<br>");
			text.append("Duplicates: " + df.format(bamJob.getDupl()) + "<br>");
			text.append("Short Reads (<25 bp): " + df.format(bamJob.getShortRead()) + "<br>");
			context.ok(text.toString());

			if (bamJob.getUnfiltered() == 0) {
				context.error("No reads passed Quality Control!");
				return false;
			}

		} else {

			context.error("QC Quality Control failed!");
			return false;

		}
		return successful;

	}

}