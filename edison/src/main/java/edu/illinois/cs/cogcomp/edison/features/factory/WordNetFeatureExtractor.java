/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureUtilities;
import edu.illinois.cs.cogcomp.edison.features.WordFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetHelper;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;

import java.io.FileNotFoundException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This feature extractor generates the following WordNet based features from a word: synonyms,
 * synsets, hypernyms, hypernym-sets.
 * <p>
 * The behavior for multiple word constituents is just like the {@link WordFeatureExtractor}:
 * <p>
 * If the input constituent is not a word, then the feature extractor can do one of two things: If a
 * flag is set in the constructor, then it will generate features from the last word of the
 * constituent. If the flag is not set, then it will throw a {@code FeatureException}.
 * <p>
 * Note: you must call {@link WordNetFeatureExtractor#addFeatureType(WordNetFeatureClass)} in order
 * to specify which types of WordNet features you would like. If you do not add any feature types,
 * no features will be returned. See {@link WordNetFeatureClass} to learn about WordNet feature
 * types.
 * </p>
 * 
 * @author Vivek Srikumar
 */
public class WordNetFeatureExtractor extends WordFeatureExtractor {

    public static WordNetManager wnManager = null;
    private final Set<WordNetFeatureClass> featureClasses;

    /**
     * Creates a new WordNetFeatureExtractor.
     * <p>
     * It is probably safest to the parameter {@code useLastWord} to true. This will provide a check
     * to ensure that the WordNetFeatureExtractor only sees words.
     */
    public WordNetFeatureExtractor(boolean useLastWord) throws FileNotFoundException, JWNLException {
        super(useLastWord);

        featureClasses = new LinkedHashSet<>();

        if (wnManager == null) {
            wnManager = WordNetManager.getInstance();
        }
    }

    /**
     * Creates a new WordNetFeatureExtractor. This constructor is equivalent to calling
     * {@code new WordNetFeatureExtractor(false)}.
     *
     * @throws JWNLException
     * @throws java.io.FileNotFoundException
     * @see edu.illinois.cs.cogcomp.edison.features.factory.WordNetFeatureExtractor#WordNetFeatureExtractor(boolean)
     */
    public WordNetFeatureExtractor() throws FileNotFoundException, JWNLException {
        this(true);
    }

    /**
     * Specify which types of features you would like this feature extractor to extract. This must
     * be called at least once or the feature extractor will return nothing.
     * 
     * @param name feature type
     */
    public void addFeatureType(WordNetFeatureClass name) {
        this.featureClasses.add(name);
    }

    @Override
    public Set<Feature> getWordFeatures(TextAnnotation ta, int tokenPosition)
            throws EdisonException {

        String token = ta.getToken(tokenPosition).toLowerCase().trim();
        String pos = WordHelpers.getPOS(ta, tokenPosition);

        POS wnPOS = WordNetHelper.getWNPOS(pos);

        if (wnPOS == null) {
            return new LinkedHashSet<>();
        }

        try {
            IndexWord iw = wnManager.getIndexWord(wnPOS, token);

            Set<String> feats = new LinkedHashSet<>();
            if (this.featureClasses.contains(WordNetFeatureClass.existsEntry)) {
                if (iw != null) {
                    feats.add("exists");

                    if (POSUtils.isPOSNoun(pos))
                        feats.add("nn+exists");
                    else if (POSUtils.isPOSVerb(pos))
                        feats.add("vb+exists");
                    else if (POSUtils.isPOSAdjective(pos))
                        feats.add("adj+exists");
                    else if (POSUtils.isPOSAdverb(pos))
                        feats.add("adv+exists");
                }

            }

            if (iw == null)
                return FeatureUtilities.getFeatures(feats);

            if (featureClasses.contains(WordNetFeatureClass.lemma))
                feats.add("lemma:" + iw.getLemma());

            boolean first = true;
            for (Synset synset : iw.getSenses()) {

                if (first) {
                    first = false;

                    addSynsetFeature(feats, synset, WordNetFeatureClass.synsetsFirstSense, "syns1:");

                    addLexFileNameFeature(feats, synset,
                            WordNetFeatureClass.lexicographerFileNamesFirstSense, "lex-file1:");

                    addVerbFrameFeature(feats, synset, WordNetFeatureClass.verbFramesFirstSense,
                            "verb-frame1:");

                    addSynonymFeature(feats, synset, WordNetFeatureClass.synonymsFirstSense,
                            "syn1:");

                    addRelatedWordsFeatures(feats, synset, PointerType.HYPERNYM,
                            WordNetFeatureClass.hypernymsFirstSense, "hyp1:");

                    addRelatedWordsFeatures(feats, synset, PointerType.PART_HOLONYM,
                            WordNetFeatureClass.partHolonymsFirstSense, "part-holo1:");

                    addRelatedWordsFeatures(feats, synset, PointerType.SUBSTANCE_HOLONYM,
                            WordNetFeatureClass.substanceHolonymsFirstSense, "subs-holo1:");

                    addRelatedWordsFeatures(feats, synset, PointerType.MEMBER_HOLONYM,
                            WordNetFeatureClass.memberHolonymsFirstSense, "mem-holo1:");

                    addRelatedWordsLexFileFeatures(feats, synset, PointerType.HYPERNYM,
                            WordNetFeatureClass.hypernymFirstSenseLexicographerFileNames,
                            "hyp1-lex-file:");

                    addRelatedWordsLexFileFeatures(feats, synset, PointerType.PART_HOLONYM,
                            WordNetFeatureClass.partHolonymsFirstSenseLexicographerFileNames,
                            "part-holo1-lex-file:");

                    addRelatedWordsLexFileFeatures(feats, synset, PointerType.SUBSTANCE_HOLONYM,
                            WordNetFeatureClass.substanceHolonymsFirstSenseLexicographerFileNames,
                            "subst-holo1-lex-file:");

                    addRelatedWordsLexFileFeatures(feats, synset, PointerType.MEMBER_HOLONYM,
                            WordNetFeatureClass.memberHolonymsFirstSenseLexicographerFileNames,
                            "mem-holo1-lex-file:");

                    addPointerFeature(feats, synset, WordNetFeatureClass.pointersFirstSense,
                            "ptrs1:");

                }

                addSynsetFeature(feats, synset, WordNetFeatureClass.synsetsAllSenses, "syns:");

                addLexFileNameFeature(feats, synset,
                        WordNetFeatureClass.lexicographerFileNamesAllSenses, "lex-file:");

                addVerbFrameFeature(feats, synset, WordNetFeatureClass.verbFramesAllSenses,
                        "vb-frame:");

                addSynonymFeature(feats, synset, WordNetFeatureClass.synonymsAllSenses, "syn:");

                addRelatedWordsFeatures(feats, synset, PointerType.HYPERNYM,
                        WordNetFeatureClass.hypernymsAllSenses, "hyp:");

                addRelatedWordsFeatures(feats, synset, PointerType.PART_HOLONYM,
                        WordNetFeatureClass.partHolonymsAllSenses, "part-holo:");

                addRelatedWordsFeatures(feats, synset, PointerType.SUBSTANCE_HOLONYM,
                        WordNetFeatureClass.substanceHolonymsAllSenses, "subst-holo:");

                addRelatedWordsFeatures(feats, synset, PointerType.MEMBER_HOLONYM,
                        WordNetFeatureClass.memberHolonymsAllSenses, "mem-holo:");

                addRelatedWordsLexFileFeatures(feats, synset, PointerType.HYPERNYM,
                        WordNetFeatureClass.hypernymAllSensesLexicographerFileNames,
                        "hyp-lex-file:");

                addRelatedWordsLexFileFeatures(feats, synset, PointerType.PART_HOLONYM,
                        WordNetFeatureClass.partHolonymsAllSensesLexicographerFileNames,
                        "part-holo-lex-file:");

                addRelatedWordsLexFileFeatures(feats, synset, PointerType.SUBSTANCE_HOLONYM,
                        WordNetFeatureClass.substanceHolonymsAllSensesLexicographerFileNames,
                        "subst-holo-lex-file:");

                addRelatedWordsLexFileFeatures(feats, synset, PointerType.MEMBER_HOLONYM,
                        WordNetFeatureClass.memberHolonymsAllSensesLexicographerFileNames,
                        "mem-holo-lex-file:");

                addPointerFeature(feats, synset, WordNetFeatureClass.pointersAllSenses, "ptrs:");

            }
            return FeatureUtilities.getFeatures(feats);
        } catch (Exception ex) {
            throw new EdisonException("Error accessing WordNet: " + ex.getMessage());
        }

    }

    private void addPointerFeature(Set<String> f1, Synset synset, WordNetFeatureClass name,
            String key) {
        if (featureClasses.contains(name)) {
            for (Pointer p : synset.getPointers()) {
                f1.add(key + p.getType().getLabel());
            }
        }
    }

    private void addSynonymFeature(Set<String> f1, Synset synset, WordNetFeatureClass name,
            String key) {
        if (featureClasses.contains(name)) {
            for (Word w : synset.getWords())
                f1.add(key + w.getLemma());
        }
    }

    private void addSynsetFeature(Set<String> f1, Synset synset, WordNetFeatureClass clazz,
            String key) {
        if (featureClasses.contains(clazz))
            f1.add(key + synset.getKey().toString());
    }

    private void addVerbFrameFeature(Set<String> f1, Synset synset, WordNetFeatureClass clazz,
            String key) {
        if (featureClasses.contains(clazz)) {
            for (String frame : synset.getVerbFrames()) {
                f1.add(key + frame);
            }
        }

    }

    private void addLexFileNameFeature(Set<String> f1, Synset synset, WordNetFeatureClass clazz,
            String key) {
        if (featureClasses.contains(clazz))
            f1.add(key + synset.getLexFileName());
    }

    private void addRelatedWordsFeatures(Set<String> f1, Synset synset, PointerType type,
            WordNetFeatureClass name, String key) throws JWNLException {
        if (featureClasses.contains(name)) {
            Pointer[] pointers = synset.getPointers(type);

            for (Pointer p : pointers) {
                Synset target = p.getTargetSynset();

                for (Word w : target.getWords()) {
                    f1.add(key + w.getLemma());
                }

            }
        }
    }

    private void addRelatedWordsLexFileFeatures(Set<String> f1, Synset synset, PointerType type,
            WordNetFeatureClass name, String key) throws JWNLException {
        if (featureClasses.contains(name)) {
            Pointer[] pointers = synset.getPointers(type);

            for (Pointer p : pointers) {
                Synset target = p.getTargetSynset();

                f1.add(key + target.getLexFileName());

            }
        }
    }

    @Override
    public String getName() {
        return "#wn#";
    }

    /**
     * Feature types as used in {@link WordNetFeatureExtractor#addFeatureType(WordNetFeatureClass)}.
     * These specify different types of features available from WordNet.
     *
     * All features classes containing 'firstSense' in the name apply only on the first sense in the
     * synset. Names with 'allSenses' apply to all senses in the synset.
     *
     * TODO: document this more fully.
     */
    public enum WordNetFeatureClass {
        /**
         * Specifies if the word is in WordNet
         */
        existsEntry,

        /**
         * Specifies the lemma
         */
        lemma,

        /**
         * Specifies lemmas of synonyms in the first synset of this word
         */
        synonymsFirstSense,

        hypernymsFirstSense,

        /**
         * Specifies the key of the first synset of this word
         */
        synsetsFirstSense,

        partHolonymsFirstSense,

        substanceHolonymsFirstSense,

        memberHolonymsFirstSense,

        pointersFirstSense,

        pointersAllSenses,

        verbFramesFirstSense,

        /**
         * Specifies lemmas of synonyms in the all synsets of this word
         */
        synonymsAllSenses,

        hypernymsAllSenses,

        partHolonymsAllSenses,

        substanceHolonymsAllSenses,

        memberHolonymsAllSenses,

        /**
         * Specifies the key of each synset of this word
         */
        synsetsAllSenses,

        lexicographerFileNamesFirstSense,

        lexicographerFileNamesAllSenses,

        hypernymFirstSenseLexicographerFileNames,

        hypernymAllSensesLexicographerFileNames,

        partHolonymsFirstSenseLexicographerFileNames,

        substanceHolonymsFirstSenseLexicographerFileNames,

        memberHolonymsFirstSenseLexicographerFileNames,

        partHolonymsAllSensesLexicographerFileNames,

        substanceHolonymsAllSensesLexicographerFileNames,

        memberHolonymsAllSensesLexicographerFileNames,

        verbFramesAllSenses

    }
}
