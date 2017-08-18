package genepi.mut.stats;

import genepi.base.Tool;

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

}
