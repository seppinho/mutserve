package genepi.cnv.sort;

import java.io.IOException;
import java.util.List;

import genepi.base.Tool;
import genepi.cnv.util.ReferenceUtil;
import genepi.hadoop.HdfsUtil;

public class SortTool extends Tool {

	public SortTool(String[] args) {
		super(args);
	}

	@Override
	public void createParameters() {
		addParameter("input", "input path");
		addParameter("reference", "reference file");
		addParameter("statistics", "output filename statistics");
		addParameter("output", "output path");
		addParameter("inType", "input file format");
		addParameter("local-output", "local output path");
	}

	@Override
	public void init() {

	}

	@Override
	public int run() {
		try {
			boolean result = true;
			String input = (String) getValue("input");
			String output = (String) getValue("output");
			String statistics = (String) getValue("statistics");
			String localOutput = (String) getValue("local-output");
			String inType = (String) getValue("inType");
			String reference = (String) getValue("reference");

			if (inType.equals("se") || inType.equals("pe")) {

				List<String> folders;

				folders = HdfsUtil.getDirectories(input);
				String[] inputs = new String[folders.size()];
				for (int i = 0; i < folders.size(); i++) {
					inputs[i] = folders.get(i);
				}

				SortJob sort = new SortJob("sort-bam");
				sort.setInput(inputs);
				String ref = ReferenceUtil.readInReference(reference + ".fasta");
				sort.setRefLength(ref.length() + "");
				sort.setOutput(output);
				sort.setStatistics(statistics);
				sort.setLocalOutput(localOutput);
				result = sort.execute();

			}

			if (result) {
				return 0;
			} else {
				return -1;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}
}
