/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.pos;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Convenience classes for mapping between data structures used by Curator and LBJ. Obsolete (?)
 * when using new AnnotatorService pipeline architecture.
 *
 * Created by mssammon on 8/24/15.
 */

public class LBJavaUtils {

    /**
     * Converts a record into LBJ Tokens for use with LBJ classifiers. If part of speech is present
     * in record, it is added to the LBJ tokens.
     */
    public static List<Token> recordToLBJTokens(TextAnnotation record) {
        List<Token> lbjTokens = new LinkedList<>();
        List<List<String>> sentences =
                tokensAsStrings(record.getView(ViewNames.TOKENS).getConstituents(),
                        record.getView(ViewNames.SENTENCE).getConstituents(), record.getText());

        List<Constituent> tags = null;

        if (record.hasView(ViewNames.POS))
            tags = record.getView(ViewNames.POS).getConstituents();

        int tagIndex = 0;

        for (List<String> sentence : sentences) {
            boolean opendblquote = true;
            Word wprevious = null;
            Token tprevious = null;
            for (String token : sentence) {
                if (token.equals("\"")) {
                    token = opendblquote ? "``" : "''";
                    opendblquote = !opendblquote;
                } else if (token.equals("(")) {
                    token = "-LRB-";
                } else if (token.equals(")")) {
                    token = "-RRB-";
                } else if (token.equals("{")) {
                    token = "-LCB-";
                } else if (token.equals("}")) {
                    token = "-RCB-";
                } else if (token.equals("[")) {
                    token = "-LSB-";
                } else if (token.equals("]")) {
                    token = "-RSB-";
                }

                Word wcurrent = new Word(token, wprevious);
                if (null != tags && !tags.isEmpty()) {
                    Constituent tag = tags.get(tagIndex++);
                    wcurrent.partOfSpeech = tag.getLabel();
                }

                Token tcurrent = new Token(wcurrent, tprevious, "");
                lbjTokens.add(tcurrent);
                if (tprevious != null) {
                    tprevious.next = tcurrent;
                }
                wprevious = wcurrent;
                tprevious = tcurrent;
            }
        }
        return lbjTokens;
    }

    /**
     * Converts sentences and tokens represented as spans into a list of lists of string.
     */
    public static List<List<String>> tokensAsStrings(List<Constituent> tokens,
            List<Constituent> sentences, String rawText) {
        List<List<String>> strTokens = new ArrayList<>();
        int sentNum = 0;
        Constituent sentence = sentences.get(sentNum);
        strTokens.add(new ArrayList<String>());
        for (Constituent token : tokens) {
            if (token.getStartSpan() >= sentence.getEndSpan()) {
                strTokens.add(new ArrayList<String>());
                sentNum++;
                sentence = sentences.get(sentNum);
            }
            strTokens.get(sentNum).add(
                    rawText.substring(token.getStartCharOffset(), token.getEndCharOffset()));
        }
        return strTokens;
    }

}
