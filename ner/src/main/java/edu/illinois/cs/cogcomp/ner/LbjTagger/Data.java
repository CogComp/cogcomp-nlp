/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.LbjTagger;

import edu.illinois.cs.cogcomp.ner.ParsingProcessingData.TaggedDataReader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;


public class Data {
    public String nickname;// this will be used to save the model to know on what dataset we have
                           // tuned....
    public String pathToData;
    public String datasetPath;
    public ArrayList<NERDocument> documents = new ArrayList<>();
    HashSet<String> labelsToIgnoreForEvaluation = new HashSet<>();
    HashSet<String> labelsToAnonymizeForEvaluation = new HashSet<>();

    private Data(Data other) {
    }


    public Data() {
        datasetPath = "missing";
        nickname = "missing";
    }

    public Data(NERDocument doc) {
        datasetPath = "missing";
        nickname = "missing";
        documents.add(doc);
    }

    public Data(String pathToData, String nickname, String dataFormat,
            Vector<String> labelsToIgnoreForEvaluation,
            Vector<String> labelsToAnonymizeForEvaluation, ParametersForLbjCode params) throws Exception {
        this.datasetPath = pathToData;
        this.nickname = nickname;
        this.pathToData = pathToData;
        if ((new File(pathToData)).isDirectory()) {
            Vector<NERDocument> docs = TaggedDataReader.readFolder(pathToData, dataFormat, params);
            for (int i = 0; i < docs.size(); i++)
                documents.add(docs.elementAt(i));
        } else {
            int idx =
                    Math.max(Math.max(0, pathToData.lastIndexOf("/")), pathToData.lastIndexOf('\\'));
            String docname = pathToData.substring(idx);
            documents.add(TaggedDataReader.readFile(pathToData, dataFormat, docname, params));
        }
        setLabelsToIgnore(labelsToIgnoreForEvaluation);
        setLabelsToAnonymize(labelsToAnonymizeForEvaluation);
    }

    public Data(String pathToData, String nickname, String dataFormat, String[] labelsToIgnoreForEvaluation, 
            String[] labelsToAnonymizeForEvaluation, ParametersForLbjCode params) throws Exception {
        this.datasetPath = pathToData;
        this.nickname = nickname;
        if ((new File(pathToData)).isDirectory()) {
            Vector<NERDocument> docs = TaggedDataReader.readFolder(pathToData, dataFormat, params);
            for (int i = 0; i < docs.size(); i++)
                documents.add(docs.elementAt(i));
        } else {
            int idx =
                    Math.max(Math.max(0, pathToData.lastIndexOf("/")), pathToData.lastIndexOf('\\'));
            String docname = pathToData.substring(idx);
            documents.add(TaggedDataReader.readFile(pathToData, dataFormat, docname, params));
        }
        setLabelsToIgnore(labelsToIgnoreForEvaluation);
        setLabelsToAnonymize(labelsToAnonymizeForEvaluation);
    }

    public void setLabelsToIgnore(Vector<String> labelsToIgnoreForEvaluation) {
        this.labelsToIgnoreForEvaluation = new HashSet<>();
        if (labelsToIgnoreForEvaluation != null) {
            for (int i = 0; i < labelsToIgnoreForEvaluation.size(); i++) {
                this.labelsToIgnoreForEvaluation.add(labelsToIgnoreForEvaluation.elementAt(i));
            }
        }
    }

    public void setLabelsToAnonymize(Vector<String> labelsToAnonymizeForEvaluation) {
        this.labelsToAnonymizeForEvaluation = new HashSet<>();
        if (labelsToAnonymizeForEvaluation != null) {
            for (int i = 0; i < labelsToAnonymizeForEvaluation.size(); i++) {
                this.labelsToAnonymizeForEvaluation
                        .add(labelsToAnonymizeForEvaluation.elementAt(i));
            }
        }
    }

    public void setLabelsToIgnore(String[] labelsToIgnoreForEvaluation) {
        this.labelsToIgnoreForEvaluation = new HashSet<>();
        if (labelsToIgnoreForEvaluation != null) {
            for (String label : labelsToIgnoreForEvaluation) {
                this.labelsToIgnoreForEvaluation.add(label);
            }
        }
    }

    public void setLabelsToAnonymize(String[] labelsToAnonymizeForEvaluation) {
        this.labelsToAnonymizeForEvaluation = new HashSet<>();
        if (labelsToAnonymizeForEvaluation != null) {
            for (String label : labelsToAnonymizeForEvaluation) {
                this.labelsToAnonymizeForEvaluation.add(label);
            }
        }
    }
}
