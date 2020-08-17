package genepi.mut;

import static lukfor.progress.Components.PROGRESS_BAR;
import static lukfor.progress.Components.SPACE;
import static lukfor.progress.Components.SPINNER;
import static lukfor.progress.Components.TASK_NAME;
import static lukfor.progress.Components.TIME;

import java.util.Arrays;

import genepi.mut.pileup.PileupToolLocal;
import genepi.mut.tools.VariantCallingCommand;
import lukfor.progress.renderer.ProgressIndicatorGroup;
import picocli.CommandLine;

public class App {

	public static final String URL = "https://github.com/seppinho/mutserve";

	public static final String APP = "mtDNA Variant Detection";

	public static final String VERSION = "v2.0.0-rc3";

	public static final String COPYRIGHT = "(c) Sebastian Schoenherr, Hansi Weissensteiner, Lukas Forer";

	// public static String[] ARGS = new String[0];

	public static String COMMAND;

	public static void main(String[] args) {

		System.out.println();
		System.out.println(APP + " " + VERSION);
		if (URL != null && !URL.isEmpty()) {
			System.out.println(URL);
		}
		if (COPYRIGHT != null && !COPYRIGHT.isEmpty()) {
			System.out.println(COPYRIGHT);
		}

		COMMAND = Arrays.toString(args);
		new CommandLine(new VariantCallingCommand()).execute(args);

	}

	public static ProgressIndicatorGroup STYLE_LONG_TASK = new ProgressIndicatorGroup(SPACE, SPINNER, SPACE, TASK_NAME,
			PROGRESS_BAR, TIME);

	public static ProgressIndicatorGroup STYLE_SHORT_TASK = new ProgressIndicatorGroup(SPACE, SPINNER, SPACE,
			TASK_NAME);

}
