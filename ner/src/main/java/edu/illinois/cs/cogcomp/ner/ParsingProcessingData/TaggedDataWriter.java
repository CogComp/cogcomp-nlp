/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ParsingProcessingData;

import edu.illinois.cs.cogcomp.ner.IO.OutFile;
import edu.illinois.cs.cogcomp.ner.LbjTagger.Data;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;

import java.io.IOException;

public class TaggedDataWriter {
    public static void writeToFile(String outputFile, Data data, String fileFormat,
            NEWord.LabelToLookAt labelType) throws IOException {
        OutFile out = new OutFile(outputFile);
        if (fileFormat.equalsIgnoreCase("-r"))
            out.println(toBracketsFormat(data, labelType));
        else {
            if (fileFormat.equalsIgnoreCase("-c"))
                out.println(toColumnsFormat(data, labelType));
            else {
                throw new IOException(
                        "Unknown file format (only options -r and -c are supported): " + fileFormat);
            }
        }
        out.close();
    }

    /*
     * labelType=NEWord.GoldLabel/NEWord.PredictionLevel2Tagger/NEWord.PredictionLevel1Tagger
     * 
     * Note : the only reason this function is public is because we want to be able to use it in the
     * demo and insert html tags into the string
     */
    public static String toBracketsFormat(Data data, NEWord.LabelToLookAt labelType) {
        StringBuilder res = new StringBuilder(data.documents.size() * 1000);
        for (int did = 0; did < data.documents.size(); did++) {
            for (int i = 0; i < data.documents.get(did).sentences.size(); i++) {
                LinkedVector vector = data.documents.get(did).sentences.get(i);
                boolean open = false;
                String[] predictions = new String[vector.size()];
                String[] words = new String[vector.size()];
                for (int j = 0; j < vector.size(); j++) {
                    predictions[j] = null;
                    if (labelType == NEWord.LabelToLookAt.PredictionLevel2Tagger)
                        predictions[j] = ((NEWord) vector.get(j)).neTypeLevel2;
                    if (labelType == NEWord.LabelToLookAt.PredictionLevel1Tagger)
                        predictions[j] = ((NEWord) vector.get(j)).neTypeLevel1;
                    if (labelType == NEWord.LabelToLookAt.GoldLabel)
                        predictions[j] = ((NEWord) vector.get(j)).neLabel;
                    words[j] = ((NEWord) vector.get(j)).form;
                }
                for (int j = 0; j < vector.size(); j++) {
                    if (predictions[j].startsWith("B-")
                            || (j > 0 && predictions[j].startsWith("I-") && (!predictions[j - 1]
                                    .endsWith(predictions[j].substring(2))))) {
                        res.append("[").append(predictions[j].substring(2)).append(" ");
                        open = true;
                    }
                    res.append(words[j]).append(" ");
                    if (open) {
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
                            res.append(" ] ");
                            open = false;
                        }
                    }
                }
                res.append("\n");
            }
        }
        return res.toString();
    }

    private static String toColumnsFormat(Data data, NEWord.LabelToLookAt labelType) {
        StringBuilder res = new StringBuilder(data.documents.size() * 1000);
        for (int did = 0; did < data.documents.size(); did++) {
            for (int i = 0; i < data.documents.get(did).sentences.size(); i++) {
                LinkedVector vector = data.documents.get(did).sentences.get(i);
                if (((NEWord) vector.get(0)).previousIgnoreSentenceBoundary == null)
                    res.append("O	0	0	O	-X-	-DOCSTART-	x	x	0\n\n");
                for (int j = 0; j < vector.size(); j++) {
                    NEWord w = (NEWord) vector.get(j);
                    res.append(w.getPrediction(labelType)).append("\t0\t").append(j)
                            .append("\tO\tO\t").append(w.form).append("\tx\tx\t0\n");
                }
                res.append("\n");
            }
        }
        return res.toString();
    }
}
