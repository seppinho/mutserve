package genepi.vcbox.lowlevelDetector;

import genepi.io.table.writer.CsvTableWriter;
import genepi.io.text.LineWriter;
import genepi.vcbox.objects.PositionObject;
import genepi.vcbox.util.Helper;
import genepi.vcbox.util.StatUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;

public class DetectVariants {

	private String hdfsFolder = "";
	private double heteroplasmy;
	private String outputRaw;
	private String outputFiltered;
	private String outputHSD;
	private String outputHSDCheck;
	private String outSummary;
	private String variants;

	NumberFormat df;

	BufferedReader bufferedReaderNumts;

	public static String refAsString;

	public static int HETEROPLASMY = 1; // reliable heteroplasmy

	public static int DOUBT_HETEROPLASMY = 2; // doubt heteroplasmy

	public DetectVariants(String fasta) {

		DetectVariants.refAsString = Helper.readInReference(fasta);
		df = DecimalFormat.getInstance();
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(4);

	}

	public boolean analyzeReads() {

		try {

			FileSystem fileSystem = FileSystem.get(new Configuration());
			FileStatus[] files = fileSystem.listStatus(new Path(hdfsFolder));

			Text line = new Text();
			TreeSet<PositionObject> lowlevelPos = new TreeSet<PositionObject>();
			CsvTableWriter rawWriter = new CsvTableWriter(outputRaw, '\t', false);
			// TODO CHANGE LLR BACK TO D
			rawWriter.setColumns(new String[] { "SampleID", "POS", "REF", "TOP-BASE-FWD", "MINOR-BASE-FWD", "TOP-BASE-REV",
					"MINOR-BASE-REV", "COV-FWD", "COV-REV", "TYPE", "HET-LEVEL", "%A", "%C", "%G", "%T", "%d", "%a", "%c",
					"%g", "%t", "%d", "LLRFWD", "LLRREV" });
			
			CsvTableWriter variantsWriter = new CsvTableWriter(outputFiltered, '\t', false);
			variantsWriter.setColumns(
					new String[] { "SampleID", "POS", "REF", "VARIANT", "COV-FWD", "COV-REV", "TYPE", "HET-LEVEL" });

			HashMap<String, TreeSet<String>> variantMap = new HashMap<String, TreeSet<String>>();

			for (FileStatus file : files) {

				if (!file.isDir()) {

					FSDataInputStream hadoopStream = fileSystem.open(file.getPath());
					LineReader hdfsReader = new LineReader(hadoopStream);

					while (hdfsReader.readLine(line, 500) > 0) {

						/** parse each line */
						PositionObject pos = new PositionObject(line.toString());

						if (pos.getPosition() > 0 && pos.getPosition() <= refAsString.length()) {

							determineLowLevelVariant(pos);

							if (pos.isHeteroplasmy() == 0) {
								determineVariant(variantMap, pos);
							} else {
								lowlevelPos.add(pos);
							}

							if (!pos.isInsertion()) {
								writeRaw(rawWriter, pos);
							}

						}
					}

					hdfsReader.close();
					hadoopStream.close();
				}
			}

			rawWriter.close();

			/** write heteroplasmy file */
			for (PositionObject obj : lowlevelPos) {
				writeVariants(variantsWriter, obj);
			}

			variantsWriter.close();

			/** write variants */
			writeVariantsOld(variantMap);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public void determineLowLevelVariant(PositionObject posObj) {

		double minorBasePercentsFWD = posObj.getMinorPercentsFWD();
		double minorBasePercentsREV = posObj.getMinorBasePercentsREV();
		try {

			/**
			 * 10× coverage of qualified bases on both positive and negative
			 * strands;
			 */
			if (checkCoverage(posObj)) {

				if (checkBases(posObj)) {

					if (checkDeletion(posObj)) {

						/**
						 * all alleles have support from at least two reads on
						 * each strand
						 **/
						if (checkAlleles(posObj)) {

							/**
							 * the raw frequency for the minor allele is no less
							 * than 1% on both strands
							 **/
							if (minorBasePercentsFWD >= heteroplasmy || minorBasePercentsREV >= heteroplasmy) {

								/**
								 * high-confidence heteroplasmy was defined as
								 * candidate heteroplasmy with LLR no less than
								 * 5
								 **/
								if (posObj.getLlrFWD() >= 5 || posObj.getLlrREV() >= 5) {

									if (calcStrandBias(posObj) <= 1) {
										//
										double fwd = minorBasePercentsFWD * posObj.getCovFWD();
										double rev = minorBasePercentsREV * posObj.getCovREV();

										posObj.setHeteroplasmy(HETEROPLASMY);

										posObj.setHetLevel((fwd + rev) / (posObj.getCovFWD() + posObj.getCovREV()));

										calcConfidence(posObj);

										if (!equalBase(posObj)) {
											posObj.setHeteroplasmy(DOUBT_HETEROPLASMY);
										}

									}
								}
							}
						}
					}

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void determineVariant(HashMap<String, TreeSet<String>> map, PositionObject posObj) {

		char ref = refAsString.charAt(posObj.getPosition() - 1);

		if (posObj.getTopBaseFWD() == posObj.getTopBaseREV()) {
			if (posObj.getTopBaseFWD() != ref && (posObj.getCovFWD() * posObj.getCovREV() / 2) > 10 * 2) {

				if (posObj.getTopBaseFWD() == 'd') {
					posObj.setDeletion(true);
				} else {
					posObj.setVariant(true);
				}
			}
		}

		TreeSet<String> set;
		if (posObj.isVariant()) {
			if (!map.containsKey(posObj.getId())) {
				set = new TreeSet<String>();
			} else {
				set = map.get(posObj.getId());
			}
			set.add(posObj.getPosition() + "\t" + ref + "\t" + posObj.getTopBaseFWD() + "\t"
					+ Integer.valueOf((int) (posObj.getTopBasePercentsFWD() * posObj.getCovFWD())) + " / "
					+ posObj.getCovFWD() + "\t"
					+ Integer.valueOf((int) (posObj.getTopBasePercentsREV() * posObj.getCovREV())) + " / "
					+ posObj.getCovREV());
			map.put(posObj.getId(), set);
		}
	}

	private double calcStrandBias(PositionObject posObj) {

		// b,d minor
		// a,c major

		double a = posObj.getTopBasePercentsFWD() * posObj.getCovFWD();
		double c = posObj.getTopBasePercentsREV() * posObj.getCovREV();
		double b = posObj.getMinorBasePercentsFWD() * posObj.getCovFWD();
		double d = posObj.getMinorBasePercentsREV() * posObj.getCovREV();

		double bias = Math.abs((b / (a + b)) - (d / (c + d))) / ((b + d) / (a + b + c + d));

		return bias;
	}

	private boolean equalBase(PositionObject posObj) {
		return posObj.getTopBaseFWD() == posObj.getTopBaseREV() && posObj.getMinorBaseFWD() == posObj.getMinorBaseREV();
	}

	private boolean checkBases(PositionObject posObj) {
		return (posObj.getMinorBaseFWD() == posObj.getMinorBaseREV()
				&& posObj.getTopBaseFWD() == posObj.getTopBaseREV())
				|| ((posObj.getMinorBaseFWD() == posObj.getTopBaseREV()
						&& posObj.getTopBaseFWD() == posObj.getMinorBaseREV()));
	}

	private boolean checkDeletion(PositionObject posOb) {

		if (posOb.getTopBaseREV() != 'd' && posOb.getMinorBaseREV() != '-') {
			return true;
		}
		if (posOb.getTopBaseFWD() != 'd' && posOb.getMinorBaseFWD() != '-') {
			return true;
		}
		return false;
	}

	private boolean checkAlleles(PositionObject posObj) {
		if (posObj.getTopBasePercentsREV() * posObj.getCovREV() < 3
				|| (posObj.getTopBasePercentsFWD() * posObj.getCovFWD()) < 3) {
			return false;
		}

		if ((posObj.getMinorBasePercentsREV() * posObj.getCovREV() < 3)
				|| (posObj.getTopBasePercentsFWD() * posObj.getCovFWD()) < 3) {
			return false;
		}

		return true;
	}

	private boolean checkCoverage(PositionObject posObj) {
		if (posObj.getCovREV() < 10 || posObj.getCovFWD() < 10) {
			return false;
		}

		return true;
	}

	private void writeRaw(CsvTableWriter writer, PositionObject posObj) {

		char ref = refAsString.charAt(posObj.getPosition() - 1);

		try {

			writer.setString(0, posObj.getId());

			writer.setInteger(1, posObj.getPosition());

			writer.setString(2, ref + "");

			writer.setString(3, posObj.getTopBaseFWD() + "");

			writer.setString(4, posObj.getMinorBaseFWD() + "");

			writer.setString(5, posObj.getTopBaseREV() + "");

			writer.setString(6, posObj.getMinorBaseREV() + "");

			writer.setInteger(7, posObj.getCovFWD());

			writer.setInteger(8, posObj.getCovREV());

			writer.setString(9, posObj.isHeteroplasmy() + "");

			writer.setString(10, df.format(posObj.getHetLevel()) + "");

			writer.setString(11, df.format(posObj.getaPercentageFWD()) + "");

			writer.setString(12, df.format(posObj.getcPercentageFWD()) + "");

			writer.setString(13, df.format(posObj.getgPercentageFWD()) + "");

			writer.setString(14, df.format(posObj.gettPercentageFWD()) + "");

			writer.setString(15, df.format(posObj.getdPercentageFWD()) + "");

			writer.setString(16, df.format(posObj.getaPercentageREV()) + "");

			writer.setString(17, df.format(posObj.getcPercentageREV()) + "");

			writer.setString(18, df.format(posObj.getgPercentageREV()) + "");

			writer.setString(19, df.format(posObj.gettPercentageREV()) + "");

			writer.setString(20, df.format(posObj.getdPercentageFWD()) + "");

			writer.setDouble(21, posObj.getLlrFWD());

			writer.setDouble(22, posObj.getLlrREV());

			// writer.setString(23, getBoundaryFWD(posObj));

			// writer.setString(24, getBoundaryREV(posObj));

			writer.next();

		} catch (Exception e) {

			e.printStackTrace();
		} catch (Error e) {

			e.printStackTrace();
		}

	}

	private void writeVariants(CsvTableWriter writer, PositionObject posObj) {

		char ref = refAsString.charAt(posObj.getPosition() - 1);

		try {

			writer.setString(0, posObj.getId());

			writer.setInteger(1, posObj.getPosition());

			writer.setString(2, ref + "");

			if (posObj.getTopBaseFWD() == ref) {
				writer.setString(3, posObj.getMinorBaseFWD() + "");
			} else {
				writer.setString(3, posObj.getTopBaseFWD() + "");
			}
			writer.setInteger(4, posObj.getCovFWD());

			writer.setInteger(5, posObj.getCovREV());

			writer.setString(6, posObj.isHeteroplasmy() + "");

			writer.setString(7, df.format(posObj.getHetLevel()) + "");

			writer.next();

		} catch (Exception e) {

			e.printStackTrace();
		} catch (Error e) {

			e.printStackTrace();
		}

	}
	
	private void writeVariantsOld(HashMap<String, TreeSet<String>> map) throws IOException {
		LineWriter varWriter = new LineWriter(variants);
		varWriter.write("SampleID\tPOS\tREF\tVARIANT\tCOV-FWD\tCOV-REV");

		for (String sampleId : map.keySet()) {
			TreeSet<String> set = map.get(sampleId);
			StringBuilder c = new StringBuilder();
			for (String poly : set) {
				c.append(sampleId + "\t" + poly + "\n");
			}
			varWriter.write(c.toString());
		}
		varWriter.close();
	}

	private void calcConfidence(PositionObject posObj) {
		if ((posObj.getCovFWD() * posObj.getCovREV() / 2) < 40) {
			generateWilsonInterval(posObj);
		} else {
			generateAgrestiInterval(posObj);
		}
	}

	private void generateAgrestiInterval(PositionObject posObj) {

		// here the input is just the base coverage
		double covBaseFWD = (posObj.getMinorBasePercentsFWD() * posObj.getCovFWD());
		double covBaseREV = (posObj.getMinorBasePercentsREV() * posObj.getCovREV());

		double lowFWD = StatUtil.CIAC_LOW(covBaseFWD, posObj.getCovFWD());
		double upFWD = StatUtil.CIAC_UP(covBaseFWD, posObj.getCovFWD());

		posObj.setCIAC_LOW_FWD(lowFWD);
		posObj.setCIAC_UP_FWD(upFWD);

		double lowREV = StatUtil.CIAC_LOW(covBaseREV, posObj.getCovREV());
		double upREV = StatUtil.CIAC_UP(covBaseREV, posObj.getCovREV());

		posObj.setCIAC_LOW_REV(lowREV);
		posObj.setCIAC_UP_REV(upREV);

	}

	private void generateWilsonInterval(PositionObject posObj) {

		// p is read depth of variant allele divided by the
		// complete coverage
		double p1 = ((posObj.getMinorBasePercentsFWD() * posObj.getCovFWD()) / posObj.getCovFWD());
		double p2 = ((posObj.getMinorBasePercentsREV() * posObj.getCovREV()) / posObj.getCovREV());

		double lowFWD = StatUtil.CIW_LOW(p1, posObj.getCovFWD());
		double upFWD = StatUtil.CIW_UP(p1, posObj.getCovFWD());

		posObj.setCIW_LOW_FWD(lowFWD);
		posObj.setCIW_UP_FWD(upFWD);

		double lowREV = StatUtil.CIW_LOW(p2, posObj.getCovREV());
		double upREV = StatUtil.CIW_UP(p2, posObj.getCovREV());

		posObj.setCIW_LOW_REV(lowREV);
		posObj.setCIW_UP_REV(upREV);

	}

	private String getBoundaryFWD(PositionObject posObj) {

		if ((posObj.getCovFWD() + posObj.getCovREV() / 2 < 40)) {
			return "[ " + df.format(posObj.getCIW_LOW_FWD()) + "/" + df.format(posObj.getCIW_UP_FWD()) + "]";
		} else {
			return "[ " + df.format(posObj.getCIAC_LOW_FWD()) + "/" + df.format(posObj.getCIAC_UP_FWD()) + "]";
		}
	}

	private String getBoundaryREV(PositionObject posObj) {

		if ((posObj.getCovFWD() + posObj.getCovREV() / 2 < 40)) {
			return "[ " + df.format(posObj.getCIW_LOW_REV()) + "/" + df.format(posObj.getCIW_UP_REV()) + "]";
		} else {
			return "[ " + df.format(posObj.getCIAC_LOW_REV()) + "/" + df.format(posObj.getCIAC_UP_REV()) + "]";
		}
	}

	public double getHeteroplasmy() {
		return heteroplasmy;
	}

	public void setHeteroplasmy(double heteroplasmy) {
		this.heteroplasmy = heteroplasmy;
	}

	public String getOutputRaw() {
		return outputRaw;
	}

	public void setOutputRaw(String outputRaw) {
		this.outputRaw = outputRaw;
	}

	public String getOutputFiltered() {
		return outputFiltered;
	}

	public void setOutputFiltered(String outputFiltered) {
		this.outputFiltered = outputFiltered;
	}

	public String getOutputHSD() {
		return outputHSD;
	}

	public void setOutputHSD(String outputHSD) {
		this.outputHSD = outputHSD;
	}

	public String getHdfsFolder() {
		return hdfsFolder;
	}

	public void setHdfsFolder(String hdfsFolder) {
		this.hdfsFolder = hdfsFolder;
	}

	public String getOutputHSDCheck() {
		return outputHSDCheck;
	}

	public void setOutputHSDCheck(String outputHSDCheck) {
		this.outputHSDCheck = outputHSDCheck;
	}

	public String getOutSummary() {
		return outSummary;
	}

	public void setOutSummary(String outSummary) {
		this.outSummary = outSummary;
	}

	public String getVariants() {
		return variants;
	}

	public void setVariants(String variants) {
		this.variants = variants;
	}

}
