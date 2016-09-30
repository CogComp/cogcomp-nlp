/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.core.io.caches;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.IResetableIterator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TextAnnotationMapDBHandlerTest {
    private final static String dbFile = "src/test/resources/mapDB-test.db";
    private final static String trainDataset = "train";
    private final static String testDataset = "test";
    private TextAnnotationMapDBHandler mapDBHandler;

    @Before
    public void setUp() throws Exception {
        mapDBHandler = new TextAnnotationMapDBHandler(dbFile);
    }

    @After
    public void tearDown() throws Exception {
        mapDBHandler.close();
    }

    @Test
    public void isCached() throws Exception {
        // The DB should already contain a single TextAnnotation in the training dataset
        assertTrue(mapDBHandler.isCached(trainDataset, dbFile));
    }

    @Test
    public void addRemoveTextAnnotation() throws Exception {
        TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 2);
        // Check that the TextAnnotation is not contained in the DB
        assertFalse(mapDBHandler.contains(ta));

        // Add it to the DB
        mapDBHandler.addTextAnnotation(testDataset, ta);

        // Check that it is contained somewhere in the DB
        assertTrue(mapDBHandler.contains(ta));

        // Check the specific dataset it was saved in
        IResetableIterator<TextAnnotation> dataset = mapDBHandler.getDataset(testDataset);
        assertEquals("The construction of the John Smith library finished on time . " +
                "The $10M building was designed in 2016 .", dataset.next().getTokenizedText());

        // Remove it and check the test dataset is now empty
        mapDBHandler.removeTextAnnotation(ta);
        assertFalse(mapDBHandler.getDataset(testDataset).hasNext());
    }

    @Test
    public void updateTextAnnotation() throws Exception {
        TextAnnotation ta = mapDBHandler.getDataset(trainDataset).next();
        // Add a new view to the TextAnnotation
        String viewName = "TEST_VIEW";
        View dummyView = new View(viewName, "TEST", ta, 0.0);
        ta.addView(viewName, dummyView);
        assertTrue(ta.hasView(viewName));

        // Update the DB
        mapDBHandler.updateTextAnnotation(ta);
        // Check if the update is present
        ta = mapDBHandler.getDataset(trainDataset).next();
        assertTrue(ta.hasView(viewName));

        // Revert the changes and check if it's updated
        ta.removeView(viewName);
        mapDBHandler.updateTextAnnotation(ta);
        ta = mapDBHandler.getDataset(trainDataset).next();
        assertFalse(ta.hasView(viewName));

    }
}