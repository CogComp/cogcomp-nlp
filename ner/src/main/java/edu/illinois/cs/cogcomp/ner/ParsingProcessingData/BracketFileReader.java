/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ParsingProcessingData;

import edu.illinois.cs.cogcomp.ner.IO.InFile;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NERDocument;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

class BracketFileReader {
    private static Logger logger = LoggerFactory.getLogger(BracketFileReader.class);

    public static NERDocument read(String fileName, String docname, ParametersForLbjCode cp) throws Exception {
        logger.info("Reading the file: " + fileName);
        String annotatedText = PlainTextReader.normalizeText(getFileText(fileName), cp);
        return parseTextWithBrackets(annotatedText, docname, cp);
    }

    public static NERDocument parseTextWithBrackets(String annotatedText, String docname, ParametersForLbjCode cp)
            throws Exception {
        if (annotatedText.replace(" ", "").replace("\n", "").replace("\t", "").length() == 0)
            return new NERDocument(new ArrayList<LinkedVector>(), docname);
        Vector<String> bracketTokens = new Vector<>();// can include newlines!!!!
        Vector<String> bracketTokensTags = new Vector<>();
        parseBracketsAnnotatedText(annotatedText, bracketTokensTags, bracketTokens, cp);
        StringBuilder buff = new StringBuilder(bracketTokens.size() * 20);
        for (int i = 0; i < bracketTokens.size(); i++)
            buff.append(bracketTokens.elementAt(i)).append(" ");
        // the tokens below will have no newline characters.
        // logger.info("Raw text: "+buff);
        Vector<Vector<String>> parsedTokens =
                PlainTextReader.sentenceSplitAndTokenizeText(buff.toString(), cp);
        // now we need to align the bracket tokens to the sentence split and tokenized tokens.
        // there are two issues to be careful with -
        // 1) The bracket tokens may have newline characters as individual tokens, the others will
        // not
        // 2) The tokenized/sentence split tokens may be bracket tokens broken into separate tokens.
        Vector<String> parsedTokensFlat = new Vector<>();
        for (int i = 0; i < parsedTokens.size(); i++)
            for (int j = 0; j < parsedTokens.elementAt(i).size(); j++)
                parsedTokensFlat.addElement(parsedTokens.elementAt(i).elementAt(j));
        // logger.info("----"+parsedTokensFlat.size());
        Vector<String> parsedTokensTagsFlat = new Vector<>();// to be filled later
        StringBuilder bracketTokensText = new StringBuilder(bracketTokens.size() * 20);
        StringBuilder parsedTokensText = new StringBuilder(parsedTokensFlat.size() * 20);
        int bracketsTokensPos = 0;
        int parsedTokensPos = 0;
        while (bracketsTokensPos < bracketTokens.size()) {
            while (bracketsTokensPos < bracketTokens.size()
                    && bracketTokens.elementAt(bracketsTokensPos).equals("\n"))
                bracketsTokensPos++;
            if (bracketsTokensPos < bracketTokens.size()) {
                bracketTokensText.append(" ").append(bracketTokens.elementAt(bracketsTokensPos));
                String currentLabel = bracketTokensTags.elementAt(bracketsTokensPos);
                parsedTokensTagsFlat.addElement(currentLabel);
                parsedTokensText.append(" ").append(parsedTokensFlat.elementAt(parsedTokensPos));
                parsedTokensPos++;
                while ((!bracketTokensText.toString().equals(parsedTokensText.toString()))
                        && parsedTokensPos < parsedTokensFlat.size()) {
                    if (currentLabel.startsWith("B-"))
                        parsedTokensTagsFlat.addElement("I-" + currentLabel.substring(2));
                    else
                        parsedTokensTagsFlat.addElement(currentLabel);
                    parsedTokensText.append(parsedTokensFlat.elementAt(parsedTokensPos));
                    parsedTokensPos++;
                }
                if (!bracketTokensText.toString().equals(parsedTokensText.toString()))
                    throw new Exception(
                            "Error aligning raw brackets tokens to token/sentence split tokens\nBrackets token text till now:\n"
                                    + bracketTokensText
                                    + "\nTokenized text till now:\n"
                                    + parsedTokensText);
                bracketsTokensPos++;
            }
        }
        // ok, we're done, just building the output sentences
        ArrayList<LinkedVector> res = new ArrayList<>();
        parsedTokensPos = 0;
        for (int i = 0; i < parsedTokens.size(); i++) {
            LinkedVector sentence = new LinkedVector();
            for (int j = 0; j < parsedTokens.elementAt(i).size(); j++) {
                NEWord.addTokenToSentence(sentence, parsedTokensFlat.elementAt(parsedTokensPos),
                        parsedTokensTagsFlat.elementAt(parsedTokensPos), cp);
                parsedTokensPos++;
            }
            res.add(sentence);
        }
        return new NERDocument(res, docname);
    }

    /**
     * note that this one will do very little normalization/tokenization and token splitting. these
     * fancy stuff is done after we get the brackets files tokens and tags. it is important however
     * to keep the newline token to know where to split the sentences if we trust newlines as new
     * sentence starts.
     */
    public static void parseBracketsAnnotatedText(String text, Vector<String> tags,
            Vector<String> words, ParametersForLbjCode cp) {
        // Add spaces before and after each bracket, except for after open bracket [
        text = text.replace("]", " ] ");
        for (int i = 0; i < cp.labelTypes.length; i++)
            text =
                    text.replace("[" + cp.labelTypes[i], " ["  + cp.labelTypes[i] + " ");

        Vector<String> tokens = new Vector<>();
        text = PlainTextReader.normalizeText(text, cp);
        StringTokenizer stLines = new StringTokenizer(text, "\n");
        while (stLines.hasMoreTokens()) {
            String line = stLines.nextToken();
            StringTokenizer st = new StringTokenizer(line, " \t");
            while (st.hasMoreTokens())
                tokens.addElement(st.nextToken());
            if (cp.forceNewSentenceOnLineBreaks
                    || cp.keepOriginalFileTokenizationAndSentenceSplitting)
                tokens.addElement("\n");
        }
        for (int i = 0; i < tokens.size(); i++) {
            boolean added = false;
            for (int labelType = 0; labelType < cp.labelTypes.length; labelType++) {
                if (tokens.elementAt(i).equals(
                        "[" + cp.labelTypes[labelType])) {
                    i++;
                    boolean first = true;
                    while (!tokens.elementAt(i).equals("]")) {
                        words.addElement(tokens.elementAt(i));
                        if (first) {
                            tags.addElement("B-"
                                    + cp.labelTypes[labelType]);
                            first = false;
                        } else {
                            tags.addElement("I-"
                                    + cp.labelTypes[labelType]);
                        }
                        i++;
                    }
                    added = true;
                }
            }
            if (!added) {
                words.addElement(tokens.elementAt(i));
                tags.addElement("O");
            }
        }// looping on the tokens
    }// func -parseBracketsAnnotatedText



    private static String getFileText(String file) {
        StringBuilder res = new StringBuilder(200000);
        InFile in = new InFile(file);
        String line = in.readLine();
        while (line != null) {
            res.append(line).append("\n");
            line = in.readLine();
        }
        in.close();
        return res.toString();
    }
}
