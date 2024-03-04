package genepi.mut.objects;

import genepi.mut.pileup.VariantCaller.Filter;

public class VariantResult {

	private String id;
	private String position;
	private char ref;
	private char alt;
	private char top;
	private char minor;
	private double level;
	private double levelTop;
	private double levelMinor;
	private int covFWD;
	private int covREV;
	private int type;
	private Filter filter;
	private String meanBaseQuality;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public char getRef() {
		return ref;
	}

	public void setRef(char ref) {
		this.ref = ref;
	}

	public char getAlt() {
		return alt;
	}

	public void setAlt(char alt) {
		this.alt = alt;
	}

	public char getTop() {
		return top;
	}

	public void setTop(char top) {
		this.top = top;
	}

	public char getMinor() {
		return minor;
	}

	public void setMinor(char minor) {
		this.minor = minor;
	}

	public double getLevel() {
		return level;
	}
	
	public void setLevel(double level) {
		this.level = level;
	}
	
	public void setLevelTop(double top) {
		this.levelTop = top;
	}
	
	public void setLevelMinor(double minor) {
		this.levelMinor = minor;
	}
	
	public double getLevelTop() {
		return levelTop;
	}

	public double getLevelMinor() {
		return levelMinor;
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

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public void setMeanBaseQuality(String meanBaseQuality) {
		this.meanBaseQuality = meanBaseQuality;
		
	}

	public String getMeanBaseQuality() {
		return meanBaseQuality;
	}
	
}
