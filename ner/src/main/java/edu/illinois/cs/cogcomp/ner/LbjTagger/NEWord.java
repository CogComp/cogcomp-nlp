/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.LbjTagger;


import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode.TokenizationScheme;
import edu.illinois.cs.cogcomp.ner.StringStatisticsUtils.CharacteristicWords;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.nlp.WordSplitter;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


public class NEWord extends Word {

    /** This field is used to store a computed named entity type tag. */
    public String neTypeLevel1;
    public String neTypeLevel2;
    public NamedEntity predictedEntity = null;// if non-null it keeps the named entity the tagger
    public ParametersForLbjCode params = null;
    public CharacteristicWords predictionConfidencesLevel1Classifier = null;
    public CharacteristicWords predictionConfidencesLevel2Classifier = null;
    public NamedEntity goldEntity = null;// if non-null it keeps the named entity the tagger
    /** This field stores the named entity type tag found in labeled data. */
    public String neLabel = null;

    /**
     * contains the parts of this word if DualTokenization is enabled, otherwise a waste of space.
     * used by the Affixes and FormParts classifiers. This appears to be an additional tokenization
     * of what was initially split as an individual word, and results in the addition of entries in
     * the feature vector for Affixes classifier. The dual tokenization could be performed during
     * Affix and FormParts classification, presumable, this might result is performing this
     * operation twice in those cases where both classifiers are utilized.
     */
    public String[] parts;
    /** used by wikipedia linkability. */
    public String normalizedMostLinkableExpression = null;
    public ArrayList<String> gazetteers;

    public String[] wikifierFeatures = null;

    /** these are referencence to previous and next words, ignoring sentence boundries. */
    public NEWord nextIgnoreSentenceBoundary = null;
    public NEWord previousIgnoreSentenceBoundary = null;
    public ArrayList<RealFeature> level1AggregationFeatures = null;
    public String form = null;// override the Word.form field!
    public String originalForm = null;// what was the form that we read from the file
    public String normalizedForm = null;// after the title normalization stage
    public boolean isCaseNormalized = false;
    private HashMap<String, Integer> nonLocalFeatures = null;
    private String[] nonLocFeatArray = null;

    /*
     * This stuff was added for form normalization purposes.
     */

    /**
     * An <code>NEWord</code> can be constructed from a <code>Word</code> object representing the
     * same word, an <code>NEWord</code> representing the previous word in the sentence, and the
     * named entity type label found in the data.
     *
     * @param w Represents the same word as the <code>NEWord</code> being constructed.
     * @param p The previous word in the sentence.
     * @param type The named entity type label for this word from the data.
     **/
    public NEWord(Word w, NEWord p, String type) {
        super(w.form, w.partOfSpeech, w.lemma, w.wordSense, p, w.start, w.end);
        form = w.form;
        originalForm = w.form;
        neLabel = type;
        neTypeLevel1 = null;
    }

    /**
     * Add the provided token to the sentence, for also do any additional word spliting.
     *
     * @param sentence the sentence to add the word to.
     * @param token the individual token.
     * @param tag the tag to annotate the word with.
     */
    public static void addTokenToSentence(LinkedVector sentence, String token, String tag, ParametersForLbjCode prs) {
        NEWord word = new NEWord(new Word(token), null, tag);
        word.params = prs;
        addTokenToSentence(sentence, word);
    }

    public static void addTokenToSentence(LinkedVector sentence, NEWord word) {
        Vector<NEWord> v = NEWord.splitWord(word);
        if (word.params.tokenizationScheme
                .equals(TokenizationScheme.DualTokenizationScheme)) {
            sentence.add(word);
            word.parts = new String[v.size()];
            for (int j = 0; j < v.size(); j++)
                word.parts[j] = v.elementAt(j).form;
        } else {
            if (word.params.tokenizationScheme
                    .equals(TokenizationScheme.LbjTokenizationScheme)) {
                for (int j = 0; j < v.size(); j++)
                    sentence.add(v.elementAt(j));
            } else {
                System.err
                        .println("Fatal error in BracketFileManager.readAndAnnotate - unrecognized tokenization scheme: "
                                + word.params.tokenizationScheme);
                System.exit(0);
            }
        }
    }

    /*
     * Used for some tokenization schemes.
     */
    private static Vector<NEWord> splitWord(NEWord word) {
        String[] sentence = {word.form + " "};
        Parser parser = new WordSplitter(new SentenceSplitter(sentence));
        LinkedVector words = (LinkedVector) parser.next();
        Vector<NEWord> res = new Vector<>();
        if (words == null) {
            res.add(word);
            return res;
        }
        String label = word.neLabel;
        for (int i = 0; i < words.size(); i++) {
            if (label.contains("B-") && i > 0)
                label = "I-" + label.substring(2);
            NEWord w = new NEWord(new Word(((Word) words.get(i)).form), null, label);
            res.addElement(w);
        }
        return res;
    }

    public HashMap<String, Integer> getNonLocalFeatures() {
        if (nonLocalFeatures == null)
            nonLocalFeatures = new HashMap<>(0);
        return nonLocalFeatures;
    }

    public ArrayList<RealFeature> getLevel1AggregationFeatures() {
        if (level1AggregationFeatures == null)
            level1AggregationFeatures = new ArrayList<>(0);
        return level1AggregationFeatures;
    }

    public ArrayList<RealFeature> resetLevel1AggregationFeatures() {
        level1AggregationFeatures = new ArrayList<>(0);
        return level1AggregationFeatures;
    }

    /**
     * Produces a simple <code>String</code> representation of this word in which the
     * <code>neLabel</code> field appears followed by the word's part of speech and finally the form
     * (i.e., spelling) of the word all surrounded by parentheses.
     **/
    public String toString() {
        return "(" + neLabel + " " + partOfSpeech + " " + form + ")";
    }

    public String[] getAllNonlocalFeatures() {
        if (nonLocFeatArray == null) {
            Vector<String> v = new Vector<>();
            for (Iterator<String> i = getNonLocalFeatures().keySet().iterator(); i.hasNext(); v
                    .addElement(i.next()));
            nonLocFeatArray = new String[v.size()];
            for (int i = 0; i < v.size(); i++)
                nonLocFeatArray[i] = v.elementAt(i);
        }
        return nonLocFeatArray;
    }

    public int getNonLocFeatCount(String nonLocFeat) {
        return this.getNonLocalFeatures().get(nonLocFeat);
    }

    public String getPrediction(LabelToLookAt labelType) {
        if (labelType == LabelToLookAt.GoldLabel)
            return this.neLabel;
        if (labelType == LabelToLookAt.PredictionLevel1Tagger)
            return this.neTypeLevel1;
        if (labelType == LabelToLookAt.PredictionLevel2Tagger)
            return this.neTypeLevel2;
        return null;
    }

    public void setPrediction(String label, LabelToLookAt labelType) {
        if (labelType == LabelToLookAt.GoldLabel)
            this.neLabel = label;
        if (labelType == LabelToLookAt.PredictionLevel1Tagger)
            this.neTypeLevel1 = label;
        if (labelType == LabelToLookAt.PredictionLevel2Tagger)
            this.neTypeLevel2 = label;
    }


    public enum LabelToLookAt {
        PredictionLevel2Tagger, PredictionLevel1Tagger, GoldLabel
    }

    public static class DiscreteFeature {
        public String featureValue;
        public String featureGroupName;
        public boolean useWithinTokenWindow = false; // generate this feature for a window of +-2
                                                     // tokens
    }

    public static class RealFeature {
        public double featureValue;
        public String featureGroupName;
        public boolean useWithinTokenWindow = false; // generate this feature for a window of +-2
                                                     // tokens

        public RealFeature(double _featureValue, String _featureGroupName) {
            this.featureValue = _featureValue;
            this.featureGroupName = _featureGroupName;
        }
    }
}
