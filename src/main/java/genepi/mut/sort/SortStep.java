package genepi.mut.sort;

import java.io.File;
import java.io.IOException;
import java.util.List;

import genepi.hadoop.HdfsUtil;
import genepi.hadoop.common.WorkflowContext;
import genepi.mut.objects.Extracter;
import genepi.mut.util.HadoopJobStep;
import genepi.mut.util.ReferenceUtil;

public class SortStep extends HadoopJobStep {

	@Override
	public boolean run(WorkflowContext context) {
		try {
			boolean result = true;
			String input = context.get("bwaOut");
			String output = context.get("outputBam");
			String inType = context.get("inType");
			String archive = context.get("archive");

			if (inType.equals("se") || inType.equals("pe")) {

				List<String> folders;

				folders = HdfsUtil.getDirectories(input);

				String[] inputs = new String[folders.size()];
				for (int i = 0; i < folders.size(); i++) {
					inputs[i] = folders.get(i);
				}

				String folder = archive.substring(0,archive.lastIndexOf("/")+1);
				Extracter.extract(archive,folder);
			
				String ref = ReferenceUtil.findFileinDir(new File(folder),"fasta");
				String reference = ReferenceUtil.readInReference(ref);
				SortJob sort = new SortJob("sort-reads");
				sort.setInput(inputs);
				sort.setRefLength(reference.length()+"");
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
