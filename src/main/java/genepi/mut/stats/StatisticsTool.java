package genepi.mut.stats;

import genepi.base.Tool;
import genepi.mut.annotate.AnnotateTool;

public class StatisticsTool extends Tool {

	public StatisticsTool(String[] args) {
		super(args);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createParameters() {
		addParameter("input", "input raw file");
		addParameter("output", "output statistics per id + pos");
		
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
	}

	@Override
	public int run() {

		String input = (String) getValue("input");
		String output = (String) getValue("output");
		
		CreateStatistics stats = new CreateStatistics(input, output);
		stats.createSampleStatistics();
		
		return 0;
	}
	
	/*public static void main(String[] args) {

		StatisticsTool statTool = new StatisticsTool(args);

		statTool = new StatisticsTool(new String[] { "--input",
		 "/data2/git/mutation-server/test-data/tmp/rawLocal1000G", "--output", "/data2/git/mutation-server/test-data/tmp/stats.txt"});

		statTool.start();

	}*/
	
}
