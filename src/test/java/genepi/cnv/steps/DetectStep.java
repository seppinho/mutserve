package genepi.cnv.steps;

import java.io.File;
import java.io.IOException;

import genepi.cnv.detect.DetectTool;
import genepi.cnv.util.WorkflowTestContext;
import genepi.hadoop.common.WorkflowStep;
import genepi.io.FileUtil;
import junit.framework.TestCase;

public class DetectStep extends TestCase {

	public static final boolean VERBOSE = true;

	public void testPipeline() throws IOException {

		String inputFolder = "test-data/mtdna/";
		String reference = FileUtil.path(inputFolder,"rcrs.fasta");
		String level = "1";

		// create workflow context
		WorkflowTestContext context = buildContext(inputFolder, reference, level);

		// create step instance
		DetectTool detect = new DetectHeteroplasmynMock();

		// run and test
		boolean result = run(context, detect);
	}


	class DetectHeteroplasmynMock extends DetectTool {

		private String folder;

		public DetectHeteroplasmynMock() {
			super();
		}

		@Override
		public String getFolder(Class clazz) {
			// override folder with static folder instead of jar location
			return folder;
		}

	}

	protected boolean run(WorkflowTestContext context, WorkflowStep step) {
		step.setup(context);
		return step.run(context);
	}

	protected WorkflowTestContext buildContext(String folder, String ref, String level) {
		
		
		File file = new File("test-data/tmp");
		if (file.exists()) {
			FileUtil.deleteDirectory(file);
		}
		file.mkdirs();
		
		
		WorkflowTestContext context = new WorkflowTestContext();
		context.setVerbose(VERBOSE);
		context.setInput("input", folder);
		context.setInput("reference", ref);
		context.setInput("detectionLevel",level);

		context.setOutput("outputRaw", file.getAbsolutePath() + "/outputRaw");
		FileUtil.createDirectory(file.getAbsolutePath() + "/outputRaw");
		
		context.setOutput("uncoveredPos", file.getAbsolutePath() + "/uncoveredPos");
		FileUtil.createDirectory(file.getAbsolutePath() + "/uncoveredPos");
		
		context.setOutput("outputFiltered", file.getAbsolutePath() + "/outputFiltered");
		FileUtil.createDirectory(file.getAbsolutePath() + "/outputFiltered");
		
		
		
		return context;

	}

}
