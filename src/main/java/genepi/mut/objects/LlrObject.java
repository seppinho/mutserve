package genepi.mut.objects;

public class LlrObject {

	private double llrFWD;
	private double llrREV;
	
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
	@Override
	public String toString() {
		return llrFWD + "/" + llrREV;
	}
	
	
}
