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
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.FeatureUtilities;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestFeaturesResource;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
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
public class TestChunkFeatures extends TestCase {

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

    public final void testChunkEmbedding() throws Exception {

        System.out.println("\n\tTesting NER embedding");
        testFex(ChunkEmbedding.NER, false, ViewNames.NER_CONLL);
        System.out.println("\n\tTesting chunk embedding");
        testFex(ChunkEmbedding.SHALLOW_PARSE, false, ViewNames.SHALLOW_PARSE);

        System.out.println("\n\tTesting conjoined features");
        testFex(FeatureUtilities.conjoin(ChunkEmbedding.NER, ChunkEmbedding.SHALLOW_PARSE), false,
                ViewNames.NER_CONLL, ViewNames.SHALLOW_PARSE);

        System.out.println("\n\tTesting NER and chunks");
        testFex(new FeatureCollection("", ChunkEmbedding.NER, ChunkEmbedding.SHALLOW_PARSE), false,
                "");
    }

    public final void testChunkPath() throws EdisonException {
        System.out.println("\n\tTesting chunk path");
        testFex(ChunkPathPattern.SHALLOW_PARSE, true, ViewNames.SHALLOW_PARSE);
    }

    public final void testChunkProperties() throws Exception {
        System.out.println("\n\tTesting hasModal");
        testFex(ChunkPropertyFeatureFactory.hasModalVerb, false, "");

        System.out.println("\n\tTesting isNegated");
        testFex(ChunkPropertyFeatureFactory.isNegated, false, "");
    }

    public final void testLinearPosition() throws Exception {
        System.out.println("\n\tTesting linear position");
        testFex(LinearPosition.instance, true, "");

    }

    private void testFex(FeatureExtractor fex, boolean printBoth, String... viewNames)
            throws EdisonException {
        for (TextAnnotation ta : tas) {
            for (String viewName : viewNames)
                if (ta.hasView(viewName))
                    System.out.println(ta.getView(viewName));

            if (!ta.hasView(ViewNames.SRL_VERB))
                continue;

            PredicateArgumentView pav = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);

            for (Constituent predicate : pav.getPredicates()) {
                Constituent p = predicate.cloneForNewView("dummy");

                for (Relation argument : pav.getArguments(predicate)) {
                    Constituent c = argument.getTarget().cloneForNewView("dummy");
                    Relation r = new Relation("", p, c, 1);
                    System.out.println((printBoth ? r : c) + "\t" + fex.getFeatures(c));
                }
            }
        }
    }
}
