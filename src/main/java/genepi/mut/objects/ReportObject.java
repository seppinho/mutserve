package genepi.mut.objects;

public class ReportObject {

	private String id;
	private int pos;
	private char var;
	private char ref;
	private int coverage;
	private int type;
	private String filter;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public char getVar() {
		return var;
	}
	public void setVar(char var) {
		this.var = var;
	}
	public char getRef() {
		return ref;
	}
	public void setRef(char ref) {
		this.ref = ref;
	}
	public int getCoverage() {
		return coverage;
	}
	public void setCoverage(int coverage) {
		this.coverage = coverage;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getPos() {
		return pos;
	}
	public void setPos(int pos) {
		this.pos = pos;
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
}
