package genepi.mut.vc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import genepi.io.text.LineWriter;
import genepi.mut.objects.BasePosition;
import genepi.mut.objects.VariantLine;
import genepi.mut.objects.VariantResult;
import genepi.mut.pileup.BamAnalyser;
import genepi.mut.util.VariantCaller;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import lukfor.progress.tasks.ITaskRunnable;
import lukfor.progress.tasks.monitors.ITaskMonitor;

public class VariantCallingTask implements ITaskRunnable {

	private File file;
	private String varName;
	private String rawName;
	private String output;
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

	@Override
	public void run(ITaskMonitor monitor) throws Exception {

		BamAnalyser analyser = new BamAnalyser(file.getName(), reference, baseQ, mapQ, alignQ, baq, mode);

		HashMap<Integer, BasePosition> positions = analyser.getCounts();

		LineWriter writerVar = new LineWriter(new File(varName).getAbsolutePath());
		writerVar.write(BamAnalyser.headerVariants);

		LineWriter writerRaw = new LineWriter(new File(rawName).getAbsolutePath());
		writerRaw.write(BamAnalyser.headerRaw);

		// first position to analyze
		int pos = 1;

		try {

			final SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT)
					.open(file);

			SAMRecordIterator fileIterator = reader.iterator();

			String reference = analyser.getReferenceString();

			monitor.begin(file.getName());

			while (fileIterator.hasNext()) {

				SAMRecord record = fileIterator.next();

				analyser.analyseRead(record, deletions, insertions);

				int current = record.getStart();

				// call variants between pos and current
				while (pos < current) {

					if (positions.containsKey(pos) && pos <= reference.length()) {
						callVariant(writerRaw, writerVar, file.getName(), level, pos, positions.get(pos), reference,
								freqFile);

					}
					positions.remove(pos);
					pos++;
				}

			}

			// analyze remaining positions
			for (int i = pos; i <= reference.length(); i++) {
				if (positions.containsKey(pos) && pos <= reference.length()) {
					callVariant(writerRaw, writerVar, file.getName(), level, i, positions.get(i), reference, freqFile);
				}
				positions.remove(i);
			}
			reader.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}

		monitor.worked(1);
		writerVar.close();
		writerRaw.close();

	}

	// monitor.done();

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

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
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

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
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

}
