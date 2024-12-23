package genepi.mut.util.report;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.FileNotFoundException;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;
import java.nio.file.Files;
import java.nio.file.Paths;

public class OutputWriter {

    private String filename;

    private boolean toFile;

    private List<String> lines = new Vector<String>();

    // Constructor with no arguments, prints to stdout
    public OutputWriter() {
        this.toFile = false;
    }

    // Constructor with filename argument, prints to specified file
    public OutputWriter(String filename) throws FileNotFoundException {
        this.filename = filename;
        this.toFile = true;
        new PrintWriter(filename).close();  // Opens and immediately closes the file, clearing its contents
    }

    public void log(String message) {
        printCommand("::log::", message);
    }
    
    public void message(String message) {
        printCommand("::message::", message);
    }

    public void message(List<String> messages) {
        printMultilineCommand("message", messages);
    }
    
    public void println(String message) {
        printCommand("::debug::", message);
    }
    
    public void warning(String message) {
        printCommand("::warning::", message);
    }

    public void warning(Exception e) {
    	warning(StringEscapeUtils.escapeHtml4(e.getMessage()));
    }
    
    public void warning(List<String> messages) {
        printMultilineCommand("warning", messages);
    }

    public void error(String message) {
        printCommand("::error::", message);
    }

    public void error(Exception e) {
        error(StringEscapeUtils.escapeHtml4(e.getMessage()));
    }

    public void error(String message, Exception e) {
        error(message +  " " + StringEscapeUtils.escapeHtml4(e.getMessage()));
    }

    public void error(List<String> messages) {
        printMultilineCommand("error", messages);
    }

    public void setCounter(String counter, long value) {
        print("::set-counter name=" + counter + ":: " + value);
    }

    public void printCommand(String type, String message) {
        print(type + " " + message);
    }

    public void printMultilineCommand(String type, List<String> messages) {
        print("::group type=" + type + "::");
        for (String message: messages) {
            print(message);
        }
        print("::endgroup::");
    }

    public void print(String message) {
        if (toFile) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
                writer.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(message);
        }
    }

    public void load() throws IOException {
        Files.lines(Paths.get(filename))
                .forEach(lines::add);
    }

    public boolean hasInMemory(String content) {
        for (String line : lines) {
            if (line.contains(content)) {
                return true;
            }
        }
        return false;
    }

    public void view() {
        for (String line : lines) {
            System.out.println(line);
        }
    }

}
