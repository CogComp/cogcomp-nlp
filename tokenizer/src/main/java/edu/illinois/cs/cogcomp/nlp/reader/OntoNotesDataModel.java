package edu.illinois.cs.cogcomp.nlp.reader;

import java.util.ArrayList;

public class OntoNotesDataModel {
    private String plainSentence;
    private ArrayList<String> tokens;
    private ArrayList<Integer> startOffsets;
    private ArrayList<Integer> endOffsets;
    private int sentenceStartOffset;
    private int sentenceEndOffset;

    public OntoNotesDataModel() {
        tokens = new ArrayList<String>();
        startOffsets = new ArrayList<Integer>();
        endOffsets = new ArrayList<Integer>();
    }

    public String getPlainSentence() {
        return plainSentence;
    }

    public void setPlainSentence(String plainSentence) {
        this.plainSentence = plainSentence;
    }

    public ArrayList<String> getTokens() {
        return tokens;
    }

    public void addAToken(String token) {
        tokens.add(token);
    }

    public ArrayList<Integer> getStartOffsets() {
        return startOffsets;
    }

    public void addAStartOffset(int start) {
        startOffsets.add(start);
    }

    public ArrayList<Integer> getEndOffsets() {
        return endOffsets;
    }

    public void addAnEndOffset(int end) {
        endOffsets.add(end);
    }

    public int getSentenceStartOffset() {
        return sentenceStartOffset;
    }

    public void setSentenceStartOffset(int sentenceStartOffset) {
        this.sentenceStartOffset = sentenceStartOffset;
    }

    public int getSentenceEndOffset(){
        return sentenceEndOffset;
    }

    public void setSentenceEndOffset(int sentenceEndOffset) {
        this.sentenceEndOffset = sentenceEndOffset;
    }
}
