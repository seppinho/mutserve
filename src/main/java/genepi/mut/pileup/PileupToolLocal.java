package genepi.mut.pileup;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import genepi.base.Tool;
import genepi.io.FileUtil;
import genepi.io.text.LineWriter;
import genepi.mut.objects.BasePosition;
import genepi.mut.objects.VariantLine;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.reference.FastaSequenceIndex;
import htsjdk.samtools.reference.FastaSequenceIndexCreator;

public class PileupToolLocal extends Tool {

	public PileupToolLocal(String[] args) {
		super(args);
	}

	@Override
	public void createParameters() {

		addParameter("input", "input bam folder", Tool.STRING);
		addParameter("outputRaw", "output raw file", Tool.STRING);
		addParameter("outputVar", "output variants file", Tool.STRING);
		addParameter("level", "detection level", Tool.DOUBLE);
		addParameter("reference", "reference as fasta", Tool.STRING);
		addParameter("indel", "call indels?", Tool.STRING);
		addParameter("baq", "apply BAQ?", Tool.STRING);
		addParameter("baseQ", "base quality", Tool.INTEGER);
		addParameter("mapQ", "mapping quality", Tool.INTEGER);
		addParameter("alignQ", "alignment quality", Tool.INTEGER);
	}

	@Override
	public void init() {
		System.out.println("Execute CNV-Server locally \n");
	}

	@Override
	public int run() {

		String version = "mtdna";

		String input = (String) getValue("input");

		String outputRaw = (String) getValue("outputRaw");

		String outputVar = (String) getValue("outputVar");

		String indel = (String) getValue("indel");

		String baq = (String) getValue("baq");

		double level = (double) getValue("level");

		int baseQ = (int) getValue("baseQ");

		int mapQ = (int) getValue("mapQ");

		int alignQ = (int) getValue("alignQ");

		String refPath = (String) getValue("reference");

		LineWriter writerRaw = null;

		LineWriter writerVar = null;

		File folderIn = new File(input);

		if (!folderIn.exists()) {

			System.out.println("Please check if input folder exists");
			System.out.println(folderIn.getAbsolutePath());
			return 0;
		}

		try {

			File outRaw = new File(outputRaw);

			File outVar = new File(outputVar);

			File parentRaw = outRaw.getParentFile();
			File parentVar = outVar.getParentFile();

			if (parentRaw != null) {
				outRaw.getParentFile().mkdirs();
			}

			if (parentVar != null) {
				outVar.getParentFile().mkdirs();
			}

			writerRaw = new LineWriter(outRaw.getAbsolutePath());

			writerVar = new LineWriter(outVar.getAbsolutePath());

			writerRaw.write(BamAnalyser.headerRaw);

			writerVar.write(BamAnalyser.headerVariants);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		long start = System.currentTimeMillis();

		File[] files = folderIn.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".bam");
			}
		});
		
		for (File file : files) {

			BamAnalyser analyser = new BamAnalyser(file.getName(), refPath, baseQ, mapQ, alignQ, Boolean.valueOf(baq),
					version);

			System.out.println(" Processing: " + file.getName());

			try {

				analyseReads(file, analyser);

				determineVariants(analyser, writerRaw, writerVar, level, Boolean.valueOf(indel));

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}

		}

		try {
			writerVar.close();
			writerRaw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Raw file written to " + new File(outputRaw).getAbsolutePath());
		System.out.println("Variants file written to " + new File(outputVar).getAbsolutePath());
		System.out.println("Time: " + (System.currentTimeMillis() - start) / 1000 + " sec");
		return 0;
	}

	// mapper
	private void analyseReads(File file, BamAnalyser analyser) throws Exception, IOException {

		// TODO double check if primary and secondary alignment is used for
		// CNV-Server
		final SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.LENIENT)
				.open(file);

		SAMRecordIterator fileIterator = reader.iterator();

		while (fileIterator.hasNext()) {

			SAMRecord record = fileIterator.next();

			analyser.analyseRead(record);

		}
		reader.close();
	}

	// reducer
	private void determineVariants(BamAnalyser analyser, LineWriter writerRaw, LineWriter writerVar, double level,
			boolean indel) throws IOException {

		HashMap<String, BasePosition> counts = analyser.getCounts();

		String reference = analyser.getReferenceString();

		for (String key : counts.keySet()) {

			String id = key.split(":")[0];

			int pos = Integer.valueOf(key.split(":")[1]);

			if (pos > 0 && pos <= reference.length()) {

				BasePosition basePos = counts.get(key);

				basePos.setId(id);

				basePos.setPos(pos);

				char ref = reference.charAt(pos - 1);

				VariantLine line = new VariantLine();

				line.setCallDel(indel);

				line.setRef(ref);

				line.analysePosition(basePos, level);

				line.callVariants(level);

				if (line.isFinalVariant()) {
					writerVar.write(line.writeVariant());
				}

				// raw data
				String raw = line.toRawString();
				writerRaw.write(raw);

			}

		}
	}

	public static void main(String[] args) {

		String input = "test-data/mtdna/bam/input/";
		String outputVar = "test-data/tmp/out_var.txt";
		String outputRaw = "test-data/tmp/out_raw.txt";
		String fasta = "test-data/mtdna/bam/reference/rCRS.fasta";

		PileupToolLocal pileup = new PileupToolLocal(new String[] { "--input", input, "--reference", fasta,
				"--outputVar", outputVar, "--outputRaw", outputRaw, "--level", "0.01", "--baq", "true", "--indel",
				"false", "--baseQ", "20", "--mapQ", "20", "--alignQ", "30" });

		pileup.start();

	}

}