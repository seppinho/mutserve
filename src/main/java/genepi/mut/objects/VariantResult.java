package genepi.mut.objects;

public class VariantResult {

	private String id;
	private int position;
	private char ref;
	private char alt;
	private char top;
	private char minor;
	private double level;
	private int covFWD;
	private int covREV;
	private int type;

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

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
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
	
}
