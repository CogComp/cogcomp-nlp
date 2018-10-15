/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.utilities;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;

import java.util.*;

/**
 * This class represents a collection of utility functions related to POS tags. Functions from
 * ParseTreeProperties will eventually move here.
 *
 * @author Vivek Srikumar
 */
public class POSUtils {

    private static Set<String> posTagSet;

    private final static List<String> punctuationPOS = Arrays.asList("''", "``", ",", ":", ".",
            "-LRB-", "-RRB-", "-LCB-", "-RCB-", "-LSB-", "-RSB-");

    public final static Set<String> closedSetPOS = Collections.unmodifiableSet(new HashSet<>(Arrays
            .asList("CC", "DT", "IN", "PDT", "POS", "TO", "UH", "SYM", "WDT", "WP", "WP$", "WRB")));

    public static final List<String> allPOS = Arrays.asList("#", "$", "``", "''", ",", "-LRB-",
            "-RRB-", ".", ":", "CC", "CD", "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD",
            "NN", "NNP", "NNPS", "NNS", "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP",
            "SYM", "TO", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB");

    /**
     * Very simple way of testing whether something is a part of speech tag. Just look up a list of
     * allowed POS tags and see if the label is in it.
     */
    public static boolean isPOSTag(String token) {
        if (posTagSet == null) {
            initializePOSTagSet();

        }
        return posTagSet.contains(token);
    }

    /**
     * Converts the input part of speech tag into a coarse set, consisting of the labels Noun, Verb,
     * Adjective, Adverb, Punctuation, Pronoun and Other
     */
    public static String getCoarsePOS(String pos) {
        if (isPOSNoun(pos))
            return "Noun";
        else if (isPOSVerb(pos))
            return "Verb";
        else if (isPOSAdjective(pos))
            return "Adjective";
        else if (isPOSAdverb(pos))
            return "Adverb";
        else if (isPOSPunctuation(pos))
            return "Punctuation";
        else if (isPOSPronoun(pos))
            return "Pronoun";
        else
            return "Other";
    }

    public static boolean isPOSOpenSet(String token) {
        return isPOSTag(token)
                && (isPOSNoun(token) || isPOSAdjective(token) || isPOSVerb(token) || isPOSAdverb(token));
    }

    public static boolean isPOSClosedSet(String token) {
        return isPOSTag(token) && !isPOSOpenSet(token);

    }

    public static boolean isPOSNumber(String token) {
        return isPOSTag(token) && token.equals("CD");
    }

    public static boolean isPOSPunctuation(String label) {
        return punctuationPOS.contains(label);
    }

    public static boolean isPOSPossessive(String label) {
        return label.equals("POS") || label.equals("PRP$");
    }

    public static boolean isPOSAdjective(String token) {
        return token.startsWith("JJ");
    }

    public static boolean isPOSNoun(String token) {
        return token.startsWith("NN");
    }

    public static boolean isPOSPronoun(String token) {
        return token.equals("PRP");
    }

    public static boolean isPOSPossessivePronoun(String token) {
        return token.equals("PRP$");
    }

    public static boolean isPOSAdverb(String token) {
        return token.startsWith("RB");
    }

    public static boolean isPOSPreposition(String token) {
        return token.equals("TO") || token.equals("IN");
    }

    public static boolean isPOSVerb(String token) {
        return token.startsWith("VB") || token.startsWith("MD") || token.startsWith("AUX");
    }

    private static void initializePOSTagSet() {
        posTagSet = new HashSet<>();
        for (String str : allPOS) {
            posTagSet.add(str);
        }

        posTagSet.add("AUX");

    }

    public static String getPOS(TextAnnotation ta, int tokenId) {
        return ((TokenLabelView) (ta.getView(ViewNames.POS))).getLabel(tokenId);
    }
}
