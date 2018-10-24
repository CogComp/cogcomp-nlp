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

import java.util.Vector;

public class PlainTextWriter {
    public static void write(Data data, String outFile) {
        OutFile out = new OutFile(outFile);
        for (int did = 0; did < data.documents.size(); did++) {
            for (int i = 0; i < data.documents.get(did).sentences.size(); i++) {
                StringBuilder buf = new StringBuilder(2000);
                for (int j = 0; j < data.documents.get(did).sentences.get(i).size(); j++)
                    buf.append(((NEWord) data.documents.get(did).sentences.get(i).get(j)).form)
                            .append(" ");
                out.println(buf.toString());
            }
        }
        out.close();
    }

    public static void write(Vector<LinkedVector> data, String outFile) {
        OutFile out = new OutFile(outFile);
        for (int i = 0; i < data.size(); i++) {
            StringBuilder buf = new StringBuilder(2000);
            for (int j = 0; j < data.elementAt(i).size(); j++)
                buf.append(((NEWord) data.elementAt(i).get(j)).form).append(" ");
            out.println(buf.toString());
        }
        out.close();
    }
}
