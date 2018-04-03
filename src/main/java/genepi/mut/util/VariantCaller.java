package genepi.mut.util;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import genepi.mut.objects.VariantLine;
import genepi.mut.objects.VariantResult;

public class VariantCaller {

	public static int VARIANT = 1;

	public static int LOW_LEVEL_VARIANT = 2;

	public static int LOW_LEVEL_DELETION = 3;

	public static int DELETION = 4;

	public static boolean isFinalVariant(VariantLine line) {

		if (line.getVariantType() == VariantCaller.VARIANT
				|| line.getVariantType() == VariantCaller.LOW_LEVEL_VARIANT) {
			return true;
		}

		if (line.getVariantType() == VariantCaller.LOW_LEVEL_DELETION) {
			return true;
		}

		return false;
	}

	public static VariantResult determineVariants(VariantLine line) {

		int type = 0;

		if (line.getTopBaseFWD() == line.getTopBaseREV()) {

			if (line.getTopBaseFWD() != line.getRef() && ((line.getCovFWD() + line.getCovREV() / 2) >= 30)) {

				if (line.getTopBaseFWD() == 'd') {
					type = DELETION;
				} else {
					type = VARIANT;
				}
			}

		}

		return addVariantResult(line, type);
	}

	public static VariantResult determineLowLevelVariant(VariantLine line, double minorBasePercentsFWD,
			double minorBasePercentsREV, double llrFwd, double llrRev, double level) {

		int type = 0;

		try {

			/**
			 * 10× coverage of qualified bases on both positive and negative
			 * strands;
			 */

			if (checkCoverage(line)) {

				// included in parse method
				// if (checkBases(line)) {

				/**
				 * all alleles have support from at least two reads on each
				 * strand
				 **/
				if (checkAlleleCoverage(line, minorBasePercentsFWD, minorBasePercentsREV)) {

					/**
					 * the raw frequency for the minor allele is no less than 1%
					 * on one of the strands
					 **/
					if (minorBasePercentsFWD >= level || minorBasePercentsREV >= level) {

						/**
						 * high-confidence heteroplasmy was defined as candidate
						 * heteroplasmy with LLR no less than 5
						 **/
						if (llrFwd >= 5 || llrRev >= 5) {

							if (calcStrandBias(line, minorBasePercentsFWD, minorBasePercentsREV) <= 1) {

								// D can either be on TOP or MINOR base
								if (line.getMinorBaseFWD() == 'D' || line.getTopBaseFWD() == 'D') {
									type = LOW_LEVEL_DELETION;
								} else {
									type = LOW_LEVEL_VARIANT;
								}

								return addVariantResult(line, type);
								// calcConfidence(line);

							}
						}
						// }
					}
				}
			} else {
				line.setMessage("Position coverage not sufficient. No model can be applied");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return addVariantResult(line, type);

	}

	private static VariantResult addVariantResult(VariantLine line, int type) {
		VariantResult output = new VariantResult();

		output.setId(line.getId());
		output.setPosition(line.getPosition());
		output.setTop(line.getTopBaseFWD());
		output.setMinor(line.getMinorBaseFWD());
		output.setRef(line.getRef());
		output.setCovFWD(line.getCovFWD());
		output.setCovREV(line.getCovREV());
		output.setType(type);

		return output;
	}

	public static double calcLevel(VariantLine line, double minorPercentFWD, double minorPercentREV) {

		double fwd;
		double rev;

		if (line.getTopBaseFWD() == line.getRef()) {
			fwd = minorPercentFWD * line.getCovFWD();
			rev = minorPercentREV * line.getCovREV();
		} else {
			fwd = line.getTopBasePercentsFWD() * line.getCovFWD();
			rev = line.getTopBasePercentsREV() * line.getCovREV();
		}

		return (fwd + rev) / (line.getCovFWD() + line.getCovREV());
	}

	private static boolean checkAlleleCoverage(VariantLine line, double minorPercentFWD, double minorPercentREV) {
		if (line.getTopBasePercentsREV() * line.getCovREV() < 3
				|| (line.getTopBasePercentsFWD() * line.getCovFWD()) < 3) {
			return false;
		}

		if ((minorPercentREV * line.getCovREV() < 3) || (line.getTopBasePercentsFWD() * line.getCovFWD()) < 3) {
			return false;
		}

		return true;
	}

	private static boolean checkCoverage(VariantLine line) {
		if (line.getCovREV() < 10 || line.getCovFWD() < 10) {
			return false;
		}

		return true;
	}

	public static double getMinorPercentageFwd(VariantLine line, char minor) {

		double minorFWD = 0;

		if (minor == 'A') {
			minorFWD = line.getaPercentageFWD();
		}
		if (minor == 'C') {
			minorFWD = line.getcPercentageFWD();
		}
		if (minor == 'G') {
			minorFWD = line.getgPercentageFWD();
		}
		if (minor == 'T') {
			minorFWD = line.gettPercentageFWD();
		}
		if (minor == 'D') {
			minorFWD = line.getdPercentageFWD();
		}
		return minorFWD;
	}

	public static double getMinorPercentageRev(VariantLine line, char minor) {

		double minorREV = 0;

		if (minor == 'A') {
			minorREV = line.getaPercentageREV();
		}
		if (minor == 'C') {
			minorREV = line.getcPercentageREV();
		}
		if (minor == 'G') {
			minorREV = line.getgPercentageREV();
		}
		if (minor == 'T') {
			minorREV = line.gettPercentageREV();
		}
		if (minor == 'D') {
			minorREV = line.getdPercentageREV();
		}
		return minorREV;
	}

	public static double determineLlrFwd(VariantLine line, char minor) {

		double llrFwd = 0;

		if (minor == 'A') {
			llrFwd = line.getLlrAFWD();
		}
		if (minor == 'C') {
			llrFwd = line.getLlrCFWD();
		}
		if (minor == 'G') {
			llrFwd = line.getLlrGFWD();
		}
		if (minor == 'T') {
			llrFwd = line.getLlrTFWD();
		}
		if (minor == 'D') {
			llrFwd = line.getLlrDFWD();
		}
		return llrFwd;
	}

	public static double determineLlrRev(VariantLine line, char minor) {

		double llrRev = 0;

		if (minor == 'A') {
			llrRev = line.getLlrAREV();
		}
		if (minor == 'C') {
			llrRev = line.getLlrCREV();
		}
		if (minor == 'G') {
			llrRev = line.getLlrGREV();
		}
		if (minor == 'T') {
			llrRev = line.getLlrTREV();
		}
		if (minor == 'D') {
			llrRev = line.getLlrDREV();
		}
		return llrRev;
	}

	private static double calcStrandBias(VariantLine line, double minorPercentFWD, double minorPercentREV) {

		// b,d minor
		// a,c major

		double a = line.getTopBasePercentsFWD() * line.getCovFWD();
		double c = line.getTopBasePercentsREV() * line.getCovREV();
		double b = minorPercentFWD * line.getCovFWD();
		double d = minorPercentREV * line.getCovREV();

		double bias = Math.abs((b / (a + b)) - (d / (c + d))) / ((b + d) / (a + b + c + d));

		return bias;
	}

	private static void calcConfidence(VariantLine line) {
		if ((line.getCovFWD() * line.getCovREV() / 2) < 40) {
			generateWilsonInterval(line);
		} else {
			generateAgrestiInterval(line);
		}
	}

	private static void generateAgrestiInterval(VariantLine line) {

		// here the input is just the base coverage
		double covBaseFWD = (line.getMinorBasePercentsFWD() * line.getCovFWD());
		double covBaseREV = (line.getMinorBasePercentsREV() * line.getCovREV());

		double lowFWD = StatUtil.CIAC_LOW(covBaseFWD, line.getCovFWD());
		double upFWD = StatUtil.CIAC_UP(covBaseFWD, line.getCovFWD());

		line.setCIAC_LOW_FWD(lowFWD);
		line.setCIAC_UP_FWD(upFWD);

		double lowREV = StatUtil.CIAC_LOW(covBaseREV, line.getCovREV());
		double upREV = StatUtil.CIAC_UP(covBaseREV, line.getCovREV());

		line.setCIAC_LOW_REV(lowREV);
		line.setCIAC_UP_REV(upREV);

	}

	private static void generateWilsonInterval(VariantLine line) {

		// p is read depth of variant allele divided by the
		// complete coverage
		double p1 = ((line.getMinorBasePercentsFWD() * line.getCovFWD()) / line.getCovFWD());
		double p2 = ((line.getMinorBasePercentsREV() * line.getCovREV()) / line.getCovREV());

		double lowFWD = StatUtil.CIW_LOW(p1, line.getCovFWD());
		double upFWD = StatUtil.CIW_UP(p1, line.getCovFWD());

		line.setCIW_LOW_FWD(lowFWD);
		line.setCIW_UP_FWD(upFWD);

		double lowREV = StatUtil.CIW_LOW(p2, line.getCovREV());
		double upREV = StatUtil.CIW_UP(p2, line.getCovREV());

		line.setCIW_LOW_REV(lowREV);
		line.setCIW_UP_REV(upREV);

	}

	public static String writeVariant(VariantResult result) throws IOException {

		NumberFormat df;

		Locale.setDefault(new Locale("en", "US"));

		df = DecimalFormat.getInstance(Locale.US);

		df.setMinimumFractionDigits(2);

		df.setMaximumFractionDigits(4);

		df.setGroupingUsed(false);

		StringBuilder build = new StringBuilder();

		build.setLength(0);

		build.append(result.getId() + "\t");

		build.append(result.getPosition() + "\t");

		build.append(result.getRef() + "\t");

		build.append(getVariantBase(result) + "\t");

		build.append(result.getTop() + "/" + result.getMinor() + "\t");

		build.append(df.format(result.getLevel()) + "\t");

		build.append(result.getCovFWD() + "\t");

		build.append(result.getCovREV() + "\t");

		build.append(result.getCovFWD() + result.getCovREV());

		/*
		 * if (getInsPosition() != null) {
		 * 
		 * build.append("\t" + this.getInsPosition());
		 * 
		 * }
		 */

		build.append("\r");

		return build.toString();

	};

	private static char getVariantBase(VariantResult line) {

		if (line.getTop() == line.getRef()) {

			return line.getMinor();

		} else {

			return line.getTop();

		}
	}

}
