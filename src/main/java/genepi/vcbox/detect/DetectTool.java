package genepi.vcbox.detect;

import genepi.base.Tool;

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
		addParameter("detectionLevel", "detection level in %");
		addParameter("reference", "reference");
		addParameter("variants", "variants");
		
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
		String level = (String) getValue("level");
		
		DetectVariants detecter = new DetectVariants(reference+".fasta");
		
		detecter.setHdfsFolder(input);
		detecter.setDetectionLevel(Double.valueOf(level)/100.0);
		detecter.setOutputFiltered(outputFiltered);
		detecter.setOutputRaw(outputRaw);
		detecter.analyzeReads();
		
		return 0;
	}

}
