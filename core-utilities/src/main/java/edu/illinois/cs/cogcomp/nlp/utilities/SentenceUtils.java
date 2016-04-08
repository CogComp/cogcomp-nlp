package edu.illinois.cs.cogcomp.nlp.utilities;

/**
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
