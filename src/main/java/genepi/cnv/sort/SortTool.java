package genepi.cnv.sort;

import java.io.IOException;
import java.util.List;

import genepi.cnv.util.HadoopJobStep;
import genepi.cnv.util.ReferenceUtil;
import genepi.hadoop.HdfsUtil;
import genepi.hadoop.common.WorkflowContext;
import genepi.io.FileUtil;

public class SortTool extends HadoopJobStep {

	@Override
	public boolean run(WorkflowContext context) {
		try {
			boolean result = true;
			String input = context.get("bwaOut");
			String output = context.get("outputBam");
			String inType = context.get("inType");
			String reference = context.get("reference");
			
			String folder = getFolder(SortTool.class);

			if (inType.equals("se") || inType.equals("pe")) {

				List<String> folders;

				folders = HdfsUtil.getDirectories(input);
				
				String[] inputs = new String[folders.size()];
				for (int i = 0; i < folders.size(); i++) {
					inputs[i] = folders.get(i);
				}
				SortJob sort = new SortJob("sort-reads");
				sort.setInput(inputs);
				String ref = ReferenceUtil.readInReference(FileUtil.path(folder,reference + ".fasta"));
				sort.setRefLength(ref.length() + "");
				sort.setOutput(output);
				
				result = executeHadoopJob(sort, context);

			}

			return result;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
}
