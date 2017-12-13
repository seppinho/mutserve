package genepi.mut.pileup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import org.broadinstitute.gatk.utils.baq.BAQ;
import genepi.mut.objects.BasePosition;
import genepi.mut.util.BaqAlt;
import genepi.mut.util.ReferenceUtil;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.reference.FastaSequenceIndex;
import htsjdk.samtools.reference.FastaSequenceIndexCreator;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;

public class BamAnalyser {

	final static String headerRaw = "SAMPLE\tPOS\tREF\tTOP-FWD\tMINOR-FWD\tTOP-REV\tMINOR-REV\tCOV-FWD\tCOV-REV\tCOV-TOTAL\tTYPE\tLEVEL\t%A\t%C\t%G\t%T\t%D\t%N\t%a\t%c\t%g\t%t\t%d\t%n\tTOP-FWD-PERCENT\tTOP-REV-PERCENT\tMINOR-FWD-PERCENT\tMINOR-REV-PERCENT\tLLRFWD\tLLRREV\tLLRAFWD\tLLRCFWD\tLLRGFWD\tLLRTFWD\tLLRAREV\tLLRCREV\tLLRGREV\tLLRTREV\tLLRDFWD\tLLRDREV";

	final static String headerVariants = "SampleID\tPos\tRef\tVariant\tMajor/Minor\tVariant-Level\tCoverage-FWD\tCoverage-Rev\tCoverage-Total\tINS Type";

	HashMap<String, BasePosition> counts;

	IndexedFastaSequenceFile refReader;

	String filename;

	String referenceString;

	BaqAlt baqHMMAltered;

	BAQ baqHMM;

	String version = "mtdna";

	int baseQual;

	int mapQual;

	int alignQual;

	boolean baq;

	enum versionEnum {

		MTDNA, GENOME

	}

	public BamAnalyser(String file, String fastaPath) {

		this(file, fastaPath, 20, 20, 30, true, "mtdna");

	}

	public BamAnalyser(String filename, String fastaPath, int baseQual, int mapQual, int alignQual, boolean baq,
			String version) {

		Path path = new File(fastaPath).toPath();

		FastaSequenceIndex fg;

		try {
			if (!new File(fastaPath + ".fai").exists()) {
				
				fg = FastaSequenceIndexCreator.buildFromFasta(path);

				fg.write(new File(fastaPath + ".fai").toPath());

			}

		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		this.referenceString = ReferenceUtil.readInReference(fastaPath);

		this.refReader = new IndexedFastaSequenceFile(new File(fastaPath),
				new FastaSequenceIndex(new File(fastaPath + ".fai")));

		this.counts = new HashMap<String, BasePosition>(referenceString.length());

		this.baseQual = baseQual;

		this.mapQual = mapQual;

		this.alignQual = alignQual;

		this.filename = filename;

		this.baq = baq;

		this.version = version;

		if (version.equalsIgnoreCase(versionEnum.MTDNA.name())) {

			baqHMMAltered = new BaqAlt(1e-4, 1e-2, 7, (byte) 0, true);

		} else {

			System.out.println("BAQ default");

			baqHMM = new BAQ(1e-4, 1e-2, 7, (byte) 0, true);
		}

	}

	public HashMap<String, BasePosition> getCounts() {
		return counts;
	}

	public void setCounts(HashMap<String, BasePosition> counts) {
		this.counts = counts;
	}

	public void analyseRead(SAMRecord samRecord, boolean indelCalling) throws Exception {

		if (samRecord.getMappingQuality() < mapQual) {
			return;
		}

		if (samRecord.getReadUnmappedFlag()) {
			return;
		}

		if (samRecord.getDuplicateReadFlag()) {
			return;
		}

		if (samRecord.getReadLength() <= 25) {
			return;
		}

		if (ReferenceUtil.getTagFromSamRecord(samRecord.getAttributes(), "AS") < alignQual) {
			return;
		}

		if (baq) {

			if (version.equalsIgnoreCase(versionEnum.MTDNA.name())) {

				baqHMMAltered.baqRead(samRecord, refReader,
						genepi.mut.util.BaqAlt.CalculationMode.CALCULATE_AS_NECESSARY,
						genepi.mut.util.BaqAlt.QualityMode.OVERWRITE_QUALS);

			} else {

				baqHMM.baqRead(samRecord, refReader,
						org.broadinstitute.gatk.utils.baq.BAQ.CalculationMode.CALCULATE_AS_NECESSARY,
						org.broadinstitute.gatk.utils.baq.BAQ.QualityMode.OVERWRITE_QUALS);

			}
		}

		String readString = samRecord.getReadString();

		for (int i = 0; i < readString.length(); i++) {

			int currentPos = samRecord.getReferencePositionAtReadPosition(i + 1);
			
			//if e.g softclips are included pos is 0, so skip them immediately! 
			if(currentPos > 0) {

			if (samRecord.getBaseQualities()[i] >= baseQual) {

				// context.getCounter("mtdna", "GOOD-QUAL").increment(1);

				String key = filename + ":" + currentPos;

				BasePosition basePos = counts.get(key);

				if (basePos == null) {
					basePos = new BasePosition();
					counts.put(key, basePos);
				}

				char base = readString.charAt(i);

				if ((samRecord.getFlags() & 0x10) == 0x10) {
					// context.getCounter("mtdna", "REV-READ").increment(1);
					switch (base) {
					case 'A':
						basePos.addaRev(1);
						basePos.addaRevQ(samRecord.getBaseQualities()[i]);
						break;
					case 'C':
						basePos.addcRev(1);
						basePos.addcRevQ(samRecord.getBaseQualities()[i]);
						break;
					case 'G':
						basePos.addgRev(1);
						basePos.addgRevQ(samRecord.getBaseQualities()[i]);
						break;
					case 'T':
						basePos.addtRev(1);
						basePos.addtRevQ(samRecord.getBaseQualities()[i]);
						break;
					case 'N':
						basePos.addnRev(1);
						break;
					default:
						break;
					}
				} else {
					// context.getCounter("mtdna", "FWD-READ").increment(1);
					switch (base) {
					case 'A':
						basePos.addaFor(1);
						basePos.addaForQ(samRecord.getBaseQualities()[i]);
						break;
					case 'C':
						basePos.addcFor(1);
						basePos.addcForQ(samRecord.getBaseQualities()[i]);
						break;
					case 'G':
						basePos.addgFor(1);
						basePos.addgForQ(samRecord.getBaseQualities()[i]);
						break;
					case 'T':
						basePos.addtFor(1);
						basePos.addtForQ(samRecord.getBaseQualities()[i]);
						break;
					case 'N':
						basePos.addnFor(1);
						break;
					default:
						break;
					}
				}

			} else {
				// context.getCounter("mtdna", "BAD-QUAL").increment(1);
			}
		}
		}

		
		if(indelCalling) {
			
		Integer currentReferencePos = samRecord.getAlignmentStart();
		
		int currentPos = 0;
		
		for (CigarElement cigarElement : samRecord.getCigar().getCigarElements()) {
			
			Integer cigarElementLength = cigarElement.getLength();
			
			if (cigarElement.getOperator() == CigarOperator.D) {

				Integer cigarElementStart = currentReferencePos;
				
				Integer cigarElementEnd = currentReferencePos + cigarElementLength;

				while (cigarElementStart < cigarElementEnd) {

					String key = filename + ":" + cigarElementStart;

					BasePosition basePos = counts.get(key);

					if (basePos == null) {
						basePos = new BasePosition();
						counts.put(key, basePos);
					}

					if ((samRecord.getFlags() & 0x10) == 0x10) {
						basePos.adddRev(1);
						//no quality for missing value, set fake
						basePos.adddRevQ((byte) 40);
					} else {
						basePos.adddFor(1);
						//no quality for missing value, set fake
						basePos.adddForQ((byte) 40);
					}

					cigarElementStart++;
				}
				
				//to get correct position for insertions
				if (currentPos > 0) {
					currentPos = currentPos - cigarElementLength;
				}

			}
			
			//currently not used. 
			if (cigarElement.getOperator() == CigarOperator.I) {
				
				Integer cigarElementStart = currentReferencePos;

				int i = 1;
				
				int length = cigarElement.getLength();
				
				while (i <= length) {
					
					//-1 since array starts at 0
					int arrayPos = currentPos + i - 1;
					
					char insBase = samRecord.getReadString().charAt(arrayPos);
					
					byte quality = samRecord.getBaseQualities()[arrayPos];
					
					String key = filename + ":" + cigarElementStart + "." + i + "/" + length;
					
					BasePosition basePos = counts.get(key);
					
					i++;

					if (basePos == null) {
						basePos = new BasePosition();
						counts.put(key, basePos);
					}
					
					if ((samRecord.getFlags() & 0x10) == 0x10) {
						// context.getCounter("mtdna", "REV-READ").increment(1);
						switch (insBase) {
						case 'A':
							basePos.addaRev(1);
							basePos.addaRevQ(quality);
							break;
						case 'C':
							basePos.addcRev(1);
							basePos.addcRevQ(quality);
							break;
						case 'G':
							basePos.addgRev(1);
							basePos.addgRevQ(quality);
							break;
						case 'T':
							basePos.addtRev(1);
							basePos.addtRevQ(quality);
							break;
						case 'N':
							basePos.addnRev(1);
							break;
						default:
							break;
						}
					} else {

						switch (insBase) {
						case 'A':
							basePos.addaFor(1);
							basePos.addaForQ(quality);
							break;
						case 'C':
							basePos.addcFor(1);
							basePos.addcForQ(quality);
							break;
						case 'G':
							basePos.addgFor(1);
							basePos.addgForQ(quality);
							break;
						case 'T':
							basePos.addtFor(1);
							basePos.addtForQ(quality);
							break;
						case 'N':
							basePos.addnFor(1);
							break;
						default:
							break;
						}
					}
				}

				//update positions for insertions
				currentPos = currentPos + cigarElement.getLength();

			}

			if (cigarElement.getOperator().consumesReferenceBases() || cigarElement.getOperator() == CigarOperator.SOFT_CLIP) {
				currentReferencePos = currentReferencePos + cigarElement.getLength();
				currentPos = currentPos + cigarElement.getLength();
			}
		}
		
		}
	}

	public String getReferenceString() {
		return referenceString;
	}

	public void setReferenceString(String referenceString) {
		this.referenceString = referenceString;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
