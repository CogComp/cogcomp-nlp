package edu.illinois.cs.cogcomp.ner.LbjTagger;

import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;

import java.util.ArrayList;

public class NERDocument {
    public String docname;

    public NERDocument(ArrayList<LinkedVector> vector, String documentName) {
        docname = documentName;
        sentences = vector;
    }

    public ArrayList<LinkedVector> sentences;
}
