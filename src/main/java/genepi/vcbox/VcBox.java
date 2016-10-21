package genepi.vcbox;

import genepi.base.Toolbox;
import genepi.vcbox.align.AlignTool;
import genepi.vcbox.pileup.PileupTool;
import genepi.vcbox.sort.SortTool;

import java.lang.reflect.InvocationTargetException;


public class VcBox {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, SecurityException,
			NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		Toolbox toolbox = new Toolbox("genepi-pileup.jar", args);

		// align SE and PE with jbwa and MapReduce
		toolbox.addTool("align", AlignTool.class);

		// sort file and output a BAM file with MapReduce + SecondarySort
		toolbox.addTool("sort", SortTool.class);

		// read in BAM file and count per POS with MapReduce
		toolbox.addTool("analyse", PileupTool.class);

		toolbox.start();

	}

}
