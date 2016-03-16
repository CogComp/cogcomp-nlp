package edu.illinois.cs.cogcomp.nlp.tokenizer;

import java.io.*;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.nlp.reader.OntoNotesDataModel;
import edu.illinois.cs.cogcomp.nlp.utility.Utility;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

public class StanfordTokenizer {

    private ArrayList<OntoNotesDataModel> sentences;

    /**
     * Builds a tokenizer for a sentence array using Stanford's Penn Treebank (PTB) tokenizer.
     */
    public StanfordTokenizer(ArrayList<String> sentenceRawTextArray) {
        sentences = new ArrayList<>();

        for (String eachRawText : sentenceRawTextArray) {
            OntoNotesDataModel eachModel = new OntoNotesDataModel();
            eachModel.setPlainSentence(eachRawText);
            StringReader reader = new StringReader(eachRawText);
            PTBTokenizer<CoreLabel> ptbTokenizer =
                    new PTBTokenizer<>(reader, new CoreLabelTokenFactory(), "");
            for (CoreLabel label; ptbTokenizer.hasNext();) {
                label = ptbTokenizer.next();
                eachModel.addAToken(label.word());
            }
            sentences.add(eachModel);
        }
        Utility.computeCharacterOffsets(sentences);
    }

    public ArrayList<OntoNotesDataModel> getSentences() {
        return sentences;
    }
}
