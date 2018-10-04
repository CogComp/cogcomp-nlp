/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Daniel Khashbai
 */
public class TestGazetteerViewGenerator {
    private static Logger logger = LoggerFactory.getLogger(TestGazetteerViewGenerator.class);

    TextAnnotation ta = DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(false, 1);
    GazetteerViewGenerator ge = GazetteerViewGenerator.cbcInstance;

    @Test
    public final void testCBCClusters() {
        ge.addView(ta);
        assertTrue(ta.getView(ge.getViewName()).getConstituents().size() > 15);
    }

    @Test
    public final void testGazetteers() {
        GazetteerViewGenerator.gazetteersInstance.ignoreGazetteer("Weapons.gz");
        GazetteerViewGenerator.gazetteersInstance.ignoreGazetteer("Weapons.Missile.gz");

        List<String[]> sentences = Arrays.asList("I live in Chicago , Illinois .".split("\\s+"), "I met George Bush .".split("\\s+"));
        TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(sentences);
        GazetteerViewGenerator.gazetteersInstance.addView(ta);

        assertEquals(ta.getView(GazetteerViewGenerator.gazetteersInstance.getViewName()).getConstituentsCoveringToken(0).get(0).getLabel(), "Films.gz");
    }

}
