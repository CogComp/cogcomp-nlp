/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.core.io.caches;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.IResetableIterator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TextAnnotationDBHandlerTest {

    private String dbFile;
    private TextAnnotationDBHandler dbHandler;
    private String datasetName;

    String sentA = "This is a text that contains pre-tokenized sentences .";
    String sentB = "For the purposes of this test , tokens are separated by whitespace .";
    String sentC = "Sentences are separated by newline characters .";
    String rawText = sentA + System.lineSeparator() + sentB + System.lineSeparator() + sentC;
    private List<String[]> tokenizedSentences;

    @Before
    public void setUp() throws Exception {
        String[] sentences = rawText.split("\\n");
        tokenizedSentences = new ArrayList<>(sentences.length);
        for (String sentTokens : sentences) {
            tokenizedSentences.add(sentTokens.split("\\s"));
        }

        // There might be a cleaner way to get the true root directory
        String rootDir = System.getProperty("user.dir");
        if (!rootDir.contains("core-utilities"))
            rootDir += "/core-utilities";
        dbFile = rootDir + "/src/test/resources/test";

        datasetName = "TestData";
        dbHandler = new TextAnnotationDBHandler(dbFile, new String[] {datasetName});
        try {
            dbHandler.initializeDatasets(dbFile);
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
    }

    @After
    public void tearDown() throws Exception {
        IOUtils.rm(dbFile + ".h2.db");
        IOUtils.rm(dbFile + ".mv.db");
    }

    @Test
    public void testAddTextAnnotation() throws Exception {
        TextAnnotation ta =
                BasicTextAnnotationBuilder.createTextAnnotationFromTokens(tokenizedSentences);
        try {
            dbHandler.addTextAnnotation(datasetName, ta);
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
        IResetableIterator<TextAnnotation> dataset = null;
        try {
            dataset = dbHandler.getDataset(datasetName);
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
        assertEquals(true, dataset.hasNext());
        TextAnnotation next = dataset.next();
        assertEquals(ta.toString(), next.toString());
    }
}
