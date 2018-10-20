/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.nlp.utilities;

/**
 * WARNING: These modifications change character offsets.
 *
 * @author Vivek Srikumar
 *         <p>
 *         Jun 30, 2009
 */
public class SentenceUtils {
    public static String makeSentencePresentable(String sentence) {
        sentence = sentence.replaceAll("` ", "'");
        sentence = sentence.replaceAll("`` ", "\"");
        sentence = sentence.replaceAll(" ''", "\"");

        sentence = sentence.replaceAll("-LRB-", "(");
        sentence = sentence.replaceAll("-RRB-", ")");

        sentence = sentence.replaceAll("-LCB-", "{");
        sentence = sentence.replaceAll("-RCB-", "}");

        sentence = sentence.replaceAll("-LSB-", "[");
        sentence = sentence.replaceAll("-RSB-", "]");

        sentence = sentence.replaceAll("\\s+", " ");

        // sentence = sentence.replaceAll(" +'([a-z])", "'${1}");

        sentence = sentence.replaceAll(" '", "'");

        sentence = sentence.replaceAll(" ,", ",");
        sentence = sentence.replaceAll(" \\.", "\\.");

        sentence = sentence.replaceAll("COMMA", ",");

        return sentence;
    }

    public static String convertFromPTBBrackets(String tokenOrSentence) {
        return ParseUtils.convertBracketsFromPTBFormat(tokenOrSentence);
    }

    public static String convertBracketsToPTB(String tokenOrSentence) {
        return ParseUtils.convertBracketsToPTBFormat(tokenOrSentence);
    }
}
