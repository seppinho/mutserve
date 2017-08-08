package genepi.cnv.pileup;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import genepi.cnv.objects.BasePosition;
import genepi.cnv.objects.PositionObject;
import genepi.cnv.util.ReferenceUtil;
import genepi.cnv.util.StatUtil;
import genepi.hadoop.CacheStore;
import genepi.hadoop.HdfsUtil;
import genepi.hadoop.PreferenceStore;
import genepi.hadoop.io.HdfsLineWriter;

public class PileupReducer extends Reducer<Text, BasePosition, Text, Text> {

	private BasePosition posInput = new BasePosition();

	String reference;

	private static int VARIANT = 1;

	public static int LOW_LEVEL_VARIANT = 2; 

	public static int SUSPICOUS_LOW_LEVEL_VARIANT = 3;
														
	public static int DELETION = 4; 

	public static int INSERTION = 5; 

	public static int MULTI_ALLELIC = 6;

	String hdfsVariants;

	HdfsLineWriter writer;

	String version;
	
	NumberFormat df;

	protected void setup(Context context) throws IOException, InterruptedException {

		HdfsUtil.setDefaultConfiguration(context.getConfiguration());
		PreferenceStore store = new PreferenceStore(context.getConfiguration());
		version = store.getString("server.version");
		
		CacheStore cache = new CacheStore(context.getConfiguration());
		File referencePath = new File(cache.getArchive("reference"));
		String fastaPath = ReferenceUtil.findFileinDir(referencePath, ".fasta");
		reference = ReferenceUtil.readInReference(fastaPath);

		hdfsVariants = context.getConfiguration().get("variantsHdfs");
		HdfsUtil.create(hdfsVariants + "/" + context.getTaskAttemptID());
		writer = new HdfsLineWriter(hdfsVariants + "/" + context.getTaskAttemptID());
		Locale.setDefault(new Locale("en", "US"));
		df = DecimalFormat.getInstance(Locale.US);
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(4);
		df.setGroupingUsed(false);
		
	}

	protected void reduce(Text key, java.lang.Iterable<BasePosition> values, Context context)
			throws java.io.IOException, InterruptedException {
		
		posInput.clear();
		List<Byte> combinedAFor = new ArrayList<Byte>();
		List<Byte> combinedCFor = new ArrayList<Byte>();
		List<Byte> combinedGFor = new ArrayList<Byte>();
		List<Byte> combinedTFor = new ArrayList<Byte>();
		List<Byte> combinedARev = new ArrayList<Byte>();
		List<Byte> combinedCRev = new ArrayList<Byte>();
		List<Byte> combinedGRev = new ArrayList<Byte>();
		List<Byte> combinedTRev = new ArrayList<Byte>();

		for (BasePosition value : values) {

			posInput.add(value);
			combinedAFor.addAll(value.getaForQ());
			combinedCFor.addAll(value.getcForQ());
			combinedGFor.addAll(value.getgForQ());
			combinedTFor.addAll(value.gettForQ());
			combinedARev.addAll(value.getaRevQ());
			combinedCRev.addAll(value.getcRevQ());
			combinedGRev.addAll(value.getgRevQ());
			combinedTRev.addAll(value.gettRevQ());

		}

		posInput.setaForQ(combinedAFor);
		posInput.setcForQ(combinedCFor);
		posInput.setgForQ(combinedGFor);
		posInput.settForQ(combinedTFor);

		posInput.setaRevQ(combinedARev);
		posInput.setcRevQ(combinedCRev);
		posInput.setgRevQ(combinedGRev);
		posInput.settRevQ(combinedTRev);

		posInput.setId(key.toString().split(":")[0]);
		posInput.setPos(Integer.valueOf(key.toString().split(":")[1]));

		//calculate all metrics
		PositionObject positionObj = new PositionObject();
		positionObj.analysePosition(posInput);

		if (positionObj.getPosition() > 0 && positionObj.getPosition() <= reference.length()) {
		
		// write raw file
		char ref = reference.charAt(positionObj.getPosition() - 1);
		context.write(null, new Text(positionObj.writeRawFile(ref)));

		//write heteroplasmy files

			determineLowLevelVariant(positionObj);

			// only execute if no low-level variants have been detected
			if (positionObj.getVariantType() == 0) {
				determineVariants(positionObj);
			}

			if (positionObj.getVariantType() == VARIANT || positionObj.getVariantType() == LOW_LEVEL_VARIANT
					|| positionObj.getVariantType() == SUSPICOUS_LOW_LEVEL_VARIANT) {
				writer.write(writeVariant(positionObj));
			}
		} else {
			System.out.println("out of range: " + positionObj.getPosition());
		}

	}

	private String writeVariant(PositionObject posOut) throws IOException {
		StringBuilder build = new StringBuilder();
		build.setLength(0);
		char ref = reference.charAt(posOut.getPosition() - 1);

		build.append(posOut.getId() + "\t");
		build.append(posOut.getPosition() + "\t");
		build.append(ref + "\t");
		build.append(getVariantBase(posOut) + "\t");

		if (posOut.getVariantType() == 1) {
			build.append("-" + "\t");
			build.append("1.0" + "\t");
		} else {
			build.append(posOut.getTopBaseFWD() + "/" + posOut.getMinorBaseFWD() + "\t");
			build.append(df.format(posOut.getVariantLevel()) + "\t");
		}
		build.append(posOut.getCovFWD() + "\t");
		build.append(posOut.getCovREV() + "\t");
		build.append(posOut.getCovFWD() + posOut.getCovREV());

		return build.toString();
	};

	public void determineVariants(PositionObject posObj) {

		char ref = reference.charAt(posObj.getPosition() - 1);

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
						if (checkAlleleCoverage(posObj)) {
							/**
							 * the raw frequency for the minor allele is no less
							 * than 1% on both strands
							 **/
							if (minorBasePercentsFWD >= 0.01 || minorBasePercentsREV >= 0.01) {
								/**
								 * high-confidence heteroplasmy was defined as
								 * candidate heteroplasmy with LLR no less than
								 * 5
								 **/
								if (posObj.getLlrFWD() >= 5 || posObj.getLlrREV() >= 5) {
									if (calcStrandBias(posObj) <= 1) {
										posObj.setVariantType(LOW_LEVEL_VARIANT);
										posObj.setVariantLevel(calcHetLevel(posObj));

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
				// uncoveredPos.add(posObj);
			}

		} catch (Exception e) {
			e.printStackTrace();
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

	private double calcHetLevel(PositionObject posObj) {

		char ref = reference.charAt(posObj.getPosition() - 1);
		double fwd;
		double rev;

		if (posObj.getTopBaseFWD() == ref) {
			fwd = posObj.getMinorPercentsFWD() * posObj.getCovFWD();
			rev = posObj.getMinorBasePercentsREV() * posObj.getCovREV();
		} else {
			fwd = posObj.getTopBasePercentsFWD() * posObj.getCovFWD();
			rev = posObj.getTopBasePercentsREV() * posObj.getCovREV();
		}

		return (fwd + rev) / (posObj.getCovFWD() + posObj.getCovREV());
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

		char ref = reference.charAt(posObj.getPosition() - 1);

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

	protected void cleanup(Context context) throws IOException, InterruptedException {
		writer.close();
	}

}
