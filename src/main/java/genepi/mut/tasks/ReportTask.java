package genepi.mut.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import genepi.mut.App;
import genepi.mut.objects.ReportObject;
import genepi.mut.objects.Sample;
import genepi.mut.objects.Variant;
import genepi.mut.util.MutationServerReader;
import lukfor.reports.HtmlReport;

public class ReportTask {

	private String input;
	private String output;

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public void createReport() throws IOException {

		HtmlReport report = new HtmlReport("/report");
		report.setSelfContained(true);
		report.set("title", "mtDNA Server Report - ("+App.VERSION+")");

		List<ReportObject> variants = new ArrayList<ReportObject>();
		MutationServerReader reader = new MutationServerReader(input);
		HashMap<String, Sample> samples = reader.parse();

		for (Sample sample : samples.values()) {

			for (ArrayList<Variant> a : sample.getVariants()) {

				for (Variant variant : a) {
					ReportObject reportObject = new ReportObject();
					reportObject.setCoverage(variant.getCoverage());
					reportObject.setId(sample.getId());
					reportObject.setPos(variant.getPos());
					reportObject.setFilter(variant.getFilter().name());
					reportObject.setRef(variant.getRef());
					reportObject.setVar(variant.getVariant());
					reportObject.setLevel(variant.getLevel());
					reportObject.setType(variant.getType());
					variants.add(reportObject);

				}
			}
		}

		report.set("variants", variants);
		report.generate(new File(output));

	}

}
