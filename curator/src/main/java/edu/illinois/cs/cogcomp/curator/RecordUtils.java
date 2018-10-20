/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.curator;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.thrift.base.*;
import edu.illinois.cs.cogcomp.thrift.curator.Record;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecordUtils {


    public static List<String> getSentenceList(Record record) {
        if (record.getLabelViews() == null
                || !record.getLabelViews().containsKey(ViewNames.SENTENCE))
            return null;
        List<String> sentenceList = new LinkedList<>();
        String rawText = record.getRawText();

        Labeling sentenceLab = record.getLabelViews().get(ViewNames.SENTENCE);
        for (Span sent : sentenceLab.getLabels()) {
            sentenceList.add(rawText.substring(sent.getStart(), sent.getEnding()));
        }
        return sentenceList;
    }

    public static Labeling tokenize(List<String> sentences) {
        Labeling labeling = new Labeling();
        List<Span> tokens = new ArrayList<>();
        int offset = 0;
        for (String sentence : sentences) {
            Matcher m = Pattern.compile("\\S+").matcher(sentence);
            while (m.find()) {
                int start = m.start() + offset;
                int end = m.end() + offset;
                Span token = new Span(start, end);
                tokens.add(token);
            }
            offset += sentence.length() + 1;
        }
        labeling.setLabels(tokens);
        labeling.setSource(getSourceIdentifier());
        return labeling;
    }

    public static Labeling sentences(List<String> sentences) {
        Labeling labeling = new Labeling();
        List<Span> sents = new ArrayList<>();
        int offset = 0;
        for (String sentence : sentences) {
            int start = offset;
            int end = sentence.length() + offset;
            Span sent = new Span(start, end);
            sents.add(sent);
            offset += sentence.length() + 1;
        }
        labeling.setLabels(sents);
        labeling.setSource(getSourceIdentifier());
        return labeling;
    }

    private static String getSourceIdentifier() {
        return "whitespacetokenizer-0.1";
    }
}
