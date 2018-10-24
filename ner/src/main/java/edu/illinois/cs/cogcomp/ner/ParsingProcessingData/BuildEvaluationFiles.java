/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ParsingProcessingData;

import edu.illinois.cs.cogcomp.ner.IO.OutFile;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.ner.StringStatisticsUtils.OccurrenceCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;


public class BuildEvaluationFiles {
    private static Logger logger = LoggerFactory.getLogger(BuildEvaluationFiles.class);

    public static void main(String[] args) {
        buildEvaluationFile(args[0], args[1], args[2]);
    }

    public static void buildEvaluationFile(String goldFile, String taggedFile, String outFile) {
        String[] goldFiles = {goldFile};
        String[] taggedFiles = {taggedFile};
        buildEvaluationFile(goldFiles, taggedFiles, outFile);
    }

    public static void buildEvaluationFile(String[] goldFiles, String[] taggedFiles, String outFile) {
        OutFile outPhrase = new OutFile(outFile + ".phraseLevel");
        OutFile outToken = new OutFile(outFile + ".tokenLevel");
        for (int i = 0; i < goldFiles.length; i++)
            appendToEvaluationFile(goldFiles[i], taggedFiles[i], outPhrase, outToken);
        outPhrase.close();
        outToken.close();
    }

    public static void appendToEvaluationFile(String goldFile, String taggedFile,
            OutFile outPhrase, OutFile outToken) {
        ParametersForLbjCode cp = new ParametersForLbjCode();
        Vector<String> goldTags = new Vector<>();
        Vector<String> goldWords = new Vector<>();
        BracketFileReader.parseBracketsAnnotatedText(goldFile, goldTags, goldWords, cp);
        Vector<String> tempgoldTags = new Vector<>();
        Vector<String> tempgoldWords = new Vector<>();
        Hashtable<Integer, Boolean> newlines = new Hashtable<>();
        for (int i = 0; i < goldWords.size(); i++) {
            String s = cleanPunctuation(goldWords.elementAt(i));
            // if(goldWords.elementAt(i).indexOf('.')>-1||goldWords.elementAt(i).indexOf('!')>-1||goldWords.elementAt(i).indexOf('?')>-1)
            // newlines.put(tempgoldTags.size(),true);
            if (s.length() > 0) {
                tempgoldWords.addElement(s);
                tempgoldTags.addElement(goldTags.elementAt(i));
            }
        }
        goldWords = tempgoldWords;
        goldTags = tempgoldTags;


        Vector<String> resTags = new Vector<>();
        Vector<String> resWords = new Vector<>();
        BracketFileReader.parseBracketsAnnotatedText(taggedFile, resTags, resWords, cp);
        Vector<String> tempresTags = new Vector<>();
        Vector<String> tempresWords = new Vector<>();
        for (int i = 0; i < resWords.size(); i++) {
            String s = cleanPunctuation(resWords.elementAt(i));
            if (s.length() > 0) {
                tempresWords.addElement(s);
                tempresTags.addElement(resTags.elementAt(i));
            }
        }
        resWords = tempresWords;
        resTags = tempresTags;

        int gWordId = 0, gCharId = 0;
        int tWordId = 0, tCharId = 0;
        while (gWordId < goldWords.size()) {
            String gw = goldWords.elementAt(gWordId).toLowerCase();
            String rw = resWords.elementAt(tWordId).toLowerCase();
            OccurrenceCounter resTagsForCurrentToken = new OccurrenceCounter();
            while (gCharId < gw.length()) {
                if (tCharId >= rw.length()) {
                    tWordId++;
                    tCharId = 0;
                    rw = resWords.elementAt(tWordId).toLowerCase();
                }
                if (gw.charAt(gCharId) != rw.charAt(tCharId)) {
                    logger.warn("mismatched characters when building evaluation files");
                    logger.warn("the words were '" + gw + "' and: '" + rw + "'  exiting");
                    logger.warn("the characters were '" + gw.charAt(gCharId) + "' and: '"
                            + rw.charAt(tCharId) + "'  exiting");
                    outToken.close();
                    outPhrase.close();
                    System.exit(0);
                } else {
                    if (gCharId == 0) {
                        resTagsForCurrentToken.addToken(resTags.elementAt(tWordId));
                    }
                    // String lastTag=resTags.elementAt(tWordId);
                    // resTagsForCurrentToken.addToken(lastTag);
                    // logger.info(gw.charAt(gCharId)+"-"+rw.charAt(tCharId));
                }
                gCharId++;
                tCharId++;
            }
            String maxLabel = "";
            int maxCount = 0;
            for (Iterator<String> iter = resTagsForCurrentToken.getTokensIterator(); iter.hasNext();) {
                String s = iter.next();
                if (maxCount <= resTagsForCurrentToken.getCount(s)) {
                    maxCount = (int) resTagsForCurrentToken.getCount(s);
                    maxLabel = s;
                }
            }
            // if((maxLabel.indexOf("-")>-1)&&(goldTags.elementAt(gWordId).indexOf("-")>-1)
            // &&(maxLabel.substring(2)).equalsIgnoreCase(goldTags.elementAt(gWordId).substring(2)))
            // outPhrase.println(goldWords.elementAt(gWordId)+" "+goldTags.elementAt(gWordId)+" "+goldTags.elementAt(gWordId));
            // else
            outPhrase.println(goldWords.elementAt(gWordId) + " " + goldTags.elementAt(gWordId)
                    + " " + maxLabel);

            String g = goldTags.elementAt(gWordId);
            if (g.indexOf('-') > -1)
                g = g.substring(g.indexOf('-') + 1);
            if (maxLabel.indexOf('-') > -1)
                maxLabel = maxLabel.substring(maxLabel.indexOf('-') + 1);
            outToken.println(goldWords.elementAt(gWordId) + " " + g + " " + maxLabel);
            if (newlines.containsKey(gWordId)) {
                outPhrase.println("");
                outToken.println("");
            }
            gWordId++;
            gCharId = 0;
            /*
             * tCharId++; if(tCharId>=rw.length()){ tWordId++; tCharId=0;
             * if(tWordId<resWords.size()) rw=resWords.elementAt(tWordId).toLowerCase(); }
             */
        }
    }

    public static String cleanPunctuation(String s) {
        String res = "";
        String punc = "\"';:/?><,.!`~@#$%^&*()-_=+|\\/[]{}\n\t\r";
        int i = 0;
        while (i < s.length()) {
            if (punc.indexOf(s.charAt(i)) == -1)
                res = res + s.charAt(i);
            i++;
        }
        return res;
    }
}
