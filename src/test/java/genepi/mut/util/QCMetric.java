package genepi.mut.util;

public class QCMetric {

	private String id;
	private double sensitivity;
	private double precision;
	private double specificity;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public double getSensitivity() {
		return sensitivity;
	}
	public void setSensitivity(double sensitivity) {
		this.sensitivity = sensitivity;
	}
	public double getPrecision() {
		return precision;
	}
	public void setPrecision(double precision) {
		this.precision = precision;
	}
	public double getSpecificity() {
		return specificity;
	}
	public void setSpecificity(double specificity) {
		this.specificity = specificity;
	}
}
