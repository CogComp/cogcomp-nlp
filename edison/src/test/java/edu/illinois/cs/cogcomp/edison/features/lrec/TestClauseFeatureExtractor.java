package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.edison.annotators.ClauseViewGenerator;
import edu.illinois.cs.cogcomp.edison.annotators.PseudoParse;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.CreateTestFeaturesResource;
import junit.framework.TestCase;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
/**
 * Test class
 * <p>
 * <b>NB:</b> If needed, please re-create the {@code feature.collection.text} file using
 * {@link CreateTestFeaturesResource}.
 *
 * @author Vivek Srikumar
 */
public class TestClauseFeatureExtractor extends TestCase {
    static Logger log = Logger.getLogger(TestClauseFeatureExtractor.class.getName());

    private static List<TextAnnotation> tas;

    static {
        try {
            tas = IOUtils.readObjectAsResource(TestClauseFeatureExtractor.class, "test.ta");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public final void testClauseView() throws Exception {
        testFex(ClauseFeatureExtractor.CHARNIAK);
    }

    private void testFex(FeatureExtractor fex) throws Exception {

        for (TextAnnotation ta : tas) {
            if (!ta.hasView(ViewNames.PARSE_CHARNIAK))
                continue;

            ta.addView(ClauseViewGenerator.CHARNIAK);
            ta.addView(PseudoParse.CHARNIAK);

            log.debug(ta.getView(ViewNames.PSEUDO_PARSE_CHARNIAK));

            PredicateArgumentView pav = (PredicateArgumentView) ta.getView(ViewNames.SRL_VERB);

            for (Constituent predicate : pav.getPredicates()) {
                Constituent p = predicate.cloneForNewView("dummy");

                for (Relation arg : pav.getArguments(predicate)) {
                    Constituent c = arg.getTarget().cloneForNewView("dummy");

                    new Relation("", p, c, 1.0);

                    Set<Feature> features = fex.getFeatures(c);

                    log.debug(c + "\t" + features);
                }
            }
        }

    }
}
