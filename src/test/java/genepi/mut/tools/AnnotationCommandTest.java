package genepi.mut.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class AnnotationCommandTest {

	@Test
	public void testMerge() throws Exception {

		File outputFile = new File("annotated.txt");
		outputFile.deleteOnExit();

		AnnotationCommand command = new AnnotationCommand();
		command.setAnnotation("files/rCRS_annotation_2020-08-20.txt");
		command.setInput("test-data/results/file.txt");
		command.setOutput(outputFile.getAbsolutePath());

		assertEquals(0, (int) command.call());

		File outputFileExpected = new File("test-data/results/file.annotated.txt");
		assertTrue(FileUtils.contentEquals(outputFileExpected, outputFile));

	}

	@Test
	public void testWrongFileFormat() {
//TODO: test with missing columns.
	}

}
