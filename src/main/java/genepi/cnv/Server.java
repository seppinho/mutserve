package genepi.cnv;

import genepi.base.Toolbox;
import genepi.cnv.align.AlignTool;
import genepi.cnv.detect.DetectTool;
import genepi.cnv.pileup.PileupTool;
import genepi.cnv.sort.SortTool;
import genepi.cnv.stats.StatisticsTool;

import java.lang.reflect.InvocationTargetException;

public class Server {
 
	public static final String REF_DIRECTORY = "/tmp/mutation-server-data";
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, SecurityException,
			NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		
		Toolbox toolbox = new Toolbox("cnv-mutation-server-1.0.jar", args);

		// align SE and PE with jbwa and MapReduce
		toolbox.addTool("align", AlignTool.class);

		// sort file and output a BAM file with MapReduce + SecondarySort
		toolbox.addTool("sort", SortTool.class);

		// read in BAM file and count per POS with MapReduce
		toolbox.addTool("analyse", PileupTool.class);

		// detect low level variants
		toolbox.addTool("detect", DetectTool.class);

		// generate statistics
		toolbox.addTool("stats", StatisticsTool.class);

		toolbox.start();

	}

}
