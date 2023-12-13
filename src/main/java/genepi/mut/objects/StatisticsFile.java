package genepi.mut.objects;

public class StatisticsFile {

	private String sampleName;
	private String contig;
	private int numberOfReads = -1;;
	private int coveredBases = -1;;
	private int coveredPercentage = -1;
	private double meanDepth = -1;;
	private double meanBaseQuality = -1;;
	private double meanMapQuality = -1;;

	public String getSampleName() {
		return sampleName;
	}

	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	public String getContig() {
		return contig;
	}

	public void setContig(String contig) {
		this.contig = contig;
	}

	public int getNumberOfReads() {
		return numberOfReads;
	}

	public void setNumberOfReads(int numberOfReads) {
		this.numberOfReads = numberOfReads;
	}

	public int getCoveredBases() {
		return coveredBases;
	}

	public void setCoveredBases(int coveredBases) {
		this.coveredBases = coveredBases;
	}

	public int getCoveredPercentage() {
		return coveredPercentage;
	}

	public void setCoveredPercentage(int coveredPercentage) {
		this.coveredPercentage = coveredPercentage;
	}

	public double getMeanDepth() {
		return meanDepth;
	}

	public void setMeanDepth(double meanDepth) {
		this.meanDepth = meanDepth;
	}

	public double getMeanBaseQuality() {
		return meanBaseQuality;
	}

	public void setMeanBaseQuality(double meanBaseQuality) {
		this.meanBaseQuality = meanBaseQuality;
	}

	public double getMeanMapQuality() {
		return meanMapQuality;
	}

	public void setMeanMapQuality(double meanMapQuality) {
		this.meanMapQuality = meanMapQuality;
	}
}
