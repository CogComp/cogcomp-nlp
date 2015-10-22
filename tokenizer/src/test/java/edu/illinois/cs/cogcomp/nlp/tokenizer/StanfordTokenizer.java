package edu.illinois.cs.cogcomp.nlp.tokenizer;

import java.io.*;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.nlp.reader.OntoNotesDataModel;
import edu.illinois.cs.cogcomp.nlp.utility.Utility;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

public class StanfordTokenizer {

    private ArrayList<OntoNotesDataModel> sentences;

    /**
     * Builds a tokenizer for a sentence array using Stanford's Penn Treebank (PTB) tokenizer.
     * @param sentenceRawTextArray
     */
    public StanfordTokenizer(ArrayList<String> sentenceRawTextArray) {
        sentences = new ArrayList<OntoNotesDataModel>();

        for (String eachRawText : sentenceRawTextArray) {
            OntoNotesDataModel eachModel = new OntoNotesDataModel();
            eachModel.setPlainSentence(eachRawText);
            StringReader reader = new StringReader(eachRawText);
            PTBTokenizer ptbTokenizer = new PTBTokenizer(reader, new CoreLabelTokenFactory(), "");
            for (CoreLabel label; ptbTokenizer.hasNext(); ) {
                label = (CoreLabel) ptbTokenizer.next();
                eachModel.addAToken(label.word());
                //System.out.println(label.word());
            }
            sentences.add(eachModel);
        }
        Utility.computeCharacterOffsets(sentences);
//        for (OntoNotesDataModel eachEntry : sentences) {
//            System.out.println(eachEntry.getPlainSentence()+"\n");
//            System.out.println(eachEntry.getTokens().toString()+"\n");
//            System.out.println(eachEntry.getStartOffsets().toString()+"\n");
//            System.out.println(eachEntry.getEndOffsets().toString() + "\n");
//            System.out.println("start: "+eachEntry.getSentenceStartOffset()+", end: "+eachEntry.getSentenceEndOffset()+"\n\n");
//        }
    }

    public Record parseIntoCuratorRecord() {
        return Utility.parseIntoCuratorRecord(sentences);
    }

}
