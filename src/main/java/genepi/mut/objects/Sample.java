package genepi.mut.objects;

import java.util.Collection;
import java.util.HashMap;

public class Sample {

	private String id;
	private HashMap<Integer, Variant> variants;
	private int amountHomoplasmies;
	private int amountVariants;
	private int amountHeteroplasmies;
	boolean chip;
	private float sumCoverage = 0;
	private float sumHeteroplasmyLevel = 0;

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

		this.updateCount(var.getType());

		if (var.getType() == 2) {
			this.updateHetLevels(var.getLevel());
		}

		this.sumCoverage(var.getCoverage());
	}

	private void updateCount(int type) {

		amountVariants += 1;

		if (type == 1) {
			amountHomoplasmies += 1;
		}

		if (type == 2) {
			amountHeteroplasmies += 1;
		}
	}

	private void sumCoverage(int coverage) {
		sumCoverage += coverage;
	}

	private void updateHetLevels(double level) {
		sumHeteroplasmyLevel += level;
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

	public float getSumCoverage() {
		return sumCoverage;
	}

	public void setSumCoverage(float totalCoverage) {
		this.sumCoverage = totalCoverage;
	}

	public float getSumHeteroplasmyLevel() {
		return sumHeteroplasmyLevel;
	}

	public void setSumHeteroplasmyLevel(float countHeteroplasmyLevel) {
		this.sumHeteroplasmyLevel = countHeteroplasmyLevel;
	}

	public boolean isChip() {
		return chip;
	}

	public void setChip(boolean chip) {
		this.chip = chip;
	}
}