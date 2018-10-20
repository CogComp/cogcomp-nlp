/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestFeaturesResource;
import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Test class
 * <p>
 * <b>NB:</b> If needed, please re-create the {@code feature.collection.text} file using
 * {@link CreateTestFeaturesResource}.
 *
 * @author Vivek Srikumar
 */
public class TestSyntacticFrame {
    private static List<TextAnnotation> tas;
    private static Logger logger = LoggerFactory.getLogger(TestSyntacticFrame.class);

    static {
        try {
            tas = IOUtils.readObjectAsResource(TestSyntacticFrame.class, "test.ta");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public final void testSyntacticFrame() throws Exception {
        testFex(SyntacticFrame.CHARNIAK);
    }

    private void testFex(FeatureExtractor fex) throws Exception {

        for (TextAnnotation ta : tas) {
            if (!ta.hasView(ViewNames.PARSE_CHARNIAK))
                continue;

            PredicateArgumentView pav = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);

            for (Constituent predicate : pav.getPredicates()) {
                Constituent p = predicate.cloneForNewView("dummy");

                for (Relation arg : pav.getArguments(predicate)) {
                    Constituent c = arg.getTarget().cloneForNewView("dummy");

                    new Relation("", p, c, 1.0);

                    Set<Feature> features = fex.getFeatures(c);

                    logger.info(c + "\t" + features);

                    assertEquals(3, features.size());
                }
            }
        }
    }
}
