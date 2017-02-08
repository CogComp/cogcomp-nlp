/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.cs.cogcomp.edison.features.lrec.srl.Nom.Identifier;

import edu.cs.cogcomp.core.datastructures.ViewNames;
import edu.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.cs.cogcomp.core.datastructures.textannotation.View;
import edu.cs.cogcomp.core.utilities.DummyTextAnnotationGenerator;
import edu.cs.cogcomp.edison.annotators.ClauseViewGenerator;
import edu.cs.cogcomp.edison.annotators.PseudoParse;
import edu.cs.cogcomp.edison.features.FeatureExtractor;
import edu.cs.cogcomp.edison.features.lrec.FeatureGenerators;
import edu.cs.cogcomp.edison.features.lrec.ProjectedPath;
import edu.cs.cogcomp.edison.features.lrec.srl.Constant;
import edu.cs.cogcomp.edison.features.lrec.srl.SRLFeaturesComparator;
import edu.cs.cogcomp.edison.features.manifest.FeatureManifest;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.List;

/**
 *
 * @author Xinbo Wu
 */
public class TestSrlNomIdentifierPredicateFeatures extends TestCase {
    private static Logger logger = LoggerFactory.getLogger(TestSrlNomIdentifierPredicateFeatures.class);

    public final void test() throws Exception {
        logger.info("PredicateFeatures Feature Extractor");

        String[] viewsToAdd =
                {ViewNames.POS, ViewNames.LEMMA, ViewNames.SHALLOW_PARSE, ViewNames.PARSE_GOLD,
                        ViewNames.SRL_VERB, ViewNames.PARSE_STANFORD, ViewNames.NER_CONLL};
        TextAnnotation ta =
                DummyTextAnnotationGenerator.generateAnnotatedTextAnnotation(viewsToAdd, true, 3);
        int i = 0;
        ta.addView(ClauseViewGenerator.STANFORD);
        ta.addView(PseudoParse.STANFORD);

        logger.info("This textannotation annotates the text: \n" + ta.getText());

        View TOKENS = ta.getView("TOKENS");

        List<Constituent> testlist = TOKENS.getConstituentsCoveringSpan(10, 13);
        testlist.addAll(TOKENS.getConstituentsCoveringSpan(26, 27));

        FeatureManifest featureManifest;
        FeatureExtractor fex;
        String fileName = Constant.prefix + "/Nom/Identifier/predicate-features.fex";

        featureManifest = new FeatureManifest(new FileInputStream(fileName));
        FeatureManifest.setFeatureExtractor("hyphen-argument-feature",
                FeatureGenerators.hyphenTagFeature);
        FeatureManifest.setTransformer("parse-left-sibling",
                FeatureGenerators.getParseLeftSibling(ViewNames.PARSE_STANFORD));
        FeatureManifest.setTransformer("parse-right-sibling",
                FeatureGenerators.getParseRightSibling(ViewNames.PARSE_STANFORD));
        FeatureManifest.setFeatureExtractor("pp-features",
                FeatureGenerators.ppFeatures(ViewNames.PARSE_STANFORD));
        FeatureManifest.setFeatureExtractor("projected-path", new ProjectedPath(
                ViewNames.PARSE_STANFORD));

        featureManifest.useCompressedName();
        featureManifest.setVariable("*default-parser*", ViewNames.PARSE_STANFORD);

        fex = featureManifest.createFex();


        SrlNomIdentifierPredicateFeatures pf = new SrlNomIdentifierPredicateFeatures();


        for (Constituent test : testlist) {
            assertTrue(SRLFeaturesComparator.isEqual(test, fex, pf));
        }
    }

}
