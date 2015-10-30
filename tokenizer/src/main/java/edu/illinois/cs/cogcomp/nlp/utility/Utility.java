package edu.illinois.cs.cogcomp.nlp.utility;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.reader.OntoNotesDataModel;
import edu.illinois.cs.cogcomp.nlp.utilities.BasicTextAnnotationBuilder;

import java.util.ArrayList;
import java.util.List;

public class Utility {

	public static TextAnnotation parseIntoTextAnnotation(List<OntoNotesDataModel> sentences) {
		List<String[]> tokenizedSentences = new ArrayList<>();
		for (OntoNotesDataModel sent : sentences) {
			tokenizedSentences.add(sent.getTokens().toArray(new String[sent.getTokens().size()]));
		}
		return BasicTextAnnotationBuilder.createTextAnnotationFromTokens(tokenizedSentences);
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
