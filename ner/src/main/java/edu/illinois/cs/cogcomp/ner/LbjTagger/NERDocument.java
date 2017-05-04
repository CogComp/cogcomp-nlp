/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.ner.LbjTagger;

import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;

import java.util.ArrayList;

public class NERDocument {
    public String docname;
    public String domainName;

    public NERDocument(ArrayList<LinkedVector> vector, String documentName) {
        docname = documentName;
        sentences = vector;
    }

    public NERDocument(ArrayList<LinkedVector> vector, String documentName, String domainName) {
        docname = documentName;
        sentences = vector;
        this.domainName = domainName;
    }

    public ArrayList<LinkedVector> sentences;
}
