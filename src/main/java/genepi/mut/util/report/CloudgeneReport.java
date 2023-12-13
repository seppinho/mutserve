package genepi.mut.util.report;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import genepi.mut.util.report.CloudgeneReportEvent.WebCommand;

public class CloudgeneReport {

	public static final int OK = 0;

	public static final int ERROR = 1;

	public static final int WARNING = 2;

	public static final int RUNNING = 3;

	private String filename = null;

	private List<CloudgeneReportEvent> events = new Vector<CloudgeneReportEvent>();

	public CloudgeneReport() {

	}

	public CloudgeneReport(String filename) throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		loadFromFile(filename);
	}

	public boolean hasInMemory(String content) {
		for (CloudgeneReportEvent event : events) {
			if (event.toString().contains(content)) {
				return true;
			}
		}
		return false;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void loadFromFile(String filename) throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		Type collectionType = new TypeToken<List<CloudgeneReportEvent>>() {
		}.getType();
		Gson gson = (new GsonBuilder()).create();
		events = gson.fromJson(new FileReader(filename), collectionType);
	}

	public void saveToFile(String filename) throws JsonIOException, IOException {
		Gson gson = (new GsonBuilder()).create();
		FileWriter writer = new FileWriter(filename);
		gson.toJson(events, writer);
		writer.close();
	}

	public void println(String line) {
		addEvent(WebCommand.PRINTLN, line);
	}

	public void log(String line) {
		addEvent(WebCommand.LOG, line);
	}
	
	public void incCounter(String name, int value) {
		addEvent(WebCommand.INC_COUNTER, name, value);
	}

	public void submitCounter(String name) {
		addEvent(WebCommand.SUBMIT_COUNTER, name);
	}

	public void message(String message, int type) {
		addEvent(WebCommand.MESSAGE, message, type);
	}

	public void ok(String message) {
		addEvent(WebCommand.MESSAGE, message, OK);
	}
	
	public void error(String message) {
		addEvent(WebCommand.MESSAGE, message, ERROR);
	}
	
	public void warning(String message) {
		addEvent(WebCommand.MESSAGE, message, WARNING);
	}

	public void beginTask(String name) {
		addEvent(WebCommand.BEGIN_TASK, name);
	}

	public void updateTask(String name, int type) {
		addEvent(WebCommand.UPDATE_TASK, name, type);
	}

	public void endTask(String message, int type) {
		addEvent(WebCommand.END_TASK, message, type);
	}

	public void addEvent(WebCommand command, Object... params) {
		CloudgeneReportEvent event = new CloudgeneReportEvent(command, params);
		events.add(event);
		if (filename != null) {
			try {
				saveToFile(filename);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

}
