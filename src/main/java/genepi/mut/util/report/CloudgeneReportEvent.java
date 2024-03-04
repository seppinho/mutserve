package genepi.mut.util.report;

public class CloudgeneReportEvent {

	public enum WebCommand {

		MESSAGE,

		BEGIN_TASK,

		UPDATE_TASK,

		END_TASK,

		INC_COUNTER,

		SUBMIT_COUNTER,

		PRINTLN,

		LOG,

		OK,

		WARNING

	}

	private WebCommand command;

	private Object[] params;

	public CloudgeneReportEvent(WebCommand command, Object[] params) {
		super();
		this.command = command;
		this.params = params;
	}

	public WebCommand getCommand() {
		return command;
	}

	public void setCommand(WebCommand command) {
		this.command = command;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public String toString() {
		return Formatter.format(this);
	}

	static private class Formatter {

		public static String format(CloudgeneReportEvent event) {

			switch (event.getCommand()) {
			case SUBMIT_COUNTER: {
				String counter = (String) event.getParams()[0];
				return submitCounter(counter);
			}
			case MESSAGE: {
				String message = (String) event.getParams()[0];
				int type = ((Double) event.getParams()[1]).intValue();
				return message(message, type);
			}
			case BEGIN_TASK: {
				String name = (String) event.getParams()[0];
				return message(name, CloudgeneReport.RUNNING);
			}
			case UPDATE_TASK: {
				// String name = (String) event.getParams()[0];
				// int type = ((Integer) event.getParams()[1]).intValue();
				// message(name, type);
				break;
			}
			case LOG: {
				String line = (String) event.getParams()[0];
				return log(line);
			}
			case PRINTLN: {
				String line = (String) event.getParams()[0];
				return info(line);
			}
			case END_TASK: {
				String name = (String) event.getParams()[0];
				int type = ((Double) event.getParams()[1]).intValue();
				return message(name, type);
			}
			case INC_COUNTER: {
				String counter = (String) event.getParams()[0];
				int value = ((Double) event.getParams()[1]).intValue();
				return incCounter(counter, value);
			}
			default:

			}

			return "";

		}

		public static String info(String message) {
			return ("[INFO] " + message);
		}

		public static String log(String message) {
			return ("[LOG] " + message);
		}

		public static String incCounter(String counter, int value) {
			return ("[INC] " + counter + " " + value);
		}

		public static String submitCounter(String counter) {
			return ("[SUBMIT] " + counter);
		}

		public static String message(String message, int type) {

			switch (type) {
			case CloudgeneReport.OK:
				return ("[OK] " + message);
			case CloudgeneReport.ERROR:
				return ("[ERROR] " + message);
			case CloudgeneReport.WARNING:
				return ("[WARN] " + message);
			case CloudgeneReport.RUNNING:
				return ("[RUN] " + message);
			default:
				return ("[INFO] " + message);
			}

		}
	}

}
