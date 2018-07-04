package genepi.mut;

import genepi.base.Toolbox;
import genepi.mut.align.AlignStep;
import genepi.mut.annotate.BaseAnnotateTool;
import genepi.mut.pileup.PileupStep;
import genepi.mut.pileup.PileupToolLocal;
import genepi.mut.sort.SortStep;
import genepi.mut.stats.StatisticsTool;

import java.lang.reflect.InvocationTargetException;

public class Server {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, SecurityException,
			NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

		Toolbox toolbox = new Toolbox("mutation-server-1.1.X.jar", args); 

		// align SE and PE with jbwa and MapReduce
		toolbox.addTool("align", AlignStep.class);

		// sort file and output a BAM file with MapReduce + SecondarySort
		toolbox.addTool("sort", SortStep.class);

		// read in BAM file and count per POS with MapReduce
		toolbox.addTool("analyse", PileupStep.class);

		// read in BAM file and count per POS locally
		toolbox.addTool("analyse-local", PileupToolLocal.class);

		// generate statistics
		toolbox.addTool("stats", StatisticsTool.class);
		
		// generate base annotation
		toolbox.addTool("base-annotate", BaseAnnotateTool.class);

		toolbox.start();

	}

}
