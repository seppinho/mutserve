package genepi.mut.steps.report;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import genepi.io.FileUtil;
import genepi.mut.objects.ReportObject;
import genepi.mut.objects.Sample;
import genepi.mut.objects.Variant;
import genepi.mut.util.MutationServerReader;
import lukfor.reports.HtmlReport;

public class ReportGeneratorTest {

	@Test
	public void testGenerate() throws Exception {

		String input = "test-data/mtdna/report/1000g.txt";
		String output = "test-data/mtdna/report/out.html";

		HtmlReport report = new HtmlReport("/report");
		report.setSelfContained(true);
		report.set("title", "mtDNA Server Report v2 (Beta)");

		List<ReportObject> objects = new ArrayList<ReportObject>();
		MutationServerReader reader = new MutationServerReader(input);
		HashMap<String, Sample> samples = reader.parse();

		for (Sample sample : samples.values()) {

			for (ArrayList<Variant> variants : sample.getVariants()) {

				for (Variant variant : variants) {
					ReportObject reportObject = new ReportObject();
					reportObject.setCoverage(variant.getCoverage());
					reportObject.setId(sample.getId());
					reportObject.setPos(variant.getPos());
					reportObject.setFilter(variant.getFilter().name());
					reportObject.setRef(variant.getRef());
					reportObject.setVar(variant.getVariant());
					reportObject.setLevel(variant.getLevel());
					reportObject.setType(variant.getType());
					objects.add(reportObject);
				}
			}
		}

		report.set("variants", objects);
		report.generate(new File(output));

		FileUtil.deleteFile(output);

	}
}
