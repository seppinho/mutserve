package genepi.mut.tasks;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import genepi.io.text.LineWriter;
import genepi.mut.objects.BasePosition;
import genepi.mut.objects.VariantLine;
import genepi.mut.objects.VariantResult;
import genepi.mut.pileup.BamAnalyser;
import genepi.mut.util.VariantCaller;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamInputResource;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;

public class VariantCallingTask implements ITaskRunnable {

	private String input;
	private String varName;
	private String rawName;
	private HashMap<String, Double> freqFile;
	private double level;
	private int baseQ = 20;
	private int mapQ = 20;
	private int alignQ = 30;
	boolean baq = true;
	boolean deletions = false;
	boolean insertions = false;
	String reference;
	String mode = "mtdna";
	String contig;

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		try {
			SamReader reader = null;
			String name = null;

			if (input.startsWith("http://") || input.startsWith("ftp://")) {
				reader = SamReaderFactory.makeDefault()
						.validationStringency(htsjdk.samtools.ValidationStringency.SILENT)
						.open(SamInputResource.of(new URL(input)));
				name = new URL(input).getFile();
			} else {
				reader = SamReaderFactory.makeDefault()
						.validationStringency(htsjdk.samtools.ValidationStringency.SILENT)
						.open(SamInputResource.of(new File(input)));
				name = new File(input).getName();
			}

			monitor.begin(name);

			SAMFileHeader header = reader.getFileHeader();
			SAMSequenceDictionary seqDictionary = header.getSequenceDictionary();

			// only if user has not defined a contig
			if (contig == null) {

				for (SAMSequenceRecord record : seqDictionary.getSequences()) {

					if (record.getSequenceLength() == 16569) {
						contig = record.getSequenceName();
					}
				}

			}

			BamAnalyser analyser = new BamAnalyser(name, reference, baseQ, mapQ, alignQ, baq, mode);

			HashMap<Integer, BasePosition> positions = analyser.getCounts();

			LineWriter writerRaw = null;

			if (rawName != null) {
				File rawFile = new File(rawName);
				rawFile.deleteOnExit();
				writerRaw = new LineWriter(rawFile.getAbsolutePath());
			}

			File varFile = new File(varName);
			varFile.deleteOnExit();
			LineWriter writerVar = new LineWriter(varFile.getAbsolutePath());

			String reference = analyser.getReferenceString();

			// first position to analyze
			int index = 1;

			SAMRecordIterator reads = null;
			try {
				reads = reader.query(contig, 0, 0, false);
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}

			while (reads.hasNext()) {

				if (monitor.isCanceled()) {
					return;
				}

				SAMRecord record = reads.next();

				analyser.analyseRead(record, deletions, insertions);

				int recordStart = record.getStart();

				// call variants of all positions that are analyzed
				while (index < recordStart) {
					if (positions.containsKey(index) && index <= reference.length()) {
						callVariant(writerRaw, writerVar, name, level, index, positions.get(index), reference,
								freqFile);

					}
					positions.remove(index);
					index++;
				}

			}

			// analyze remaining positions
			while (index <= reference.length()) {
				if (positions.containsKey(index) && index <= reference.length()) {
					callVariant(writerRaw, writerVar, name, level, index, positions.get(index), reference, freqFile);
				}
				positions.remove(index);
				index++;
			}

			positions = null;
			reader.close();

			monitor.done();
			writerVar.write("");
			writerVar.close();
			if (writerRaw != null) {
				writerRaw.close();
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void callVariant(LineWriter writerRaw, LineWriter writerVar, String id, double level, int pos,
			BasePosition basePosition, String reference, HashMap<String, Double> freqFile) throws IOException {

		basePosition.setId(id);

		basePosition.setPos(pos);

		VariantLine line = new VariantLine();

		char ref = reference.charAt(pos - 1);

		line.setRef(ref);

		line.parseLine(basePosition, level, freqFile);

		boolean isHeteroplasmy = false;

		for (char base : line.getMinors()) {

			// this only works since minorFWD and minorREV are equal
			double minorPercentageFwd = VariantCaller.getMinorPercentageFwd(line, base);

			double minorPercentageRev = VariantCaller.getMinorPercentageRev(line, base);

			double llrFwd = VariantCaller.determineLlrFwd(line, base);

			double llrRev = VariantCaller.determineLlrRev(line, base);

			VariantResult varResult = VariantCaller.determineLowLevelVariant(line, minorPercentageFwd,
					minorPercentageRev, llrFwd, llrRev, level, base);

			if (varResult.getType() == VariantCaller.LOW_LEVEL_VARIANT) {

				isHeteroplasmy = true;

				// set correct minor base for output result!
				varResult.setMinor(base);

				double hetLevel = VariantCaller.calcVariantLevel(line, minorPercentageFwd, minorPercentageRev);
				double levelTop = VariantCaller.calcLevel(line, line.getTopBasePercentsFWD(),
						line.getTopBasePercentsREV());

				double levelMinor = VariantCaller.calcLevel(line, minorPercentageFwd, minorPercentageRev);

				varResult.setLevelTop(levelTop);

				varResult.setLevelMinor(levelMinor);

				varResult.setLevel(hetLevel);

				String res = VariantCaller.writeVariant(varResult);

				writerVar.write(res);
			}
		}

		if (!isHeteroplasmy) {

			VariantResult varResult = VariantCaller.determineVariants(line);

			if (varResult != null) {

				String res = VariantCaller.writeVariant(varResult);

				writerVar.write(res);
			}
		}
		// raw data
		if (writerRaw != null) {
			String raw = line.toRawString();
			writerRaw.write(raw);
		}

	}

	public double getLevel() {
		return level;
	}

	public void setLevel(double level) {
		this.level = level;
	}

	public int getBaseQ() {
		return baseQ;
	}

	public void setBaseQ(int baseQ) {
		this.baseQ = baseQ;
	}

	public double getMapQ() {
		return mapQ;
	}

	public void setMapQ(int mapQ) {
		this.mapQ = mapQ;
	}

	public double getAlignQ() {
		return alignQ;
	}

	public void setAlignQ(int alignQ) {
		this.alignQ = alignQ;
	}

	public boolean isBaq() {
		return baq;
	}

	public void setBaq(boolean baq) {
		this.baq = baq;
	}

	public boolean isDeletions() {
		return deletions;
	}

	public void setDeletions(boolean deletions) {
		this.deletions = deletions;
	}

	public boolean isInsertions() {
		return insertions;
	}

	public void setInsertions(boolean insertions) {
		this.insertions = insertions;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public HashMap<String, Double> getFreqFile() {
		return freqFile;
	}

	public void setFreqFile(HashMap<String, Double> freqFile) {
		this.freqFile = freqFile;
	}

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public String getRawName() {
		return rawName;
	}

	public void setRawName(String rawName) {
		this.rawName = rawName;
	}

	public String getContig() {
		return contig;
	}

	public void setContig(String contig) {
		this.contig = contig;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

}
