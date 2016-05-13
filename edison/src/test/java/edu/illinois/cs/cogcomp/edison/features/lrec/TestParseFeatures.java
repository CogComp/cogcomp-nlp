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

import org.apache.log4j.Logger;

/**
 * Test class
 * <p>
 * <b>NB:</b> If needed, please re-create the {@code feature.collection.text} file using
 * {@link CreateTestFeaturesResource}.
 *
 * @author Vivek Srikumar
 */
public class TestParseFeatures extends TestCase {
    static Logger log = Logger.getLogger(TestParseFeatures.class.getName());

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
        log.debug("\n\tTesting parse head-word+POS: Charniak");
        testFex(ParseHeadWordPOS.CHARNIAK, false, ViewNames.PARSE_CHARNIAK);

        log.debug("\n\tTesting parse head-word+POS: Stanford");
        testFex(ParseHeadWordPOS.STANFORD, false, ViewNames.PARSE_STANFORD);

    }

    public final void testParsePath() throws Exception {
        log.debug("\n\tTesting parse path: Charniak");
        testFex(ParsePath.CHARNIAK, true, ViewNames.PARSE_CHARNIAK);

        log.debug("\n\tTesting parse path: Stanford");
        testFex(ParsePath.STANFORD, true, ViewNames.PARSE_STANFORD);

    }

    public final void testParsePhraseType() throws Exception {
        log.debug("\n\tTesting parse phrase: Charniak");
        testFex(ParsePhraseType.CHARNIAK, false, ViewNames.PARSE_CHARNIAK);

        log.debug("\n\tTesting parse phrase: Stanford");
        testFex(ParsePhraseType.STANFORD, false, ViewNames.PARSE_STANFORD);

    }

    public final void testParseSiblings() throws Exception {
        log.debug("\n\tTesting parse siblings: Charniak");
        testFex(ParseSiblings.CHARNIAK, false, ViewNames.PARSE_CHARNIAK);

        log.debug("\n\tTesting parse siblings: Stanford");
        testFex(ParseSiblings.STANFORD, false, ViewNames.PARSE_STANFORD);

    }

    public final void testVerbVoiceIndicator() throws Exception {
        log.debug("\n\tTesting verb voice: Charniak");
        for (TextAnnotation ta : tas) {
            if (!ta.hasView(ViewNames.SRL_VERB))
                continue;
            PredicateArgumentView pav = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);
            log.debug(ta);

            for (Constituent predicate : pav.getPredicates()) {
                log.debug(predicate + "\t"
                        + VerbVoiceIndicator.CHARNIAK.getFeatures(predicate));
            }
        }

        log.debug("\n\tTesting verb voice: Stanford");
        for (TextAnnotation ta : tas) {
            if (!ta.hasView(ViewNames.SRL_VERB))
                continue;
            PredicateArgumentView pav = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);

            log.debug(ta);
            for (Constituent predicate : pav.getPredicates()) {
                log.debug(predicate + "\t"
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
                    log.debug(ta.getView(viewName));

            PredicateArgumentView pav = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);

            for (Constituent predicate : pav.getPredicates()) {
                Constituent p = predicate.cloneForNewView("dummy");

                for (Relation argument : pav.getArguments(predicate)) {
                    Constituent c = argument.getTarget().cloneForNewView("dummy");
                    Relation r = new Relation("", p, c, 1);

                    log.debug((printBoth ? r : c) + "\t" + fex.getFeatures(c));
                }
            }
        }
    }
}
