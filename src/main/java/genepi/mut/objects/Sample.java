package genepi.mut.objects;

import java.util.Collection;
import java.util.HashMap;

public class Sample {

	private String id;
	private HashMap<Integer, Variant> variants;
	private int amountHomoplasmies;
	private int amountVariants;
	private int amountHeteroplasmies;
	private float totalCoverage = 0;
	private float countHeteroplasmyLevel = 0;

	public Sample() {
		variants = new HashMap<Integer, Variant>();
	}

	public Collection<Variant> getVariants() {
		return variants.values();
	}

	public Variant getVariant(int pos) {
		return variants.get(pos);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void addVariant(Variant var) {
		variants.put(var.getPos(), var);
	}

	public void updateVariantCount(int type) {

		amountVariants += 1;

		if (type == 1) {
			amountHomoplasmies += 1;
		}

		if (type == 2) {
			amountHeteroplasmies += 1;
		}
	}

	public void updateCoverage(int coverage) {
		totalCoverage += coverage;
	}

	public void updateHetLevels(double level) {
		countHeteroplasmyLevel += level;
	}

	public int getAmountHomoplasmies() {
		return amountHomoplasmies;
	}

	public void setAmountHomoplasmies(int amountHomoplasmies) {
		this.amountHomoplasmies = amountHomoplasmies;
	}

	public int getAmountVariants() {
		return amountVariants;
	}

	public void setAmountVariants(int amountVariants) {
		this.amountVariants = amountVariants;
	}

	public int getAmountHeteroplasmies() {
		return amountHeteroplasmies;
	}

	public void setAmountHeteroplasmies(int amountHeteroplasmies) {
		this.amountHeteroplasmies = amountHeteroplasmies;
	}

	public float getTotalCoverage() {
		return totalCoverage;
	}

	public void setTotalCoverage(float totalCoverage) {
		this.totalCoverage = totalCoverage;
	}

	public float getCountHeteroplasmyLevel() {
		return countHeteroplasmyLevel;
	}

	public void setCountHeteroplasmyLevel(float countHeteroplasmyLevel) {
		this.countHeteroplasmyLevel = countHeteroplasmyLevel;
	}

}
