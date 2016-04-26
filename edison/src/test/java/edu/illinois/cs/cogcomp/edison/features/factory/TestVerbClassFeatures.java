/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestFeaturesResource;
import junit.framework.TestCase;

import java.util.List;

/**
 * Test class
 * <p>
 * <b>NB:</b> If needed, please re-create the {@code feature.collection.text} file using
 * {@link CreateTestFeaturesResource}.
 *
 * @author Vivek Srikumar
 */
public class TestVerbClassFeatures extends TestCase {

    private static List<TextAnnotation> tas;

    static {
        try {
            tas = IOUtils.readObjectAsResource(TestChunkFeatures.class, "test.ta");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public final void test() throws Exception {
        for (TextAnnotation ta : tas) {

            if (!ta.hasView(ViewNames.SRL_VERB))
                continue;

            PredicateArgumentView pav = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);

            System.out.println(ta);
            for (Constituent predicate : pav.getPredicates()) {
                Constituent p = predicate.cloneForNewView("dummy");

                System.out.println(p + "\t" + LevinVerbClassFeature.instance.getFeatures(p));
            }
        }
    }
}
