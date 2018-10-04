/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ParsingProcessingData;

import edu.illinois.cs.cogcomp.ner.IO.InFile;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.lbjava.nlp.Sentence;
import edu.illinois.cs.cogcomp.lbjava.nlp.SentenceSplitter;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

public class PlainTextReader {
    public static ArrayList<LinkedVector> parsePlainTextFile(String file, ParametersForLbjCode cp) {
        InFile in = new InFile(file);
        String line = in.readLine();
        StringBuilder buf = new StringBuilder(100000);
        while (line != null) {
            buf.append(line).append(" \n");
            line = in.readLine();
        }
        buf.append(" ");
        in.close();
        return parseText(normalizeText(buf.toString(), cp), cp);
    }

    public static ArrayList<LinkedVector> parseText(String text, ParametersForLbjCode cp) {
        Vector<Vector<String>> processed = sentenceSplitAndTokenizeText(text, cp);
        ArrayList<LinkedVector> res = new ArrayList<>();
        for (int i = 0; i < processed.size(); i++) {
            LinkedVector sentence = new LinkedVector();
            for (int j = 0; j < processed.elementAt(i).size(); j++)
                NEWord.addTokenToSentence(sentence, processed.elementAt(i).elementAt(j),
                        "unlabeled", cp);
            res.add(sentence);
        }
        TaggedDataReader.connectSentenceBoundaries(res);
        return res;
    }

    /**
     * This method will normalize and parse the raw text returning a representation of sentences,
     * where each sentence is a primitive array of words as strings. This representation is more
     * compatible with the new core data structures which no long take vectors.
     * 
     * @param text the text to parse.
     * @return a list of sentences represented as an array of words.
     */
    public static List<String[]> parseTextRaw(String text, ParametersForLbjCode cp) {
        text = normalizeText(text, cp);
        ArrayList<String> sentences1 = new ArrayList<>();// sentences split by newlines. will keep
                                                         // just one element- the text if no
                                                         // sentence splitting on newlines is
                                                         // used...
        if (cp.forceNewSentenceOnLineBreaks
                || cp.keepOriginalFileTokenizationAndSentenceSplitting) {
            StringTokenizer st = new StringTokenizer(text, "\n");
            while (st.hasMoreTokens())
                sentences1.add(st.nextToken());
        } else
            sentences1.add(text);

        ArrayList<String> sentences2 = new ArrayList<>();// we add Lbj sentence splitting on
                                                         // top.
        if (!cp.keepOriginalFileTokenizationAndSentenceSplitting) {
            for (String aSentences1 : sentences1) {
                SentenceSplitter parser = new SentenceSplitter(new String[] {aSentences1});
                Sentence s = (Sentence) parser.next();
                while (s != null) {
                    sentences2.add(s.text);
                    s = (Sentence) parser.next();
                }
            }
        } else
            sentences2 = sentences1;

        ArrayList<String[]> res = new ArrayList<>();
        // tokenizing
        for (String sentenceText : sentences2) {
            if (sentenceText.length() > 0) {
                // adding the space before the final period in the sentence,
                // this is just a formatting issue with LBJ sentence splitter that can happen
                if (sentenceText.charAt(sentenceText.length() - 1) == '.'
                        && !cp.keepOriginalFileTokenizationAndSentenceSplitting)
                    sentenceText = sentenceText.substring(0, sentenceText.length() - 1) + " . ";
                // now tokenizing for real...

                String[] sentence = sentenceText.split("[ \\n\\t]");
                if (sentence.length > 0) {
                    // fixing a bug in LBJ sentence splitter if needed
                    if ((!cp.keepOriginalFileTokenizationAndSentenceSplitting)
                            && sentence.length == 1
                            && res.size() > 0
                            && (sentence[0].equals("\"") || sentence[0].equals("''") || sentence[0]
                                    .equals("'"))) {

                        int where = res.size() - 1;
                        String[] tmp = res.remove(where);
                        if (tmp == null) {
                            tmp = new String[0];
                        }
                        int len = tmp.length;
                        String[] newtmp = new String[len + 1];
                        System.arraycopy(tmp, 0, newtmp, 0, len);
                        newtmp[len] = sentence[0];
                        res.add(newtmp);
                    } else
                        res.add(sentence);
                }
            }
        }
        return res;

    }

    public static Vector<Vector<String>> sentenceSplitAndTokenizeText(String text, ParametersForLbjCode cp) {
        text = normalizeText(text, cp);
        Vector<String> sentences1 = new Vector<>();// sentences split by newlines. will keep just
                                                   // one element- the text if no sentence splitting
                                                   // on newlines is used...
        if (cp.forceNewSentenceOnLineBreaks || cp.keepOriginalFileTokenizationAndSentenceSplitting) {
            StringTokenizer st = new StringTokenizer(text, "\n");
            while (st.hasMoreTokens())
                sentences1.addElement(st.nextToken());
        } else
            sentences1.addElement(text);

        Vector<String> sentences2 = new Vector<>();// we add Lbj sentence splitting on top.
        if (!cp.keepOriginalFileTokenizationAndSentenceSplitting) {
            for (int i = 0; i < sentences1.size(); i++) {
                SentenceSplitter parser =
                        new SentenceSplitter(new String[] {sentences1.elementAt(i)});
                Sentence s = (Sentence) parser.next();
                while (s != null) {
                    sentences2.addElement(s.text);
                    s = (Sentence) parser.next();
                }
            }
        } else
            sentences2 = sentences1;
        Vector<Vector<String>> res = new Vector<>();
        // tokenizing
        for (int i = 0; i < sentences2.size(); i++) {
            String sentenceText = sentences2.elementAt(i);
            if (sentenceText.length() > 0) {
                // adding the space before the final period in the sentence,
                // this is just a formatting issue with LBJ sentence splitter that can happen
                if (sentenceText.charAt(sentenceText.length() - 1) == '.'
                        && !cp.keepOriginalFileTokenizationAndSentenceSplitting)
                    sentenceText = sentenceText.substring(0, sentenceText.length() - 1) + " . ";
                // now tokenizing for real...
                StringTokenizer st = new StringTokenizer(sentenceText, " \n\t");
                Vector<String> sentence = new Vector<>();
                while (st.hasMoreTokens())
                    sentence.addElement(st.nextToken());
                if (sentence.size() > 0) {
                    // fixing a bug in LBJ sentence splitter if needed
                    if ((!cp.keepOriginalFileTokenizationAndSentenceSplitting)
                            && sentence.size() == 1
                            && res.size() > 0
                            && (sentence.elementAt(0).equals("\"")
                                    || sentence.elementAt(0).equals("''") || sentence.elementAt(0)
                                    .equals("'")))
                        res.elementAt(res.size() - 1).add(sentence.elementAt(0));
                    else
                        res.addElement(sentence);
                }
            }
        }
        return res;
    }

    public static String normalizeText(String text, ParametersForLbjCode cp) {
        if (cp.keepOriginalFileTokenizationAndSentenceSplitting)
            return text;
        StringBuilder buf = new StringBuilder((int) (text.length() * 1.2));
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ',') {
                if (i > 0 && i < text.length() - 1 && Character.isDigit(text.charAt(i - 1))
                        && Character.isDigit(text.charAt(i + 1)))
                    buf.append(",");
                else
                    buf.append(" , ");
            } else {
                buf.append(text.charAt(i));
            }
        }
        text = buf.toString().replace("\"", " \" ");
        text = text.replace("`", "'");
        text = text.replace("``", "\"");
        text = text.replace("&quot;", "\"");
        text = text.replace("&QUOT;", "\"");
        text = text.replace("&amp;", "&");
        text = text.replace("&AMP;", "&");
        text = text.replace("&HT;", " ");
        text = text.replace("&ht;", " ");
        text = text.replace("&MD;", "--");
        text = text.replace("&md;", "--");
        text = text.replace("-LRB-", "(");
        text = text.replace("-RRB-", ")");
        text = text.replace("-LCB-", "{");
        text = text.replace("-RCB-", "}");
        text = text.replace("'nt", " 'nt ");
        text = text.replace("'s", " 's ");
        text = text.replace("'d", " 'd ");
        text = text.replace("'m", " 'm ");
        text = text.replace("'ve", " 've ");
        text = text.replace("``", " \" ");
        text = text.replace("''", " \" ");
        text = text.replace(";", " ; ");
        text = text.replace("]", " ] ");
        // now, I want to replace all '[' by ' [ ', but I have to be careful with chunk markers!
        for (int i = 0; i < cp.labelTypes.length; i++)
            text =
                    text.replace("[" + cp.labelTypes[i],
                            "_START_" + cp.labelTypes[i] + "_");
        text = text.replace("[", " [ ");
        for (int i = 0; i < cp.labelTypes.length; i++)
            text =
                    text.replace("_START_" + cp.labelTypes[i]
                            + "_", " [" + cp.labelTypes[i]);
        text = text.replace(")", " ) ");
        text = text.replace("(", " ( ");
        text = text.replace("{", " { ");
        text = text.replace("}", " } ");
        text = text.replace("?", " ? ");
        text = text.replace("!", " ! ");
        return text;
    }


    public static String showSentenceVector(Vector<LinkedVector> sentences) {
        String display = "";

        for (LinkedVector v : sentences) {
            for (int i = 0; i < v.size(); ++i) {
                NEWord s = (NEWord) (v.get(i));
                display += (s.toString());
            }
        }
        return display;
    }

}
