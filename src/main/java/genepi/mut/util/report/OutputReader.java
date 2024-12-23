package genepi.mut.util.report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Vector;

public class OutputReader {

    private List<String> lines = new Vector<String>();

    public OutputReader(String filename) throws IOException {

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
