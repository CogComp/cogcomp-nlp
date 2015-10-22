package edu.illinois.cs.cogcomp.nlp.utility;

import edu.illinois.cs.cogcomp.thrift.base.Labeling;
import edu.illinois.cs.cogcomp.thrift.base.Span;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.nlp.reader.OntoNotesDataModel;

import java.util.ArrayList;

public class Utility {
    static public Record parseIntoCuratorRecord(ArrayList<OntoNotesDataModel> sentences) {
        Record article = new Record();
        Labeling sentencesLabeling = new Labeling();
        Labeling tokensLabeling = new Labeling();

        ArrayList<Span> sentencesList = new ArrayList<Span>();
        ArrayList<Span> tokensList = new ArrayList<Span>();

        for (int i = 0; i < sentences.size(); i++) {
            for (int j = 0; j < sentences.get(i).getTokens().size(); j++) {
                Span eachToken = new Span();
                eachToken.setSource(sentences.get(i).getTokens().get(j));
                eachToken.setStart(sentences.get(i).getStartOffsets().get(j));
                eachToken.setEnding(sentences.get(i).getEndOffsets().get(j));
                tokensList.add(eachToken);
            }

            Span eachSentence = new Span();
            eachSentence.setSource(sentences.get(i).getPlainSentence());
            eachSentence.setStart(sentences.get(i).getSentenceStartOffset());
            eachSentence.setEnding(sentences.get(i).getSentenceEndOffset());
            sentencesList.add(eachSentence);
        }

        sentencesLabeling.setLabels(sentencesList);
        tokensLabeling.setLabels(tokensList);

        article.putToLabelViews("sentences", sentencesLabeling);
        article.putToLabelViews("tokens", tokensLabeling);

//        System.out.println(article.getLabelViews().get("sentences").toString());
//        System.out.println(article.getLabelViews().get("tokens").toString());

        return article;
    }

    static public void computeCharacterOffsets(ArrayList<OntoNotesDataModel> sentences) {
        int sentence_start_offset = 0;
        for (int i = 0; i < sentences.size(); i++) {
            sentence_start_offset = computeCharacterOffsetsForEachSentence(sentences, i, sentence_start_offset);
            sentences.get(i).setSentenceStartOffset(sentences.get(i).getStartOffsets().get(0));
            sentences.get(i).setSentenceEndOffset(sentences.get(i).getEndOffsets().get(sentences.get(i).getEndOffsets().size()-1));
        }
    }

    static private int computeCharacterOffsetsForEachSentence(ArrayList<OntoNotesDataModel> sentences, int index, int sentence_start_offset) {
        String sentence = sentences.get(index).getPlainSentence();
        int currentStart = sentence_start_offset, currentEnd = sentence_start_offset;

        for (String eachToken : sentences.get(index).getTokens()) {
            for (int i = 0; i < eachToken.length(); i++) {
                if (sentence.charAt(currentEnd-sentence_start_offset) == eachToken.charAt(i)) {
                    currentEnd ++;
                }
            }
            if (currentEnd-sentence_start_offset != sentence.length()) {
                sentences.get(index).addAStartOffset(currentStart);
                sentences.get(index).addAnEndOffset(currentEnd);
                if (sentence.charAt(currentEnd-sentence_start_offset) == ' ') {
                    currentEnd++;
                    currentStart = currentEnd;
                }
                else {
                    currentStart = currentEnd;
                }
            }
            else {
                sentences.get(index).addAStartOffset(currentStart);
                sentences.get(index).addAnEndOffset(currentEnd);
            }
        }
        return currentEnd+1;
    }
}
