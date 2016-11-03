package genepi.vcbox.detect;

import java.io.File;

import genepi.base.Tool;
import genepi.hadoop.PreferenceStore;
import genepi.vcbox.util.ReferenceUtil;

public class DetectTool extends Tool {

	public DetectTool(String[] args) {
		super(args);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createParameters() {
		addParameter("input", "input path");
		addParameter("outputRaw", "output path raw");
		addParameter("outputFiltered", "output path filtered");
		addParameter("uncoveredPos", "output pos not covered by model");
		addParameter("detectionLevel", "detection level in %");
		addParameter("reference", "reference");
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
	}

	@Override
	public int run() {

		String reference = (String) getValue("reference");
		String input = (String) getValue("input");
		String outputRaw = (String) getValue("outputRaw");
		String outputFiltered = (String) getValue("outputFiltered");
		String uncoveredPos = (String) getValue("uncoveredPos");
		String level = (String) getValue("detectionLevel");
		
		DetectVariants detecter = new DetectVariants();
		
		PreferenceStore store = new PreferenceStore(new File("job.config"));
		String version = store.getString("server.version");
		String ref =  ReferenceUtil.readInReference(reference+".fasta");
		
		detecter.setVersion(version);
		detecter.setRefAsString(ref);
		detecter.setHdfsFolder(input);
		detecter.setDetectionLevel(Double.valueOf(level)/100.0);
		detecter.setOutputFiltered(outputFiltered);
		detecter.setUncoveredPos(uncoveredPos);
		detecter.setOutputRaw(outputRaw);
		detecter.analyzeReads();
		
		return 0;
	}

}
