/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.helpers;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vivek Srikumar
 */
public class WordHelpers {

    public static String getWord(TextAnnotation ta, int tokenId) {
        return ta.getToken(tokenId);
    }

    public static String getPOS(TextAnnotation ta, int tokenId) {
        return getTokenLabel(ta, tokenId, ViewNames.POS);
    }

    public static String getLemma(TextAnnotation ta, int tokenId) {
        return getTokenLabel(ta, tokenId, ViewNames.LEMMA).toLowerCase();
    }

    public static boolean isCapitalized(TextAnnotation ta, int tokenId) {
        String word = getWord(ta, tokenId);

        String firstCharacter = word.substring(0, 1);

        String upperCase = firstCharacter.toUpperCase();
        return upperCase.matches("[A-Z]") && upperCase.equals(firstCharacter);
    }

    public static List<String> getSynset(TextAnnotation ta, int tokenId, WordNetManager wnManager)
            throws JWNLException {

        String word = getWord(ta, tokenId).toLowerCase();
        String wordPOS = getPOS(ta, tokenId);

        if (POSUtils.isPOSOpenSet(wordPOS)) {
            POS wnPos = getWNPOS(wordPOS);
            return wnManager.getSynonyms(word, wnPos, false);
        } else {
            return new ArrayList<>();
        }
    }

    public static List<String> getSynsetMostFrequentSense(TextAnnotation ta, int tokenId,
            WordNetManager wnManager) throws JWNLException {

        String word = getWord(ta, tokenId).toLowerCase();
        String wordPOS = getPOS(ta, tokenId);

        if (POSUtils.isPOSOpenSet(wordPOS)) {
            POS wnPos = getWNPOS(wordPOS);
            return wnManager.getSynonyms(word, wnPos, true);
        } else {
            return new ArrayList<>();
        }
    }

    public static List<String> getHypernyms(TextAnnotation ta, int tokenId, WordNetManager wnManager)
            throws JWNLException {

        String word = getWord(ta, tokenId).toLowerCase();
        String wordPOS = getPOS(ta, tokenId);

        if (POSUtils.isPOSOpenSet(wordPOS)) {
            POS wnPos = getWNPOS(wordPOS);
            return wnManager.getHypernyms(word, wnPos, true);
        } else {
            return new ArrayList<>();
        }
    }

    public static List<String> getHypernymsMostFrequentSense(TextAnnotation ta, int tokenId,
            WordNetManager wnManager) throws JWNLException {

        String word = getWord(ta, tokenId).toLowerCase();
        String wordPOS = getPOS(ta, tokenId);

        if (POSUtils.isPOSOpenSet(wordPOS)) {
            POS wnPos = getWNPOS(wordPOS);
            return wnManager.getHypernyms(word, wnPos, false);
        } else {
            return new ArrayList<>();
        }
    }

    private static String getTokenLabel(TextAnnotation ta, int tokenId, String viewName) {
        return ((TokenLabelView) (ta.getView(viewName))).getLabel(tokenId);
    }

    private static net.didion.jwnl.data.POS getWNPOS(String pos) {
        if (POSUtils.isPOSNoun(pos))
            return net.didion.jwnl.data.POS.NOUN;
        else if (POSUtils.isPOSVerb(pos))
            return net.didion.jwnl.data.POS.VERB;
        else if (POSUtils.isPOSAdjective(pos))
            return net.didion.jwnl.data.POS.ADJECTIVE;
        else if (POSUtils.isPOSAdverb(pos))
            return net.didion.jwnl.data.POS.ADVERB;
        else
            return null;
    }
}
