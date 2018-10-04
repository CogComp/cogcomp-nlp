/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.LbjTagger;

import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class AnnotatedDocument {
    String taggedLine;
    HashMap<String, ArrayList<String>> labels;
    Data data;

    public AnnotatedDocument(Data data) {
        this.data = data;
        this.init();
    }

    public void init() {
        HashMap<String, ArrayList<String>> out = new HashMap<>();
        StringBuffer res = new StringBuffer();
        for (int docid = 0; docid < data.documents.size(); docid++) {
            ArrayList<LinkedVector> sentences = data.documents.get(docid).sentences;
            for (int i = 0; i < sentences.size(); i++) {
                LinkedVector vector = sentences.get(i);
                boolean open = false;
                String[] predictions = new String[vector.size()];
                String[] words = new String[vector.size()];
                for (int j = 0; j < vector.size(); j++) {
                    predictions[j] = ((NEWord) vector.get(j)).neTypeLevel2;
                    words[j] = ((NEWord) vector.get(j)).form;
                }
                StringBuffer entity = null;
                String tag = null;
                for (int j = 0; j < vector.size(); j++) {
                    if (predictions[j].startsWith("B-")
                            || (j > 0 && predictions[j].startsWith("I-") && (!predictions[j - 1]
                                    .endsWith(predictions[j].substring(2))))) {
                        res.append("[").append(predictions[j].substring(2)).append(" ");
                        entity = new StringBuffer();
                        open = true;
                        tag = predictions[j].substring(2);
                    }
                    res.append(words[j]).append(" ");

                    if (open) {
                        entity.append(words[j]).append(" ");
                        boolean close = false;
                        if (j == vector.size() - 1) {
                            close = true;
                        } else {
                            if (predictions[j + 1].startsWith("B-"))
                                close = true;
                            if (predictions[j + 1].equals("O"))
                                close = true;
                            if (predictions[j + 1].indexOf('-') > -1
                                    && (!predictions[j].endsWith(predictions[j + 1].substring(2))))
                                close = true;
                        }
                        if (close) {
                            String str_res = res.toString().trim();
                            res = new StringBuffer(str_res);
                            res.append("] ");
                            open = false;

                            if (out.containsKey(tag))
                                out.get(tag).add(entity.toString().trim());
                            else {
                                ArrayList<String> entities = new ArrayList<>();
                                entities.add(entity.toString().trim());
                                out.put(tag, entities);
                            }
                        }
                    }
                }
            }
        }
        taggedLine = res.toString();
        labels = out;
    }

    public void writeTagsToCSV(String filename) {

        try {
            PrintWriter writer = new PrintWriter(filename, "UTF-8");
            for (String tag : labels.keySet()) {
                writer.print(tag);
                for (String entity : labels.get(tag))
                    writer.print("," + entity);
                writer.println();
            }
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String getTaggedLine() {
        return taggedLine;
    }

    public HashMap<String, ArrayList<String>> getLabels() {
        return labels;
    }
}
