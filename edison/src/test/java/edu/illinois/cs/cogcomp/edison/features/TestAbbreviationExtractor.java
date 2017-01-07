/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestFeaturesResource;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Test class
 * @author Daniel Khashabi
 */
public class TestAbbreviationExtractor {
    private static String str = "This is a test of schwartz 's excellent abbreviation tool (SEAT) on a simple example. ABC is not defined here. ";

    private static TextAnnotation ta = TextAnnotationUtilities.createFromTokenizedString(str);
    private static AbbreviationFeatureExtractor abbreviationFeatureExtractor = new AbbreviationFeatureExtractor();

    @Test
    public void testAbbreviations() throws EdisonException {
        try {
            abbreviationFeatureExtractor.addView(ta);
        } catch (AnnotatorException e) {
            e.printStackTrace();
        }
        assert ta.getAvailableViews().contains(abbreviationFeatureExtractor.getViewName());
        assert ta.getView(abbreviationFeatureExtractor.getViewName()).getConstituents().size() >= 1;
    }
}
