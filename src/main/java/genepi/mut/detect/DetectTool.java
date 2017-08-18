package genepi.mut.detect;

import java.io.File;

import genepi.hadoop.PreferenceStore;
import genepi.hadoop.common.WorkflowContext;
import genepi.hadoop.common.WorkflowStep;
import genepi.io.FileUtil;
import genepi.mut.objects.Extracter;
import genepi.mut.util.ReferenceUtil;

public class DetectTool extends WorkflowStep {


	@Override
	public boolean run(WorkflowContext context) {

		String archive = context.get("archive");
		String input = context.get("analyseOut");
		String outputRaw = context.get("raw");
		String outputFiltered = context.get("variants");
		String outputMultiallelic = context.get("multiallelic");
		String uncoveredPos = context.get("uncovered_pos");
		String level = context.get("level");
		
		DetectVariants detecter = new DetectVariants();
		
		String folder = getFolder(DetectTool.class);
		PreferenceStore store = new PreferenceStore(new File(FileUtil.path(folder, "job.config")));
		String version = store.getString("server.version");

		
		String archiveFolder = archive.substring(0,archive.lastIndexOf("/")+1);
		Extracter.extract(archive,archiveFolder);
	
		String ref = ReferenceUtil.findFileinDir(new File(archiveFolder),"fasta");
		String reference = ReferenceUtil.readInReference(ref);
		
		
		detecter.setVersion(version);
		detecter.setRefAsString(reference);
		detecter.setHdfsFolder(input);
		detecter.setDetectionLevel(Double.valueOf(level)/100.0);
		detecter.setOutputFiltered(outputFiltered+".txt");
		detecter.setOutputMultiallelic(outputMultiallelic+".txt");
		detecter.setUncoveredPos(uncoveredPos+".txt");
		detecter.setOutputRaw(outputRaw+".txt");
		
		context.beginTask("Detect variants...");
		
		detecter.analyzeReads();
		
		context.endTask("Variants detection finished", WorkflowContext.OK);
		
		return true;
	}

}
