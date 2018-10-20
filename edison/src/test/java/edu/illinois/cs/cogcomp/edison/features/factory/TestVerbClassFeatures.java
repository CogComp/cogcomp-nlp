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
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestFeaturesResource;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Test class
 * <p>
 * <b>NB:</b> If needed, please re-create the {@code feature.collection.text} file using
 * {@link CreateTestFeaturesResource}.
 *
 * @author Vivek Srikumar
 */
public class TestVerbClassFeatures {

    private static List<TextAnnotation> tas;

    static {
        try {
            tas = IOUtils.readObjectAsResource(TestChunkFeatures.class, "test.ta");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Set<String> correctResponses = new HashSet<>(Arrays.asList(new String[]{
            "landed\t[9.10]",
            "walked\t[51.3.2]",
            "steered\t[26.7]",
            "radioed\t[37.4]",
            "haslanded\t[*]",
            "reach\t[13.5.1]",
            "brought\t[11.3]",
            "rest\t[47.6, 9.2]",
            "opened\t[40.3.2, 45.4, 47.6, 48.1.1]",
            "stepped\t[*]",
            "declared\t[29.4, 37.7, 48.1.2]",
            "planted\t[9.7]",
            "'s\t[*]"
    }));

    @Test
    public final void test() throws Exception {
        TextAnnotation ta = tas.get(tas.size()-1);
        PredicateArgumentView pav = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);
        for (Constituent predicate : pav.getPredicates()) {
            Constituent p = predicate.cloneForNewView("dummy");
            String response = p + "\t" + LevinVerbClassFeature.instance.getFeatures(p);
            assertTrue(correctResponses.contains(response));
        }
    }
}
