package genepi.mut.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import genepi.mut.objects.ReportObject;
import genepi.mut.objects.Sample;
import genepi.mut.objects.Variant;
import genepi.mut.util.MutationServerReader;
import lukfor.reports.HtmlReport;
import lukfor.tables.Table;
import lukfor.tables.columns.IBuildValueFunction;
import lukfor.tables.columns.types.StringColumn;
import lukfor.tables.io.TableBuilder;
import lukfor.tables.io.TableWriter;
import lukfor.tables.rows.Row;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "report", description = "Annotate your mutserve variant file.")
public class ReportCommand implements Callable<Integer> {

	@Option(names = { "--input" }, description = "Input", required = true)
	private String input;

	@Option(names = { "--output" }, description = "Output", required = true)
	private String output;

	@Override
	public Integer call() throws Exception {
		
		File inputFile = new File(input);
		if (!inputFile.exists()) {
			System.out.println("Input file '" + inputFile.getAbsolutePath() + "' not found.");
			return 1;
		}
		
		HtmlReport report = new HtmlReport("/report");
		report.setSelfContained(true);
		report.set("title", "mtDNA Server Report v2 (Beta)");

		List<ReportObject> variants = new ArrayList<ReportObject>();
		MutationServerReader reader = new MutationServerReader(input);
		HashMap<String, Sample> samples = reader.parse();

		for (Sample sample : samples.values()) {
			
			for (ArrayList<Variant> a : sample.getVariants()) {
				
				for(Variant variant : a) {
				ReportObject reportObject = new ReportObject();
				reportObject.setCoverage(variant.getCoverage());
				reportObject.setId(sample.getId());
				reportObject.setPos(variant.getPos());
				reportObject.setFilter(variant.getFilter().name());
				reportObject.setRef(variant.getRef());
				reportObject.setVar(variant.getVariant());
				variants.add(reportObject);
				}
			}
		}

		report.set("variants", variants);
		report.generate(new File(output));

		return 0;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public void setOutput(String output) {
		this.output = output;
	}

}
