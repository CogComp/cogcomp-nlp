/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.manifest;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.CurrencyIndicator;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.*;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.*;

import static edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory.*;

class KnownFexes {

    final static Map<String, FeatureExtractor> fexes = new HashMap<>();
    private static final Set<Feature> bias = Collections
            .unmodifiableSet(new LinkedHashSet<Feature>(Collections.singletonList(DiscreteFeature
                    .create("bias"))));
    private final static FeatureExtractor biasFeature = new FeatureExtractor() {

        @Override
        public String getName() {
            return "";
        }

        @Override
        public Set<Feature> getFeatures(Constituent c) throws EdisonException {
            return bias;
        }
    };

    static {
        fexes.put("bias", biasFeature);
        fexes.put("capitalization", capitalization);
        fexes.put("conflated-pos", conflatedPOS);
        fexes.put("de-adj-nouns", deAdjectivalAbstractNounsSuffixes);
        fexes.put("de-nom-nouns", deNominalNounProducingSuffixes);
        fexes.put("de-verbal-suffixes", deVerbalSuffix);
        fexes.put("gerunds", gerundMarker);
        fexes.put("known-prefixes", knownPrefixes);
        fexes.put("lemma", WordFeatureExtractorFactory.lemma);
        fexes.put("nom", nominalizationMarker);
        fexes.put("numbers", numberNormalizer);
        fexes.put("pos", pos);
        fexes.put("prefix-suffix", prefixSuffixes);
        fexes.put("word", word);
        fexes.put("word-case", wordCase);

        fexes.put("label", new FeatureExtractor() {

            @Override
            public String getName() {
                return "";
            }

            @Override
            public Set<Feature> getFeatures(Constituent c) throws EdisonException {
                return new LinkedHashSet<Feature>(Collections.singletonList(DiscreteFeature
                        .create(c.getLabel())));
            }
        });

        fexes.put("date", dateMarker);
        fexes.put("days-of-week", ListFeatureFactory.daysOfTheWeek);
        fexes.put("months", ListFeatureFactory.months);
        fexes.put("possessive-pronouns", ListFeatureFactory.possessivePronouns);

        fexes.put("length", SpanLengthFeature.instance);

        fexes.put("chunk-embedding", ChunkEmbedding.SHALLOW_PARSE);
        fexes.put("ne-embedding", ChunkEmbedding.NER);

        fexes.put("chunk-path", ChunkPathPattern.SHALLOW_PARSE);

        fexes.put("linear-position", LinearPosition.instance);
        fexes.put("linear-distance", LinearDistance.instance);

        fexes.put("nom-lex-class", NomLexClassFeature.instance);
        fexes.put("is-negated", ChunkPropertyFeatureFactory.isNegated);
        fexes.put("has-modal-verb", ChunkPropertyFeatureFactory.hasModalVerb);

        fexes.put("levin-verb-class", LevinVerbClassFeature.instance);

        // fexes.put("clauses-charniak", ClauseFeatureExtractor.CHARNIAK);
        // fexes.put("clauses-stanford", ClauseFeatureExtractor.STANFORD);
        // fexes.put("clauses-berkeley", ClauseFeatureExtractor.BERKELEY);

        fexes.put("currency", CurrencyIndicator.instance);

        fexes.put("brown-clusters-100", BrownClusterFeatureExtractor.instance100);
        fexes.put("brown-clusters-320", BrownClusterFeatureExtractor.instance320);
        fexes.put("brown-clusters-1000", BrownClusterFeatureExtractor.instance1000);
        fexes.put("brown-clusters-3200", BrownClusterFeatureExtractor.instance3200);

//        fexes.put("gazetteers", WordFeatureExtractorFactory.getGazetteerFeatureExtractor(
//                "gazetteers", GazetteerViewGenerator.gazetteersInstance));
//
//        fexes.put("cbc", WordFeatureExtractorFactory.getGazetteerFeatureExtractor("cbc",
//                GazetteerViewGenerator.gazetteersInstance));

        fexes.put("CORLEX", CorelexFeatureExtractor.instance);
        fexes.put("roget-thesaurus", RogetThesaurusFeatures.INSTANCE);

    }

    static List<String> getKnownFeatureExtractors() {

        List<String> f = new ArrayList<>();
        for (String name : new TreeSet<>(fexes.keySet())) {
            f.add(name);
        }
        return f;
    }
}
