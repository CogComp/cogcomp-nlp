package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestFeaturesResource;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import junit.framework.TestCase;

import java.util.List;

/**
 * Test class
 * <p/>
 * <b>NB:</b> If needed, please re-create the {@code feature.collection.text} file using
 * {@link CreateTestFeaturesResource}.
 *
 * @author Vivek Srikumar
 */
public class TestParseFeatures extends TestCase {
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

    public final void testParseHeadWordPOS() throws Exception {
        System.out.println("\n\tTesting parse head-word+POS: Charniak");
        testFex(ParseHeadWordPOS.CHARNIAK, false, ViewNames.PARSE_CHARNIAK);

        System.out.println("\n\tTesting parse head-word+POS: Stanford");
        testFex(ParseHeadWordPOS.STANFORD, false, ViewNames.PARSE_STANFORD);

    }

    public final void testParsePath() throws Exception {
        System.out.println("\n\tTesting parse path: Charniak");
        testFex(ParsePath.CHARNIAK, true, ViewNames.PARSE_CHARNIAK);

        System.out.println("\n\tTesting parse path: Stanford");
        testFex(ParsePath.STANFORD, true, ViewNames.PARSE_STANFORD);

    }

    public final void testParsePhraseType() throws Exception {
        System.out.println("\n\tTesting parse phrase: Charniak");
        testFex(ParsePhraseType.CHARNIAK, false, ViewNames.PARSE_CHARNIAK);

        System.out.println("\n\tTesting parse phrase: Stanford");
        testFex(ParsePhraseType.STANFORD, false, ViewNames.PARSE_STANFORD);

    }

    public final void testParseSiblings() throws Exception {
        System.out.println("\n\tTesting parse siblings: Charniak");
        testFex(ParseSiblings.CHARNIAK, false, ViewNames.PARSE_CHARNIAK);

        System.out.println("\n\tTesting parse siblings: Stanford");
        testFex(ParseSiblings.STANFORD, false, ViewNames.PARSE_STANFORD);

    }

    public final void testVerbVoiceIndicator() throws Exception {
        System.out.println("\n\tTesting verb voice: Charniak");
        for (TextAnnotation ta : tas) {
            if (!ta.hasView(ViewNames.SRL_VERB))
                continue;
            PredicateArgumentView pav = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);
            System.out.println(ta);

            for (Constituent predicate : pav.getPredicates()) {
                System.out.println(predicate + "\t"
                        + VerbVoiceIndicator.CHARNIAK.getFeatures(predicate));
            }
        }

        System.out.println("\n\tTesting verb voice: Stanford");
        for (TextAnnotation ta : tas) {
            if (!ta.hasView(ViewNames.SRL_VERB))
                continue;
            PredicateArgumentView pav = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);

            System.out.println(ta);
            for (Constituent predicate : pav.getPredicates()) {
                System.out.println(predicate + "\t"
                        + VerbVoiceIndicator.STANFORD.getFeatures(predicate));
            }
        }

    }

    private void testFex(FeatureExtractor fex, boolean printBoth, String... viewNames)
            throws EdisonException {

        for (TextAnnotation ta : tas) {
            if (!ta.hasView(ViewNames.SRL_VERB))
                continue;

            for (String viewName : viewNames)
                if (ta.hasView(viewName))
                    System.out.println(ta.getView(viewName));

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
