/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.prepsrl.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.FeatureInputTransformer;
import edu.illinois.cs.cogcomp.edison.features.factory.RogetThesaurusFeatures;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.features.factory.WordNetFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory.*;
import static edu.illinois.cs.cogcomp.edison.features.factory.WordNetFeatureExtractor.*;

public class PrepSRLFeatures extends LBJavaFeatureExtractor {
    private final FeatureExtractor fex;
    private static WordNetFeatureExtractor firstSense, wordNetFexes;
    static {
        firstSense = getWordNetFeatureExtractor(WordNetFeatureClass.synsetsFirstSense);
        wordNetFexes =
                getWordNetFeatureExtractor(WordNetFeatureClass.existsEntry,
                        WordNetFeatureClass.synonymsFirstSense,
                        WordNetFeatureClass.synsetsAllSenses,
                        WordNetFeatureClass.partHolonymsFirstSense,
                        WordNetFeatureClass.partHolonymsAllSenses,
                        WordNetFeatureClass.memberHolonymsFirstSense,
                        WordNetFeatureClass.memberHolonymsAllSenses,
                        WordNetFeatureClass.substanceHolonymsFirstSense,
                        WordNetFeatureClass.substanceHolonymsAllSenses,
                        WordNetFeatureClass.lexicographerFileNamesFirstSense,
                        WordNetFeatureClass.lexicographerFileNamesAllSenses);
    }

    private PrepSRLFeatures(FeatureExtractor fex) {
        this.fex = fex;
    }

    private static final FeatureInputTransformer previousVerb = new FeatureInputTransformer() {
        @Override
        public List<Constituent> transform(Constituent input) {
            // the first verb to the left of the input
            TextAnnotation ta = input.getTextAnnotation();

            List<Constituent> list = new ArrayList<>();
            for (int tokenId = input.getStartSpan() - 1; tokenId >= 0; tokenId--) {
                String pos = WordHelpers.getPOS(ta, tokenId);

                // if the iterator crosses a punctuation or a WH word, exit this loop.
                // however, stopping on punctuation decreases recall.
                // if (POSUtils.isPOSPunctuation(pos))
                // break;

                // WDT is a W-determiner
                if (pos.equals("WDT"))
                    break;

                if (POSUtils.isPOSVerb(pos)) {
                    Constituent constituent =
                            addIncomingRelation(input, new Constituent("", "", ta, tokenId,
                                    tokenId + 1));

                    constituent.addAttribute("Head", tokenId + "");
                    list.add(constituent);
                    break;
                }
            }
            return list;
        }

        @Override
        public String name() {
            return "prev-verb";
        }
    };

    public static PrepSRLFeatures prevWordFeatures =
            new PrepSRLFeatures(new FeatureCollection("#prev-word-lemma+firstSense",
                    FeatureInputTransformer.previousWord, lemma, firstSense));

    public static PrepSRLFeatures prevVerbFeatures = new PrepSRLFeatures(new FeatureCollection(
            "#prev-verb-lemma+firstSense", previousVerb, lemma, firstSense));

    public static PrepSRLFeatures govFeatures = new PrepSRLFeatures(new FeatureCollection(
            "#govFeats", FeatureInputTransformer.dependencyGovernor, wordNetFexes,
            RogetThesaurusFeatures.INSTANCE, WordFeatureExtractorFactory.word,
            WordFeatureExtractorFactory.pos, WordFeatureExtractorFactory.capitalization,
            WordFeatureExtractorFactory.lemma, WordFeatureExtractorFactory.conflatedPOS,
            WordFeatureExtractorFactory.deAdjectivalAbstractNounsSuffixes,
            WordFeatureExtractorFactory.deNominalNounProducingSuffixes,
            WordFeatureExtractorFactory.deVerbalSuffix, WordFeatureExtractorFactory.knownPrefixes,
            WordFeatureExtractorFactory.prefixSuffixes));
    public static PrepSRLFeatures objFeatures = new PrepSRLFeatures(new FeatureCollection(
            "#govFeats", FeatureInputTransformer.dependencyObject, wordNetFexes,
            RogetThesaurusFeatures.INSTANCE, WordFeatureExtractorFactory.word,
            WordFeatureExtractorFactory.pos, WordFeatureExtractorFactory.capitalization,
            WordFeatureExtractorFactory.lemma, WordFeatureExtractorFactory.conflatedPOS,
            WordFeatureExtractorFactory.deAdjectivalAbstractNounsSuffixes,
            WordFeatureExtractorFactory.deNominalNounProducingSuffixes,
            WordFeatureExtractorFactory.deVerbalSuffix, WordFeatureExtractorFactory.knownPrefixes,
            WordFeatureExtractorFactory.prefixSuffixes));

    /** Needed for the LBJava generation. */
    @SuppressWarnings("unused")
    public PrepSRLFeatures() {
        fex = null;
    }

    @Override
    public Set<Feature> getFeatures(Constituent instance) throws EdisonException {
        if (fex == null)
            throw new RuntimeException(
                    "PrepSRLFeatures should be accessed only via its static members");
        return fex.getFeatures(instance);
    }

    @Override
    public String getName() {
        if (fex == null)
            throw new RuntimeException(
                    "PrepSRLFeatures should be accessed only via its static members");
        return fex.getName();
    }

    private static Constituent addIncomingRelation(Constituent input, Constituent c) {
        Constituent i = input.cloneForNewView("");

        Constituent c1 = c.cloneForNewView("");
        new Relation("", i, c1, 0d);

        return c1;
    }
}
