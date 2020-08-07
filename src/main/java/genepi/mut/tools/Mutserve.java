package genepi.mut.tools;

import java.io.File;
import java.util.Arrays;

import genepi.base.Tool;
import genepi.mut.vc.VariantCallingTask;

public class Mutserve extends Tool {

	String version = "v2.0.0-rc";
	String mode = "mtdna";
	String command;

	public Mutserve(String[] args) {
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
		addFlag("noBaq", "turn off BAQ");
		addFlag("noFreq", "turn off 1000G frequency file");
		addFlag("writeFasta", "Write fasta");
	}

	@Override
	public void init() {
		System.out.println("mtDNA Low-frequency Variant Detection " + version);
		System.out.println("Institute of Genetic Epidemiology - Medical University of Innsbruck");
		System.out.println("(c) Sebastian Schoenherr, Hansi Weissensteiner, Lukas Forer");
		System.out.println("");
	}

	@Override
	public int run() {

		String input = (String) getValue("input");

		String output = (String) getValue("output");

		boolean baq = !isFlagSet("noBaq");

		boolean freq = !isFlagSet("noFreq");

		boolean writeFasta = isFlagSet("writeFasta");

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

		String reference = (String) getValue("reference");

		System.out.println("Parameters:");
		System.out.println("Input: " + new File(input).getAbsolutePath());
		System.out.println("Output: " + new File(output).getAbsolutePath());
		System.out.println("Detection limit: " + level);
		System.out.println("Base Quality: " + baseQ);
		System.out.println("Map Quality: " + mapQ);
		System.out.println("Alignment Quality: " + alignQ);
		System.out.println("BAQ: " + baq);
		System.out.println("1000G Frequency File: " + freq);
		System.out.println("Fasta: " + writeFasta);
		System.out.println("");

		VariantCallingTask task = new VariantCallingTask();

		task.setInput(input);
		task.setOutput(output);
		task.setLevel(level);
		task.setBaseQ(baseQ);
		task.setMapQ(mapQ);
		task.setAlignQ(alignQ);
		task.setBaq(baq);
		task.setFreq(freq);
		task.setReference(reference);
		task.setMode(mode);
		task.setCommand(command);
		task.setVersion(version);

		return task.run();

	}

	public static void main(String[] args) {

		String input = "test-data/mtdna/bam/input";

		String output = "test-data/tmp.txt";

		String ref = "test-data/mtdna/reference/rCRS.fasta";

		Mutserve pileup = new Mutserve(new String[] { "--input", input, "--reference", ref, "--output", output,
				"--level", "0.01", "--noBaq", "--noFreq" });

		pileup.start();

	}

}
