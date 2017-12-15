package genepi.mut.objects;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import genepi.io.table.reader.CsvTableReader;
import genepi.mut.util.StatUtil;

public class VariantLine implements Comparable<VariantLine> {

	public static int VARIANT = 1;

	public static int LOW_LEVEL_VARIANT = 2;

	public static int LOW_LEVEL_DELETION = 3;

	public static int DELETION = 4;

	public static int INSERTION = 5;

	public static int MULTI_ALLELIC = 6;

	private String id;
	private int position;
	private char ref;
	private int covFWD;
	private int covREV;

	private double llrFWD;
	private double llrREV;

	private char topBaseFWD;
	private char topBaseREV;
	private char minorBaseFWD;
	private char minorBaseREV;
	private double aPercentageFWD;
	private double cPercentageFWD;
	private double gPercentageFWD;
	private double tPercentageFWD;
	private double nPercentageFWD;
	private double dPercentageFWD;
	private double aPercentageREV;
	private double cPercentageREV;
	private double gPercentageREV;
	private double tPercentageREV;
	private double nPercentageREV;
	private double dPercentageREV;

	private double llrAFWD;
	private double llrCFWD;
	private double llrGFWD;
	private double llrTFWD;

	private String insPosition;

	private double llrAREV;
	private double llrCREV;
	private double llrGREV;
	private double llrTREV;

	private double llrDFWD;
	private double llrDREV;

	private double topBasePercentsFWD;
	private double minorBasePercentsFWD;
	private double topBasePercentsREV;
	private double minorBasePercentsREV;

	private String message;

	private int type = 0;
	private double varLevel = 0.0;
	private boolean fwdOK = false;
	private boolean revOK = false;
	private boolean isInsertion = false;
	private boolean isVariant = false;
	private boolean oneSideVariant = false;
	private boolean isDeletion = false;
	private boolean isRevVariant = false;
	private double CIW_LOW_FWD;
	private double CIW_UP_FWD;
	private double CIW_LOW_REV;
	private double CIW_UP_REV;
	private double CIAC_LOW_FWD;
	private double CIAC_UP_FWD;
	private double CIAC_LOW_REV;
	private double CIAC_UP_REV;
	NumberFormat df;
	private String multiAllelic;

	public VariantLine() {
		Locale.setDefault(new Locale("en", "US"));
		df = DecimalFormat.getInstance(Locale.US);
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(4);
		df.setGroupingUsed(false);
	}

	public void parseLineFromFile(CsvTableReader cloudgeneReader) {

		this.setId(cloudgeneReader.getString("SAMPLE"));
		this.setPosition(cloudgeneReader.getInteger("POS"));
		this.setRef(cloudgeneReader.getString("REF").charAt(0));

		this.setLlrFWD(cloudgeneReader.getDouble("LLRFWD"));
		this.setLlrREV(cloudgeneReader.getDouble("LLRREV"));

		this.setCovFWD(cloudgeneReader.getInteger("COV-FWD"));
		this.setCovREV(cloudgeneReader.getInteger("COV-REV"));

		this.setLlrAFWD(cloudgeneReader.getDouble("LLRAFWD"));
		this.setLlrCFWD(cloudgeneReader.getDouble("LLRCFWD"));
		this.setLlrGFWD(cloudgeneReader.getDouble("LLRGFWD"));
		this.setLlrTFWD(cloudgeneReader.getDouble("LLRTFWD"));

		this.setLlrAREV(cloudgeneReader.getDouble("LLRAREV"));
		this.setLlrCREV(cloudgeneReader.getDouble("LLRCREV"));
		this.setLlrGREV(cloudgeneReader.getDouble("LLRGREV"));
		this.setLlrTREV(cloudgeneReader.getDouble("LLRTREV"));

		this.setLlrDFWD(cloudgeneReader.getDouble("LLRDFWD"));
		this.setLlrDREV(cloudgeneReader.getDouble("LLRDREV"));

		this.setaPercentageFWD(cloudgeneReader.getDouble("%A"));
		this.setcPercentageFWD(cloudgeneReader.getDouble("%C"));
		this.setgPercentageFWD(cloudgeneReader.getDouble("%G"));
		this.settPercentageFWD(cloudgeneReader.getDouble("%T"));

		this.setaPercentageREV(cloudgeneReader.getDouble("%a"));
		this.setcPercentageREV(cloudgeneReader.getDouble("%c"));
		this.setgPercentageREV(cloudgeneReader.getDouble("%g"));
		this.settPercentageREV(cloudgeneReader.getDouble("%t"));

		this.setTopBasePercentsFWD(cloudgeneReader.getDouble("TOP-FWD-PERCENT"));
		this.setMinorBasePercentsFWD(cloudgeneReader.getDouble("MINOR-FWD-PERCENT"));

		this.setTopBasePercentsREV(cloudgeneReader.getDouble("TOP-REV-PERCENT"));
		this.setMinorBasePercentsREV(cloudgeneReader.getDouble("MINOR-REV-PERCENT"));

		this.setTopBaseFWD(cloudgeneReader.getString("TOP-FWD").charAt(0));
		this.setTopBaseREV(cloudgeneReader.getString("TOP-REV").charAt(0));
		this.setMinorBaseFWD(cloudgeneReader.getString("MINOR-FWD").charAt(0));
		this.setMinorBaseREV(cloudgeneReader.getString("MINOR-REV").charAt(0));

	}

	public void analysePosition(BasePosition base, double level) throws IOException {

		double aFWDPercents = 0;
		double cFWDPercents = 0;
		double gFWDPercents = 0;
		double tFWDPercents = 0;
		double nFWDPercents = 0;
		double dFWDPercents = 0;
		double aREVPercents = 0;
		double cREVPercents = 0;
		double gREVPercents = 0;
		double tREVPercents = 0;
		double nREVPercents = 0;
		double dREVPercents = 0;
		int pos;
		String id;

		id = base.getId();
		pos = base.getPos();

		int aFWD = base.getaFor();
		int cFWD = base.getcFor();
		int gFWD = base.getgFor();
		int tFWD = base.gettFor();

		int aREV = base.getaRev();
		int cREV = base.getcRev();
		int gREV = base.getgRev();
		int tREV = base.gettRev();

		int dFWD = base.getdFor();
		int dREV = base.getdRev();

		int nFWD = base.getnFor();
		int nREV = base.getnRev();

		int totalFWD = aFWD + cFWD + gFWD + tFWD + dFWD;
		int totalREV = aREV + cREV + gREV + tREV + dREV;

		if (totalFWD > 0) {
			aFWDPercents = aFWD / (double) totalFWD;
			cFWDPercents = cFWD / (double) totalFWD;
			gFWDPercents = gFWD / (double) totalFWD;
			tFWDPercents = tFWD / (double) totalFWD;
			dFWDPercents = dFWD / (double) totalFWD;
			nFWDPercents = nFWD / (double) (totalFWD + nFWD);
		}

		if (totalREV > 0) {
			aREVPercents = aREV / (double) totalREV;
			cREVPercents = cREV / (double) totalREV;
			gREVPercents = gREV / (double) totalREV;
			tREVPercents = tREV / (double) totalREV;
			dREVPercents = dREV / (double) totalREV;
			nREVPercents = nREV / (double) (totalREV + nREV);
		}

		ArrayList<Integer> allelesFWD = new ArrayList<Integer>();
		allelesFWD.add(aFWD);
		allelesFWD.add(cFWD);
		allelesFWD.add(gFWD);
		allelesFWD.add(tFWD);
		allelesFWD.add(dFWD);

		Collections.sort(allelesFWD, Collections.reverseOrder());
		double topBasePercentsFWD = 0.0;
		double minorBasePercentsFWD = 0.0;

		if (totalFWD > 0) {

			topBasePercentsFWD = allelesFWD.get(0) / (double) totalFWD;

			minorBasePercentsFWD = allelesFWD.get(1) / (double) totalFWD;

		}

		ArrayList<Integer> allelesREV = new ArrayList<Integer>();
		allelesREV.add(aREV);
		allelesREV.add(cREV);
		allelesREV.add(gREV);
		allelesREV.add(tREV);
		allelesREV.add(dREV);

		Collections.sort(allelesREV, Collections.reverseOrder());
		double topBasePercentsREV = 0.0;
		double minorBasePercentsREV = 0.0;

		if (totalREV > 0) {
			topBasePercentsREV = allelesREV.get(0) / (double) totalREV;

			minorBasePercentsREV = allelesREV.get(1) / (double) totalREV;

		}

		char topBaseFWD = '-';
		char minorBaseFWD = '-';
		// prefer lexicographic order in case of 50/50
		if (topBasePercentsFWD == minorBasePercentsFWD && topBasePercentsFWD > 0) {
			if (aFWDPercents == gFWDPercents && gFWDPercents == topBasePercentsFWD) {
				topBaseFWD = 'A';
				minorBaseFWD = 'G';
			} else if (aFWDPercents == cFWDPercents && cFWDPercents == topBasePercentsFWD) {
				topBaseFWD = 'A';
				minorBaseFWD = 'C';
			} else if (aFWDPercents == tFWDPercents && tFWDPercents == topBasePercentsFWD) {
				topBaseFWD = 'A';
				minorBaseFWD = 'T';
			} else if (cFWDPercents == tFWDPercents && tFWDPercents == topBasePercentsFWD) {
				topBaseFWD = 'C';
				minorBaseFWD = 'T';
			} else if (cFWDPercents == gFWDPercents && gFWDPercents == topBasePercentsFWD) {
				topBaseFWD = 'C';
				minorBaseFWD = 'G';
			} else if (gFWDPercents == tFWDPercents && tFWDPercents == topBasePercentsFWD) {
				topBaseFWD = 'G';
				minorBaseFWD = 'T';
			}
		}

		else if (aFWD >= cFWD && aFWD >= gFWD && aFWD >= tFWD && aFWD >= dFWD && aFWD > 0) {
			topBaseFWD = 'A';
		}

		else if (cFWD >= aFWD && cFWD >= gFWD && cFWD >= tFWD && cFWD >= dFWD && cFWD > 0) {
			topBaseFWD = 'C';
		}

		else if (gFWD >= cFWD && gFWD >= aFWD && gFWD >= tFWD && gFWD >= dFWD && gFWD > 0) {
			topBaseFWD = 'G';
		}

		else if (tFWD >= cFWD && tFWD >= gFWD && tFWD >= aFWD && tFWD >= dFWD && tFWD > 0) {
			topBaseFWD = 'T';
		}

		else if (dFWD >= cFWD && dFWD >= gFWD && dFWD >= aFWD && dFWD >= tFWD && dFWD > 0) {
			topBaseFWD = 'D';
		}

		char topBaseREV = '-';
		char minorBaseREV = '-';

		if (topBasePercentsREV == minorBasePercentsREV) {
			if (aREVPercents == gREVPercents && gREVPercents == topBasePercentsREV) {
				topBaseREV = 'A';
				minorBaseREV = 'G';
			}
			if (aREVPercents == cREVPercents && cREVPercents == topBasePercentsREV) {
				topBaseREV = 'A';
				minorBaseREV = 'C';
			}
			if (aREVPercents == tREVPercents && tREVPercents == topBasePercentsREV) {
				topBaseREV = 'A';
				minorBaseREV = 'T';
			}
			if (cREVPercents == tREVPercents && tREVPercents == topBasePercentsREV) {
				topBaseREV = 'C';
				minorBaseREV = 'T';
			}
			if (cREVPercents == gREVPercents && gREVPercents == topBasePercentsREV) {
				topBaseREV = 'C';
				minorBaseREV = 'G';
			}
			if (gREVPercents == tREVPercents && tREVPercents == topBasePercentsREV) {
				topBaseREV = 'G';
				minorBaseREV = 'T';
			}
		}

		else if (aREV >= cREV && aREV >= gREV && aREV >= tREV && aREV >= dREV && aREV > 0) {
			topBaseREV = 'A';
		}

		else if (cREV >= aREV && cREV >= gREV && cREV >= tREV && cREV >= dREV && cREV > 0) {
			topBaseREV = 'C';
		}

		else if (gREV >= cREV && gREV >= aREV && gREV >= tREV && gREV >= dREV && gREV > 0) {
			topBaseREV = 'G';
		}

		else if (tREV >= cREV && tREV >= gREV && tREV >= aREV && tREV >= dREV && tREV > 0) {
			topBaseREV = 'T';
		}

		else if (dREV >= cREV && dREV >= gREV && dREV >= aREV && dREV >= tREV && dREV > 0) {
			topBaseREV = 'D';
		}

		if (minorBasePercentsFWD > 0 && minorBasePercentsFWD < 0.5) {

			if (minorBasePercentsFWD == aFWDPercents) {
				minorBaseFWD = 'A';
			}

			else if (minorBasePercentsFWD == cFWDPercents) {
				minorBaseFWD = 'C';
			}

			else if (minorBasePercentsFWD == gFWDPercents) {
				minorBaseFWD = 'G';
			}

			else if (minorBasePercentsFWD == tFWDPercents) {
				minorBaseFWD = 'T';
			} else if (minorBasePercentsFWD == dFWDPercents) {
				minorBaseFWD = 'D';
			}

		}

		if (minorBasePercentsREV > 0 && minorBasePercentsREV < 0.5) {

			if (minorBasePercentsREV == aREVPercents) {
				minorBaseREV = 'A';
			}

			else if (minorBasePercentsREV == cREVPercents) {
				minorBaseREV = 'C';
			}

			else if (minorBasePercentsREV == gREVPercents) {
				minorBaseREV = 'G';
			}

			else if (minorBasePercentsREV == tREVPercents) {
				minorBaseREV = 'T';
			}

			else if (minorBasePercentsREV == dREVPercents) {
				minorBaseREV = 'D';
			}

		}

		// set attributes
		this.setId(id);
		this.setPosition(pos);

		this.setCovFWD(totalFWD);
		this.setCovREV(totalREV);

		this.setTopBaseFWD(topBaseFWD);
		this.setTopBaseREV(topBaseREV);
		this.setMinorBaseFWD(minorBaseFWD);
		this.setMinorBaseREV(minorBaseREV);

		this.setaPercentageFWD(aFWDPercents);
		this.setcPercentageFWD(cFWDPercents);
		this.setgPercentageFWD(gFWDPercents);
		this.settPercentageFWD(tFWDPercents);
		this.setnPercentageFWD(nFWDPercents);
		this.setdPercentageFWD(dFWDPercents);

		this.setaPercentageREV(aREVPercents);
		this.setcPercentageREV(cREVPercents);
		this.setgPercentageREV(gREVPercents);
		this.settPercentageREV(tREVPercents);
		this.setnPercentageREV(nREVPercents);
		this.setdPercentageREV(dREVPercents);

		this.setTopBasePercentsFWD(topBasePercentsFWD);
		this.setMinorBasePercentsFWD(minorBasePercentsFWD);

		this.setTopBasePercentsREV(topBasePercentsREV);
		this.setMinorBasePercentsREV(minorBasePercentsREV);

		// TODO combine this with LLR for all bases
		if (minorBasePercentsFWD >= level || minorBasePercentsREV >= level) {
			LlrObject llr = calcLlr(base, getMinorBaseFWD(), getMinorBaseREV());
			this.setLlrFWD(llr.getLlrFWD());
			this.setLlrREV(llr.getLlrREV());
		}

		if (getTopBaseFWD() != 'A') {
			LlrObject llr = calcLlr(base, 'A');
			this.setLlrAFWD(llr.getLlrFWD());
			this.setLlrAREV(llr.getLlrREV());
		}

		if (getTopBaseFWD() != 'C') {
			LlrObject llr = calcLlr(base, 'C');
			this.setLlrCFWD(llr.getLlrFWD());
			this.setLlrCREV(llr.getLlrREV());
		}

		if (getTopBaseFWD() != 'G') {
			LlrObject llr = calcLlr(base, 'G');
			this.setLlrGFWD(llr.getLlrFWD());
			this.setLlrGREV(llr.getLlrREV());
		}

		if (getTopBaseFWD() != 'T') {
			LlrObject llr = calcLlr(base, 'T');
			this.setLlrTFWD(llr.getLlrFWD());
			this.setLlrTREV(llr.getLlrREV());
		}

		if (getTopBaseFWD() != 'D') {
			LlrObject llr = calcLlr(base, 'D');
			this.setLlrDFWD(llr.getLlrFWD());
			this.setLlrDREV(llr.getLlrREV());
		}

	}

	private LlrObject calcLlr(BasePosition base, char minorBaseFWD) {
		return calcLlr(base, minorBaseFWD, minorBaseFWD);
	}

	private LlrObject calcLlr(BasePosition base, char minorBaseFWD, char minorBaseREV) {

		LlrObject llr = new LlrObject();
		double fm0FWD = calcFirst(base);
		double fm1FWD = calcFirst(base) + calcSecond(base, minorBaseFWD);

		double fm0REV = calcFirstRev(base);
		double fm1REV = calcFirstRev(base) + calcSecondR(base, minorBaseREV);

		llr.setLlrFWD(Math.abs(fm1FWD - fm0FWD));
		llr.setLlrREV(Math.abs(fm1REV - fm0REV));

		return llr;
	}

	@Override
	public int compareTo(VariantLine o) {

		if (id.equals(o.getId())) {
			return position > o.getPosition() ? 1 : -1;
		} else {
			return id.compareTo(o.getId());
		}
	}

	public String toRawString() {
		return id + "\t" + position + "\t" + ref + "\t" + topBaseFWD + "\t" + minorBaseFWD + "\t" + topBaseREV + "\t"
				+ minorBaseREV + "\t" + covFWD + "\t" + covREV + "\t" + (covFWD + covREV) + "\t" + type + "\t"
				+ varLevel + "\t" + aPercentageFWD + "\t" + cPercentageFWD + "\t" + gPercentageFWD + "\t"
				+ tPercentageFWD + "\t" + dPercentageFWD + "\t" + nPercentageFWD + "\t" + aPercentageREV + "\t"
				+ cPercentageREV + "\t" + gPercentageREV + "\t" + tPercentageREV + "\t" + dPercentageREV + "\t"
				+ nPercentageREV + "\t" + topBasePercentsFWD + "\t" + topBasePercentsREV + "\t" + minorBasePercentsFWD
				+ "\t" + minorBasePercentsREV + "\t" + llrFWD + "\t" + llrREV + "\t" + llrAFWD + "\t" + llrCFWD + "\t"
				+ llrGFWD + "\t" + llrTFWD + "\t" + llrAREV + "\t" + llrCREV + "\t" + llrGREV + "\t" + llrTREV + "\t"
				+ llrDFWD + "\t" + llrDREV;

	}

	public double calcFirst(BasePosition base) {
		char major = getTopBaseFWD();
		double tmp = 0;
		double f = getTopBasePercentsFWD();
		switch (major) {
		case 'A':
			int aFWD = base.getaFor();
			for (int i = 0; i < aFWD; i++) {
				byte err = 20;
				err = base.getaForQ().get(i);
				double p = Math.pow(10, (-err / 10));
				tmp = majorCalc(tmp, f, p);
			}

			break;
		case 'C':
			int cFWD = base.getcFor();
			for (int i = 0; i < cFWD; i++) {
				Byte err = base.getcForQ().get(i);
				double p = Math.pow(10, (-err / 10));
				tmp = majorCalc(tmp, f, p);
			}
			break;
		case 'G':
			int gFWD = base.getgFor();
			for (int i = 0; i < gFWD; i++) {
				Byte err = base.getgForQ().get(i);
				double p = Math.pow(10, (-err / 10));
				tmp = majorCalc(tmp, f, p);
			}
			break;
		case 'T':
			int tFWD = base.gettFor();
			for (int i = 0; i < tFWD; i++) {
				Byte err = base.gettForQ().get(i);
				double p = Math.pow(10, (-err / 10));
				tmp = majorCalc(tmp, f, p);
			}
			break;
		case 'D':
			int dFWD = base.getdFor();
			for (int i = 0; i < dFWD; i++) {
				Byte err = base.getdForQ().get(i);
				double p = Math.pow(10, (-err / 10));
				tmp = majorCalc(tmp, f, p);
			}
			break;
		default:
			break;
		}
		return tmp;
	}

	public double calcSecond(BasePosition base, char baseChar) {
		char minor = baseChar;
		double tmp = 0;
		double f = getTopBasePercentsFWD();
		switch (minor) {
		case 'A':
			int aFWD = base.getaFor();
			for (int i = 0; i < aFWD; i++) {
				Byte err = base.getaForQ().get(i);
				double p = Math.pow(10, (-err / 10));
				tmp = minorCalc(tmp, f, p);
			}
			break;
		case 'C':
			int cFWD = base.getcFor();
			for (int i = 0; i < cFWD; i++) {
				Byte err = base.getcForQ().get(i);
				double p = Math.pow(10, (-err / 10));
				tmp = minorCalc(tmp, f, p);
			}
			break;
		case 'G':
			int gFWD = base.getgFor();
			for (int i = 0; i < gFWD; i++) {
				Byte err = base.getgForQ().get(i);
				double p = Math.pow(10, (-err / 10));
				tmp = minorCalc(tmp, f, p);
			}
			break;
		case 'T':
			int tFWD = base.gettFor();
			for (int i = 0; i < tFWD; i++) {
				Byte err = base.gettForQ().get(i);
				double p = Math.pow(10, (-err / 10));
				tmp = minorCalc(tmp, f, p);
			}
			break;
		case 'D':
			int dFWD = base.getdFor();
			for (int i = 0; i < dFWD; i++) {
				Byte err = base.getdForQ().get(i);
				double p = Math.pow(10, (-err / 10));
				tmp = minorCalc(tmp, f, p);
			}
			break;
		default:
			break;
		}
		return tmp;
	}

	private double majorCalc(double tmp, double f, double p) {
		return tmp + Math.log(((1 - f) * p + f * (1 - p)));
	}

	private double minorCalc(double tmp, double f, double p) {
		return tmp + Math.log(((1 - f) * (1 - p) + (f * p)));
	}

	public double calcFirstRev(BasePosition base) {
		char major = getTopBaseREV();
		double tmp = 0;
		double f = getTopBasePercentsREV();
		switch (major) {
		case 'A':
			int aREV = base.getaRev();
			for (int i = 0; i < aREV; i++) {
				Byte quality = base.getaRevQ().get(i);
				double p = Math.pow(10, (-quality / 10));
				tmp = majorCalc(tmp, f, p);
			}
			break;
		case 'C':
			int cREV = base.getcRev();
			for (int i = 0; i < cREV; i++) {
				Byte quality = base.getcRevQ().get(i);
				double p = Math.pow(10, (-quality / 10));
				tmp = majorCalc(tmp, f, p);
			}
			break;
		case 'G':
			int gREV = base.getgRev();
			for (int i = 0; i < gREV; i++) {
				Byte quality = base.getgRevQ().get(i);
				double p = Math.pow(10, (-quality / 10));
				tmp = majorCalc(tmp, f, p);
			}
			break;
		case 'T':
			int tREV = base.gettRev();
			for (int i = 0; i < tREV; i++) {
				Byte quality = base.gettRevQ().get(i);
				double p = Math.pow(10, (-quality / 10));
				tmp = majorCalc(tmp, f, p);
			}
			break;
		case 'D':
			int dREV = base.getdRev();
			for (int i = 0; i < dREV; i++) {
				Byte quality = base.getdRevQ().get(i);
				double p = Math.pow(10, (-quality / 10));
				tmp = majorCalc(tmp, f, p);
			}
			break;
		default:
			break;
		}
		return tmp;
	}

	public double calcSecondR(BasePosition base, char baseChar) {
		char minor = baseChar;
		double tmp = 0;
		double f = getTopBasePercentsREV();
		switch (minor) {
		case 'A':
			int aREV = base.getaRev();
			for (int i = 0; i < aREV; i++) {
				Byte quality = base.getaRevQ().get(i);
				double p = Math.pow(10, (-quality / 10));
				tmp = minorCalc(tmp, f, p);
			}
			break;
		case 'C':
			int cREV = base.getcRev();
			for (int i = 0; i < cREV; i++) {
				Byte quality = base.getcRevQ().get(i);
				double p = Math.pow(10, (-quality / 10));
				tmp = minorCalc(tmp, f, p);
			}
			break;
		case 'G':
			int gREV = base.getgRev();
			for (int i = 0; i < gREV; i++) {
				Byte quality = base.getgRevQ().get(i);
				double p = Math.pow(10, (-quality / 10));
				tmp = minorCalc(tmp, f, p);
			}
			break;
		case 'T':
			int tREV = base.gettRev();
			for (int i = 0; i < tREV; i++) {
				Byte quality = base.gettRevQ().get(i);
				double p = Math.pow(10, (-quality / 10));
				tmp = minorCalc(tmp, f, p);
			}
			break;
		case 'D':
			int dREV = base.getdRev();
			for (int i = 0; i < dREV; i++) {
				Byte quality = base.getdRevQ().get(i);
				double p = Math.pow(10, (-quality / 10));
				tmp = minorCalc(tmp, f, p);
			}
			break;
		default:
			break;
		}
		return tmp;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getnPercentageFWD() {
		return nPercentageFWD;
	}

	public void setnPercentageFWD(double nPercentageFWD) {
		this.nPercentageFWD = nPercentageFWD;
	}

	public double getaPercentageREV() {
		return aPercentageREV;
	}

	public void setaPercentageREV(double aPercentageREV) {
		this.aPercentageREV = aPercentageREV;
	}

	public double getcPercentageREV() {
		return cPercentageREV;
	}

	public void setcPercentageREV(double cPercentageREV) {
		this.cPercentageREV = cPercentageREV;
	}

	public double getgPercentageREV() {
		return gPercentageREV;
	}

	public void setgPercentageREV(double gPercentageREV) {
		this.gPercentageREV = gPercentageREV;
	}

	public double gettPercentageREV() {
		return tPercentageREV;
	}

	public void settPercentageREV(double tPercentageREV) {
		this.tPercentageREV = tPercentageREV;
	}

	public double getnPercentageREV() {
		return nPercentageREV;
	}

	public void setnPercentageREV(double nPercentageREV) {
		this.nPercentageREV = nPercentageREV;
	}

	public double getdPercentageFWD() {
		return dPercentageFWD;
	}

	public void setdPercentageFWD(double dPercentageFWD) {
		this.dPercentageFWD = dPercentageFWD;
	}

	public double getdPercentageREV() {
		return dPercentageREV;
	}

	public void setdPercentageREV(double dPercentageREV) {
		this.dPercentageREV = dPercentageREV;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public double getaPercentageFWD() {
		return aPercentageFWD;
	}

	public void setaPercentageFWD(double aPercentageFWD) {
		this.aPercentageFWD = aPercentageFWD;
	}

	public double getcPercentageFWD() {
		return cPercentageFWD;
	}

	public void setcPercentageFWD(double cPercentageFWD) {
		this.cPercentageFWD = cPercentageFWD;
	}

	public double getgPercentageFWD() {
		return gPercentageFWD;
	}

	public void setgPercentageFWD(double gPercentageFWD) {
		this.gPercentageFWD = gPercentageFWD;
	}

	public double gettPercentageFWD() {
		return tPercentageFWD;
	}

	public void settPercentageFWD(double tPercentageFWD) {
		this.tPercentageFWD = tPercentageFWD;
	}

	public int getCovFWD() {
		return covFWD;
	}

	public void setCovFWD(int covFWD) {
		this.covFWD = covFWD;
	}

	public int getCovREV() {
		return covREV;
	}

	public void setCovREV(int covREV) {
		this.covREV = covREV;
	}

	public double getTopBasePercentsFWD() {
		return topBasePercentsFWD;
	}

	public void setTopBasePercentsFWD(double topBasePercentsFWD) {
		this.topBasePercentsFWD = topBasePercentsFWD;
	}

	public double getMinorPercentsFWD() {
		return minorBasePercentsFWD;
	}

	public void setMinorBasePercentsFWD(double minorBasePercentsFWD) {
		this.minorBasePercentsFWD = minorBasePercentsFWD;
	}

	public double getTopBasePercentsREV() {
		return topBasePercentsREV;
	}

	public void setTopBasePercentsREV(double topBasePercentsREV) {
		this.topBasePercentsREV = topBasePercentsREV;
	}

	public char getTopBaseFWD() {
		return topBaseFWD;
	}

	public void setTopBaseFWD(char posFWD) {
		this.topBaseFWD = posFWD;
	}

	public char getTopBaseREV() {
		return topBaseREV;
	}

	public void setTopBaseREV(char posREV) {
		this.topBaseREV = posREV;
	}

	public boolean isVariant() {
		return isVariant;
	}

	public void setVariant(boolean isVariant) {
		this.isVariant = isVariant;
	}

	public int getVariantType() {
		return type;
	}

	public void setVariantType(int isHeteroplasmy) {
		this.type = isHeteroplasmy;
	}

	public char getMinorBaseFWD() {
		return minorBaseFWD;
	}

	public void setMinorBaseFWD(char minorBaseFWD) {
		this.minorBaseFWD = minorBaseFWD;
	}

	public char getMinorBaseREV() {
		return minorBaseREV;
	}

	public void setMinorBaseREV(char minorBaseREV) {
		this.minorBaseREV = minorBaseREV;
	}

	public double getVariantLevel() {
		return varLevel;
	}

	public void setVariantLevel(double hetLevelFWD) {
		this.varLevel = hetLevelFWD;
	}

	public double getMinorBasePercentsREV() {
		return minorBasePercentsREV;
	}

	public void setMinorBasePercentsREV(double minorBasePercentsREV) {
		this.minorBasePercentsREV = minorBasePercentsREV;
	}

	public double getMinorBasePercentsFWD() {
		return minorBasePercentsFWD;
	}

	public double getCIW_LOW_FWD() {
		return CIW_LOW_FWD;
	}

	public void setCIW_LOW_FWD(double cIW_LOW_FWD) {
		CIW_LOW_FWD = cIW_LOW_FWD;
	}

	public double getCIW_UP_FWD() {
		return CIW_UP_FWD;
	}

	public void setCIW_UP_FWD(double cIW_UP_FWD) {
		CIW_UP_FWD = cIW_UP_FWD;
	}

	public double getCIAC_LOW_REV() {
		return CIAC_LOW_REV;
	}

	public void setCIAC_LOW_REV(double cIAC_LOW_REV) {
		CIAC_LOW_REV = cIAC_LOW_REV;
	}

	public double getCIAC_UP_REV() {
		return CIAC_UP_REV;
	}

	public void setCIAC_UP_REV(double cIAC_UP_REV) {
		CIAC_UP_REV = cIAC_UP_REV;
	}

	public double getCIW_LOW_REV() {
		return CIW_LOW_REV;
	}

	public void setCIW_LOW_REV(double cIW_LOW_REV) {
		CIW_LOW_REV = cIW_LOW_REV;
	}

	public double getCIW_UP_REV() {
		return CIW_UP_REV;
	}

	public void setCIW_UP_REV(double cIW_UP_REV) {
		CIW_UP_REV = cIW_UP_REV;
	}

	public double getCIAC_LOW_FWD() {
		return CIAC_LOW_FWD;
	}

	public void setCIAC_LOW_FWD(double cIAC_LOW_FWD) {
		CIAC_LOW_FWD = cIAC_LOW_FWD;
	}

	public double getCIAC_UP_FWD() {
		return CIAC_UP_FWD;
	}

	public void setCIAC_UP_FWD(double cIAC_UP_FWD) {
		CIAC_UP_FWD = cIAC_UP_FWD;
	}

	public boolean isFwdOK() {
		return fwdOK;
	}

	public void setFwdOK(boolean fwdOK) {
		this.fwdOK = fwdOK;
	}

	public boolean isRevOK() {
		return revOK;
	}

	public void setRevOK(boolean revOK) {
		this.revOK = revOK;
	}

	public boolean isInsertion() {
		return isInsertion;
	}

	public void setInsertion(boolean isInsertion) {
		this.isInsertion = isInsertion;
	}

	public boolean isRevVariant() {
		return isRevVariant;
	}

	public void setRevVariant(boolean isRevVariant) {
		this.isRevVariant = isRevVariant;
	}

	public boolean isOneSideVariant() {
		return oneSideVariant;
	}

	public void setOneSideVariant(boolean oneSideVariant) {
		this.oneSideVariant = oneSideVariant;
	}

	public boolean isDeletion() {
		return isDeletion;
	}

	public void setDeletion(boolean isDeletion) {
		this.isDeletion = isDeletion;
	}

	public double getLlrFWD() {
		return llrFWD;
	}

	public void setLlrFWD(double llrFWD) {
		this.llrFWD = llrFWD;
	}

	public double getLlrREV() {
		return llrREV;
	}

	public void setLlrREV(double llrREV) {
		this.llrREV = llrREV;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public double getLlrAFWD() {
		return llrAFWD;
	}

	public void setLlrAFWD(double llrAFWD) {
		this.llrAFWD = llrAFWD;
	}

	public double getLlrCFWD() {
		return llrCFWD;
	}

	public void setLlrCFWD(double llrCFWD) {
		this.llrCFWD = llrCFWD;
	}

	public double getLlrGFWD() {
		return llrGFWD;
	}

	public void setLlrGFWD(double llrGFWD) {
		this.llrGFWD = llrGFWD;
	}

	public double getLlrTFWD() {
		return llrTFWD;
	}

	public void setLlrTFWD(double llrTFWD) {
		this.llrTFWD = llrTFWD;
	}

	public double getLlrAREV() {
		return llrAREV;
	}

	public void setLlrAREV(double llrAREV) {
		this.llrAREV = llrAREV;
	}

	public double getLlrCREV() {
		return llrCREV;
	}

	public void setLlrCREV(double llrCREV) {
		this.llrCREV = llrCREV;
	}

	public double getLlrGREV() {
		return llrGREV;
	}

	public void setLlrGREV(double llrGREV) {
		this.llrGREV = llrGREV;
	}

	public double getLlrTREV() {
		return llrTREV;
	}

	public void setLlrTREV(double llrTREV) {
		this.llrTREV = llrTREV;
	}

	public String getMultiAllelic() {
		return multiAllelic;
	}

	public void setMultiAllelic(String multiAllelic) {
		this.multiAllelic = multiAllelic;
	}

	public String writeVariant() throws IOException {

		StringBuilder build = new StringBuilder();

		build.setLength(0);

		build.append(this.getId() + "\t");

		build.append(this.getPosition() + "\t");

		build.append(this.ref + "\t");

		build.append(getVariantBase(this) + "\t");

		build.append(this.getTopBaseFWD() + "/" + this.getMinorBaseFWD() + "\t");

		build.append(df.format(getVariantLevel()) + "\t");

		build.append(this.getCovFWD() + "\t");

		build.append(this.getCovREV() + "\t");

		build.append(this.getCovFWD() + this.getCovREV());

		if (getInsPosition() != null) {

			build.append("\t" + this.getInsPosition());

		}

		build.append("\r");

		return build.toString();

	};

	public void determineVariants() {

		if (this.getTopBaseFWD() == this.getTopBaseREV()) {

			if (this.getTopBaseFWD() != this.ref && (this.getCovFWD() * this.getCovREV() / 2) > 10 * 2) {

				if (this.getTopBaseFWD() == 'd') {
					this.setDeletion(true);
					this.setVariantType(DELETION);
				} else {
					this.setVariant(true);
					this.setVariantType(VARIANT);
				}
			}
		}
	}

	public void callVariants(double level) {

		this.determineLowLevelVariant(level);

		// only execute if no low-level variant has been detected
		if (this.getVariantType() == 0) {

			if (this.getVariantLevel() > 1 - level) {

				this.determineVariants();

			}

		}

	}

	public void determineLowLevelVariant(double level) {

		double minorBasePercentsFWD = this.getMinorPercentsFWD();
		double minorBasePercentsREV = this.getMinorBasePercentsREV();
		try {

			/**
			 * 10× coverage of qualified bases on both positive and negative
			 * strands;
			 */

			//always calculate level to check for non-low-level variants later
			this.setVariantLevel(calcHetLevel());

			if (checkCoverage()) {

				if (checkBases()) {

					/**
					 * all alleles have support from at least two reads on each
					 * strand
					 **/
					if (checkAlleleCoverage()) {
						/**
						 * the raw frequency for the minor allele is no less
						 * than 1% on one of the strands
						 **/
						if (minorBasePercentsFWD >= level || minorBasePercentsREV >= level) {
							/**
							 * high-confidence heteroplasmy was defined as
							 * candidate heteroplasmy with LLR no less than 5
							 **/
							if (this.getLlrFWD() >= 5 || this.getLlrREV() >= 5) {

								if (calcStrandBias() <= 1) {

									// D can either be on TOP or MINOR base
									if (this.minorBaseFWD == 'D' || this.topBaseFWD == 'D') {
										this.setVariantType(LOW_LEVEL_DELETION);
									} else {
										this.setVariantType(LOW_LEVEL_VARIANT);
									}

									calcConfidence();

								}
							}
						}
					}
				}
			} else {
				this.setMessage("Position coverage not sufficient. No model can be applied");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private double calcStrandBias() {

		// b,d minor
		// a,c major

		double a = this.getTopBasePercentsFWD() * this.getCovFWD();
		double c = this.getTopBasePercentsREV() * this.getCovREV();
		double b = this.getMinorBasePercentsFWD() * this.getCovFWD();
		double d = this.getMinorBasePercentsREV() * this.getCovREV();

		double bias = Math.abs((b / (a + b)) - (d / (c + d))) / ((b + d) / (a + b + c + d));

		return bias;
	}

	private double calcHetLevel() {

		double fwd;
		double rev;

		if (this.getTopBaseFWD() == this.ref) {
			fwd = this.getMinorPercentsFWD() * this.getCovFWD();
			rev = this.getMinorBasePercentsREV() * this.getCovREV();
		} else {
			fwd = this.getTopBasePercentsFWD() * this.getCovFWD();
			rev = this.getTopBasePercentsREV() * this.getCovREV();
		}

		return (fwd + rev) / (this.getCovFWD() + this.getCovREV());
	}

	private boolean checkBases() {
		return (this.getMinorBaseFWD() == this.getMinorBaseREV() && this.getTopBaseFWD() == this.getTopBaseREV())
				|| ((this.getMinorBaseFWD() == this.getTopBaseREV() && this.getTopBaseFWD() == this.getMinorBaseREV()));
	}

	private boolean checkAlleleCoverage() {
		if (this.getTopBasePercentsREV() * this.getCovREV() < 3
				|| (this.getTopBasePercentsFWD() * this.getCovFWD()) < 3) {
			return false;
		}

		if ((this.getMinorBasePercentsREV() * this.getCovREV() < 3)
				|| (this.getTopBasePercentsFWD() * this.getCovFWD()) < 3) {
			return false;
		}

		return true;
	}

	private boolean checkCoverage() {
		if (this.getCovREV() < 10 || this.getCovFWD() < 10) {
			return false;
		}

		return true;
	}

	private void calcConfidence() {
		if ((this.getCovFWD() * this.getCovREV() / 2) < 40) {
			generateWilsonInterval();
		} else {
			generateAgrestiInterval();
		}
	}

	private char getVariantBase(VariantLine posObj) {

		if (posObj.getTopBaseFWD() == this.ref) {

			return posObj.getMinorBaseFWD();

		} else {

			return posObj.getTopBaseFWD();

		}
	}

	public boolean isFinalVariant() {

		if (this.getVariantType() == VariantLine.VARIANT || this.getVariantType() == VariantLine.LOW_LEVEL_VARIANT) {
			return true;
		}

		if (this.getVariantType() == VariantLine.LOW_LEVEL_DELETION) {
			return true;
		}

		return false;
	}

	private void generateAgrestiInterval() {

		// here the input is just the base coverage
		double covBaseFWD = (this.getMinorBasePercentsFWD() * this.getCovFWD());
		double covBaseREV = (this.getMinorBasePercentsREV() * this.getCovREV());

		double lowFWD = StatUtil.CIAC_LOW(covBaseFWD, this.getCovFWD());
		double upFWD = StatUtil.CIAC_UP(covBaseFWD, this.getCovFWD());

		this.setCIAC_LOW_FWD(lowFWD);
		this.setCIAC_UP_FWD(upFWD);

		double lowREV = StatUtil.CIAC_LOW(covBaseREV, this.getCovREV());
		double upREV = StatUtil.CIAC_UP(covBaseREV, this.getCovREV());

		this.setCIAC_LOW_REV(lowREV);
		this.setCIAC_UP_REV(upREV);

	}

	private void generateWilsonInterval() {

		// p is read depth of variant allele divided by the
		// complete coverage
		double p1 = ((this.getMinorBasePercentsFWD() * this.getCovFWD()) / this.getCovFWD());
		double p2 = ((this.getMinorBasePercentsREV() * this.getCovREV()) / this.getCovREV());

		double lowFWD = StatUtil.CIW_LOW(p1, this.getCovFWD());
		double upFWD = StatUtil.CIW_UP(p1, this.getCovFWD());

		this.setCIW_LOW_FWD(lowFWD);
		this.setCIW_UP_FWD(upFWD);

		double lowREV = StatUtil.CIW_LOW(p2, this.getCovREV());
		double upREV = StatUtil.CIW_UP(p2, this.getCovREV());

		this.setCIW_LOW_REV(lowREV);
		this.setCIW_UP_REV(upREV);

	}

	public char getRef() {
		return ref;
	}

	public void setRef(char ref) {
		this.ref = ref;
	}

	public double getLlrDFWD() {
		return llrDFWD;
	}

	public void setLlrDFWD(double llrDFWD) {
		this.llrDFWD = llrDFWD;
	}

	public double getLlrDREV() {
		return llrDREV;
	}

	public void setLlrDREV(double llrDREV) {
		this.llrDREV = llrDREV;
	}

	public String getInsPosition() {
		return insPosition;
	}

	public void setInsPosition(String insPosition) {
		this.insPosition = insPosition;
	}

}
