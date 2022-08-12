package genepi.mut.pileup;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import genepi.mut.objects.VariantLine;
import genepi.mut.objects.VariantResult;
import genepi.mut.util.StatUtil;

public class VariantCaller {

	public enum Filter {
		PASS, BLACKLISTED, STRAND_BIAS, LLR_ERROR, DETECTION_LIMIT;
	}

	private static Set<Integer> blacklist = new HashSet<Integer>(
			Arrays.asList(301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 315, 316, 3105, 3106, 3107, 16182));

	public static int VARIANT = 1;

	public static int LOW_LEVEL_VARIANT = 2;

	public static int DELETION = 4;

	public static int INSERTION = 5;

	public static boolean isFinalVariant(VariantLine line) {

		if (line.getVariantType() == VariantCaller.VARIANT
				|| line.getVariantType() == VariantCaller.LOW_LEVEL_VARIANT) {
			return true;
		}

		if (line.getVariantType() == VariantCaller.DELETION) {
			return true;
		}

		return false;
	}

	public static VariantResult determineVariants(VariantLine line) {

		if (!line.isInsertion()) {

			if (line.getBayesBase() != '-' && line.getBayesProbability() > 0.8 && line.getBayesBase() != line.getRef()
					&& ((line.getCovFWD() + line.getCovREV()) >= 2)) {

				int type = VARIANT;

				// TODO currently ignored for homoplasmies since bayes only includes A,C,G,T
				if (line.getBayesBase() == 'D') {
					type = DELETION;
				}
				return addHomoplasmyResult(line, type);

			}
		} else {
			if (line.getTopBaseFWD() == line.getTopBaseREV() && line.getTopBaseFWD() != line.getRef()
					&& ((line.getCovFWD() + line.getCovREV() / 2) >= 70)) {
				int type = INSERTION;
				return addHomoplasmyResult(line, type);
			}
		}
		return null;
	}

	public static VariantResult determineLowLevelVariant(VariantLine line, double minorBasePercentsFWD,
			double minorBasePercentsREV, double llrFwd, double llrRev, double level, char minor) {

		int type = 0;

		Filter filter = null;
		try {

			/**
			 * 10× coverage of qualified bases on both positive and negative strands;
			 */

			if (!checkCoverage(line)) {
				return null;
			}

			/**
			 * all alleles have support from at least two reads on each strand
			 **/
			if (!checkAlleleCoverage(line, minorBasePercentsFWD, minorBasePercentsREV)) {
				return null;
			}

			/**
			 * the raw frequency for the minor allele is no less than 1% on one of the
			 * strands
			 **/
			if (!(minorBasePercentsFWD >= level || minorBasePercentsREV >= level)) {
				filter = Filter.DETECTION_LIMIT;
				line.setFilter(filter);
				return addVariantResult(line, type, filter);
			}

			/**
			 * high-confidence heteroplasmy was defined as candidate heteroplasmy with LLR
			 * no less than 5
			 **/
			if (!(llrFwd >= 5 || llrRev >= 5)) {
				filter = Filter.LLR_ERROR;
				line.setFilter(filter);
				return addVariantResult(line, type, filter);
			}

			if (!(calcStrandBias(line, minorBasePercentsFWD, minorBasePercentsREV) <= 1)) {
				type = LOW_LEVEL_VARIANT;
				filter = Filter.STRAND_BIAS;
				line.setFilter(filter);
				return addVariantResult(line, type, filter);
			}

			line.setFilter(Filter.PASS);
			type = LOW_LEVEL_VARIANT;
			return addVariantResult(line, type);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	private static VariantResult addVariantResult(VariantLine line, int type) {
		return addVariantResult(line, type, null);
	}

	private static VariantResult addVariantResult(VariantLine line, int type, Filter filter) {

		VariantResult output = new VariantResult();
		output.setId(line.getId());

		if (line.getInsPosition() != null) {
			output.setPosition(line.getInsPosition());
		} else {
			output.setPosition(line.getPosition() + "");
		}

		output.setTop(line.getTopBaseFWD());
		output.setMinor(line.getMinorBaseFWD());
		output.setRef(line.getRef());
		output.setCovFWD(line.getCovFWD());
		output.setCovREV(line.getCovREV());
		output.setType(type);

		if (filter == null) {
			filter = Filter.PASS;
		}

		if (filter == null && blacklist.contains(line.getPosition())) {
			filter = Filter.BLACKLISTED;
		}
		output.setFilter(filter);

		return output;
	}

	private static VariantResult addHomoplasmyResult(VariantLine line, int type) {

		VariantResult output = new VariantResult();
		output.setId(line.getId());

		if (line.getInsPosition() != null) {
			output.setPosition(line.getInsPosition());
		} else {
			output.setPosition(line.getPosition() + "");
		}

		if (type == 1) {
			output.setTop(line.getBayesBase());
			output.setLevel(calcLevel(line, line.getBayesPercentageFWD(), line.getBayesPercentageREV()));
		} else {
			output.setTop(line.getTopBaseFWD());
			output.setLevel(calcLevel(line, line.getMinorBasePercentsFWD(), line.getMinorBasePercentsREV()));
		}

		output.setMinor('-');
		output.setRef(line.getRef());
		output.setCovFWD(line.getCovFWD());
		output.setCovREV(line.getCovREV());
		output.setType(type);

		Filter filter = Filter.PASS;
		if (blacklist.contains(line.getPosition())) {
			filter = Filter.BLACKLISTED;
		}
		output.setFilter(filter);

		return output;
	}

	public static double calcVariantLevel(VariantLine line, double minorPercentFWD, double minorPercentREV) {

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

	public static double calcLevel(VariantLine line, double percentFWD, double percentREV) {

		double fwd = percentFWD * line.getCovFWD();
		double rev = percentREV * line.getCovREV();

		return (fwd + rev) / (line.getCovFWD() + line.getCovREV());
	}


	private static boolean checkAlleleCoverage(VariantLine line, double minorPercentFWD, double minorPercentREV) {
		int coverage = 2;

		if (line.getTopBasePercentsREV() * line.getCovREV() <= coverage
				&& (line.getTopBasePercentsFWD() * line.getCovFWD()) <= coverage) {
			return false;
		}

		if ((minorPercentREV * line.getCovREV() <= coverage)
				&& (minorPercentFWD * line.getCovFWD()) <= coverage) {
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

		df.setMaximumFractionDigits(3);

		df.setGroupingUsed(false);

		StringBuilder build = new StringBuilder();

		build.append(result.getId() + "\t");

		build.append(result.getFilter() + "\t");

		build.append(result.getPosition() + "\t");

		build.append(result.getRef() + "\t");

		build.append(getVariantBase(result) + "\t");

		build.append(df.format(result.getLevel()) + "\t");

		build.append(result.getTop() + "\t");

		if (result.getType() != 1) {
			build.append(df.format(result.getLevelTop()) + "\t");
		} else {
			build.append("" + "\t");
		}

		build.append(result.getMinor() + "\t");

		if (result.getType() != 1) {
			build.append(df.format(result.getLevelMinor()) + "\t");
		} else {
			build.append("" + "\t");
		}

		build.append((result.getCovFWD() + result.getCovREV()) + "\t");

		build.append((result.getCovFWD()) + "\t");

		build.append((result.getCovREV()) + "\t");

		build.append(result.getType());

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
