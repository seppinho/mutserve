package genepi.vcbox.detect;

import genepi.io.table.writer.CsvTableWriter;
import genepi.vcbox.objects.PositionObject;
import genepi.vcbox.util.ReferenceUtil;
import genepi.vcbox.util.StatUtil;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;

public class DetectVariants {

	private String hdfsFolder = "";
	private double detectionLevel = 0.01;
	private String outputRaw;
	private String outputFiltered;
	private String uncoveredPos;
	private String outSummary;

	NumberFormat df;

	private String refAsString;

	private String version;

	private static int VARIANT = 1; // variant

	private static int LOW_LEVEL_VARIANT = 2; // low level variant

	private static int SUSPICOUS_LOW_LEVEL_VARIANT = 3; // double check low
														// level
	private static int DELETION = 4; // deletion

	private static int INSERTION = 5; // deletion

	public DetectVariants() {

		df = DecimalFormat.getInstance();
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(4);

	}

	public boolean analyzeReads() {

		try {

			FileSystem fileSystem = FileSystem.get(new Configuration());
			FileStatus[] files = fileSystem.listStatus(new Path(hdfsFolder));

			Text line = new Text();
			List<PositionObject> variantPos = new ArrayList<PositionObject>();
			CsvTableWriter rawWriter = new CsvTableWriter(outputRaw, '\t', false);
			// TODO CHANGE LLR BACK TO D
			rawWriter.setColumns(new String[] { "SampleID", "Pos", "Ref", "Top-Base-FWD", "Minor-Base-FWD",
					"Top-Base-REV", "Minor-Base-REV", "Coverage-FWD", "Coverage-REV", "Coverage-Total", "Variant-Type",
					"Variant-Level", "%A", "%C", "%G", "%T", "%D", "%N", "%a", "%c", "%g", "%t", "%d", "%n" });

			List<PositionObject> uncoveredPosList = new ArrayList<PositionObject>();

			for (FileStatus file : files) {

				if (!file.isDir()) {

					FSDataInputStream hadoopStream = fileSystem.open(file.getPath());
					LineReader hdfsReader = new LineReader(hadoopStream);

					while (hdfsReader.readLine(line, 500) > 0) {

						/** parse each line */
						PositionObject pos = new PositionObject(line.toString());

						if (pos.getPosition() > 0 && pos.getPosition() <= refAsString.length()) {

							// write each pos; ignore insertions
							if (!pos.isInsertion()) {
								writePositionsFile(rawWriter, pos);
							}

							// detect low-level variants
							if (version.equals("mtdna")) {
								if (!ReferenceUtil.ismtDNAHotSpot(pos.getPosition()))
									determineLowLevelVariants(pos, uncoveredPosList);
							} else {
								determineLowLevelVariants(pos, uncoveredPosList);
							}

							// no low-level variant detected
							if (pos.getVariantType() == 0) {
								determineVariants(pos);
							}

							if (pos.getVariantType() == VARIANT || pos.getVariantType() == LOW_LEVEL_VARIANT
									|| pos.getVariantType() == SUSPICOUS_LOW_LEVEL_VARIANT) {
								variantPos.add(pos);
							}

						}
					}

					hdfsReader.close();
					hadoopStream.close();
				}
			}

			rawWriter.close();

			writeVariantsFile(variantPos);

			writeUncoveredPos(uncoveredPosList);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public void determineLowLevelVariants(PositionObject posObj, List<PositionObject> uncoveredPos) {

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
						if (checkAlleleCoverage(posObj)) {

							/**
							 * the raw frequency for the minor allele is no less
							 * than 1% on both strands
							 **/
							if (minorBasePercentsFWD >= detectionLevel || minorBasePercentsREV >= detectionLevel) {

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

										posObj.setVariantType(LOW_LEVEL_VARIANT);

										posObj.setVariantLevel((fwd + rev) / (posObj.getCovFWD() + posObj.getCovREV()));

										calcConfidence(posObj);

										if (!equalBase(posObj)) {
											posObj.setVariantType(SUSPICOUS_LOW_LEVEL_VARIANT);
										}

									}
								}
							}
						}
					}
				}
			} else {
				posObj.setMessage("Position coverage not sufficient. No model can be applied");
				uncoveredPos.add(posObj);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void writePositionsFile(CsvTableWriter writer, PositionObject posObj) {

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

			writer.setInteger(9, posObj.getCovFWD() + posObj.getCovREV());

			writer.setString(10, posObj.getVariantType() + "");

			writer.setString(11, df.format(posObj.getVariantLevel()) + "");

			writer.setString(12, df.format(posObj.getaPercentageFWD()) + "");

			writer.setString(13, df.format(posObj.getcPercentageFWD()) + "");

			writer.setString(14, df.format(posObj.getgPercentageFWD()) + "");

			writer.setString(15, df.format(posObj.gettPercentageFWD()) + "");

			writer.setString(16, df.format(posObj.getdPercentageFWD()) + "");

			writer.setString(17, df.format(posObj.getnPercentageFWD()) + "");

			writer.setString(18, df.format(posObj.getaPercentageREV()) + "");

			writer.setString(19, df.format(posObj.getcPercentageREV()) + "");

			writer.setString(20, df.format(posObj.getgPercentageREV()) + "");

			writer.setString(21, df.format(posObj.gettPercentageREV()) + "");

			writer.setString(22, df.format(posObj.getdPercentageREV()) + "");

			writer.setString(23, df.format(posObj.getnPercentageREV()) + "");

			writer.next();

		} catch (Exception e) {

			e.printStackTrace();
		} catch (Error e) {

			e.printStackTrace();
		}

	}

	private void writeVariantsFile(List<PositionObject> list) {

		CsvTableWriter writer = new CsvTableWriter(outputFiltered, '\t', false);
		writer.setColumns(new String[] { "SampleID", "Pos", "Ref", "Variant", "Coverage-FWD", "Coverage-REV",
				"Coverage-Total", "Variant-Type", "Variant-Level" });

		Collections.sort(list);

		for (PositionObject posObj : list) {

			char ref = refAsString.charAt(posObj.getPosition() - 1);

			writer.setString(0, posObj.getId());

			writer.setInteger(1, posObj.getPosition());

			writer.setString(2, ref + "");

			writer.setString(3, getVariantBase(posObj) + "");

			writer.setInteger(4, posObj.getCovFWD());

			writer.setInteger(5, posObj.getCovREV());

			writer.setInteger(6, posObj.getCovFWD() + posObj.getCovREV());

			writer.setString(7, posObj.getVariantType() + "");

			if (posObj.getVariantType() == 1) {

				writer.setString(8, "1.0");

			} else {

				writer.setString(8, df.format(posObj.getVariantLevel()) + "");

			}

			writer.next();

		}

		writer.close();

	}

	private void writeUncoveredPos(List<PositionObject> list) {

		CsvTableWriter writer = new CsvTableWriter(uncoveredPos, '\t', false);
		writer.setColumns(new String[] { "SampleID", "Pos", "Message" });

		Collections.sort(list);

		for (PositionObject posObj : list) {

			writer.setString(0, posObj.getId());

			writer.setInteger(1, posObj.getPosition());

			writer.setString(2, posObj.getMessage());

			writer.next();

		}

		writer.close();

	}

	public void determineVariants(PositionObject posObj) {

		char ref = refAsString.charAt(posObj.getPosition() - 1);

		if (posObj.getTopBaseFWD() == posObj.getTopBaseREV()) {

			if (posObj.getTopBaseFWD() != ref && (posObj.getCovFWD() * posObj.getCovREV() / 2) > 10 * 2) {

				if (posObj.getTopBaseFWD() == 'd') {
					posObj.setDeletion(true);
					posObj.setVariantType(DELETION);
				} else {
					posObj.setVariant(true);
					posObj.setVariantType(VARIANT);
				}
			}
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

	private boolean checkAlleleCoverage(PositionObject posObj) {
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

	private void calcConfidence(PositionObject posObj) {
		if ((posObj.getCovFWD() * posObj.getCovREV() / 2) < 40) {
			generateWilsonInterval(posObj);
		} else {
			generateAgrestiInterval(posObj);
		}
	}

	private char getVariantBase(PositionObject posObj) {

		char ref = refAsString.charAt(posObj.getPosition() - 1);

		if (posObj.getTopBaseFWD() == ref) {

			return posObj.getMinorBaseFWD();

		} else {

			return posObj.getTopBaseFWD();

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

	public double getDetectionLevel() {
		return detectionLevel;
	}

	public void setDetectionLevel(double detectionLevel) {
		this.detectionLevel = detectionLevel;
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

	public String getHdfsFolder() {
		return hdfsFolder;
	}

	public void setHdfsFolder(String hdfsFolder) {
		this.hdfsFolder = hdfsFolder;
	}

	public String getOutSummary() {
		return outSummary;
	}

	public void setOutSummary(String outSummary) {
		this.outSummary = outSummary;
	}

	public String getUncoveredPos() {
		return uncoveredPos;
	}

	public void setUncoveredPos(String uncoveredPos) {
		this.uncoveredPos = uncoveredPos;
	}

	public String getRefAsString() {
		return refAsString;
	}

	public void setRefAsString(String refAsString) {
		this.refAsString = refAsString;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
