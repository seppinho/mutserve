package genepi.mut.pileup;

import java.io.File;
import java.util.HashMap;
import org.broadinstitute.gatk.utils.baq.BAQ;
import genepi.mut.objects.BasePosition;
import genepi.mut.util.BaqAlt;
import genepi.mut.util.ReferenceUtil;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.reference.FastaSequenceIndex;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;

public class BamAnalyser {
	
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

	public BamAnalyser(String filename, String fastaPath, int baseQual, int mapQual, int alignQual, boolean baq, String version) {
		
		this.referenceString = ReferenceUtil.readInReference(fastaPath);
		
		this.refReader = new IndexedFastaSequenceFile(new File(fastaPath), new FastaSequenceIndex(new File(fastaPath+".fai")));

		this.counts = new HashMap<String, BasePosition>(referenceString.length());
		
		this.baseQual = baseQual;

		this.mapQual = mapQual;

		this.alignQual = alignQual;
		
		this.filename = filename;

		this.baq = baq;

		this.version = version;
		
		System.out.println("base quality -> " + baseQual);
		System.out.println("map quality -> " + mapQual);
		System.out.println("align quality -> " + alignQual);
		System.out.println("baq -> " + baq);

		if (version.equalsIgnoreCase(versionEnum.MTDNA.name())) {
			
			System.out.println("BAQ mtDNA");
			
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

	public void analyseRead(SAMRecord samRecord) throws Exception {

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

			if (samRecord.getBaseQualities()[i] >= baseQual) {

				// context.getCounter("mtdna", "GOOD-QUAL").increment(1);

				String key = filename+":"+currentPos;
				
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

		/** for deletions */
		Integer currentReferencePos = samRecord.getAlignmentStart();
		for (CigarElement cigarElement : samRecord.getCigar().getCigarElements()) {
			int count = 0;
			if (cigarElement.getOperator() == CigarOperator.D) {

				Integer cigarElementStart = currentReferencePos;
				Integer cigarElementLength = cigarElement.getLength();
				Integer cigarElementEnd = currentReferencePos + cigarElementLength;

				while (cigarElementStart < cigarElementEnd) {
					
					String key = filename+":"+cigarElementStart;

					BasePosition basePos = counts.get(key);

					if (basePos == null) {
						basePos = new BasePosition();
						counts.put(key, basePos);
					}

					if ((samRecord.getFlags() & 0x10) == 0x10) {
						basePos.adddRev(1);
						basePos.adddRevQ(samRecord.getBaseQualities()[count]);
					} else {
						basePos.adddFor(1);
						basePos.adddForQ(samRecord.getBaseQualities()[count]);
					}

					cigarElementStart++;
				}

			}

			if (cigarElement.getOperator().consumesReferenceBases()) {
				currentReferencePos = currentReferencePos + cigarElement.getLength();
			}
			count++;
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
