package genepi.mut;

import static lukfor.progress.Components.PROGRESS_BAR;
import static lukfor.progress.Components.SPACE;
import static lukfor.progress.Components.SPINNER;
import static lukfor.progress.Components.TASK_NAME;
import static lukfor.progress.Components.TIME;

import java.util.Arrays;

import genepi.mut.pileup.PileupToolLocal;
import genepi.mut.tools.VariantCallingCommand;
import genepi.mut.tools.AnnotationCommand;
import lukfor.progress.renderer.ProgressIndicatorGroup;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "mutserve")
public class App implements Runnable {

	public static final String URL = "https://github.com/seppinho/mutserve";

	public static final String APP = "mtDNA Variant Detection";

	public static final String VERSION = "v2.0.0-rc4";

	public static final String COPYRIGHT = "(c) Sebastian Schoenherr, Hansi Weissensteiner, Lukas Forer";

	// public static String[] ARGS = new String[0];

	public static String COMMAND;
	
	static CommandLine commandLine; 
	
	@Option(names = { "--version" }, versionHelp = true)
	boolean showVersion;

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

		commandLine = new CommandLine(new App());
		commandLine.addSubcommand("call", new VariantCallingCommand());
		commandLine.addSubcommand("annotate", new AnnotationCommand());

		commandLine.setExecutionStrategy(new CommandLine.RunLast());
		commandLine.execute(args);
		
	}

	public static ProgressIndicatorGroup STYLE_LONG_TASK = new ProgressIndicatorGroup(SPACE, SPINNER, SPACE, TASK_NAME,
			PROGRESS_BAR, TIME);

	public static ProgressIndicatorGroup STYLE_SHORT_TASK = new ProgressIndicatorGroup(SPACE, SPINNER, SPACE,
			TASK_NAME);

	@Override
	public void run() {
		System.out.println("mutserve");
		commandLine.usage(System.out);

	}

}
