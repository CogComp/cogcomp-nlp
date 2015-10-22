package edu.illinois.cs.cogcomp.nlp.tokenizer;

import edu.illinois.cs.cogcomp.lbjava.nlp.Sentence;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.nlp.reader.OntoNotesDataModel;
import edu.illinois.cs.cogcomp.nlp.utility.Utility;

import java.util.ArrayList;

public class IllinoisTokenizerPrev {

    private ArrayList<OntoNotesDataModel> sentences;



    /**
     * Builds a tokenizer for a sentence array using the LBJava word splitter.
     * @param sentenceRawTextArray
     */
    public IllinoisTokenizerPrev(ArrayList<String> sentenceRawTextArray) {
        sentences = new ArrayList<OntoNotesDataModel>();

        for (String eachSentenceString : sentenceRawTextArray) {
            OntoNotesDataModel eachModel = new OntoNotesDataModel();
            eachModel.setPlainSentence(eachSentenceString);

            Sentence eachSentence = new Sentence(eachSentenceString);

            for (int i = 0; i < eachSentence.wordSplit().size(); i++) {
                Word eachToken = (Word) eachSentence.wordSplit().get(i);
                eachModel.addAToken(eachToken.form);
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
