package genepi.mut;

import genepi.base.Toolbox;
import genepi.mut.align.AlignTool;
import genepi.mut.detect.DetectTool;
import genepi.mut.pileup.PileupTool;
import genepi.mut.sort.SortTool;
import genepi.mut.stats.StatisticsTool;

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
