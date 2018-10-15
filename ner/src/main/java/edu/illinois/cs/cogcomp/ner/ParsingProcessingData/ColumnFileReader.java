/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.ner.ParsingProcessingData;


import edu.illinois.cs.cogcomp.ner.LbjTagger.NERDocument;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.ner.LbjTagger.ParametersForLbjCode;
import edu.illinois.cs.cogcomp.lbjava.nlp.ColumnFormat;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;

import java.util.ArrayList;


class ColumnFileReader extends ColumnFormat {
    String filename = null;
    ParametersForLbjCode params = null;
    public ColumnFileReader(String file, ParametersForLbjCode params) {
        super(file);
        filename = file;
        this.params = params;
    }

    int linec = 0;
    public Object next() {
        String token = null;
        String pos = null;
        String label = null;
        linec++;
        // Skip to start of next line, skip unnecessary blank lines, headers and so on.
        String[] line = (String[]) super.next();
        while (line != null && (line.length == 0 || (line.length > 4 && line[4].equals("-X-")))) {
            line = (String[]) super.next();
            linec++;
        }
        if (line == null)
            return null;

        // parse the data, CoNLL 2002 or CoNLL 2003.
        if (line.length == 2) {
            token = line[0];
            label = line[1];
        } else {
            token = line[5];
            label = line[0];
            pos = line[4];
        }

        LinkedVector res = new LinkedVector();
        NEWord w = new NEWord(new Word(token, pos), null, label);
        NEWord.addTokenToSentence(res, w.form, w.neLabel, params);
        for (line = (String[]) super.next(); line != null && line.length > 0; line =
                (String[]) super.next()) {
            linec++;

            // parse the data, CoNLL 2002 or CoNLL 2003.
            if (line.length == 2) {
                token = line[0];
                label = line[1];
            } else  if (line.length > 5) {
                token = line[5];
                label = line[0];
                pos = line[4];
            } else {
                System.out.println("Line "+linec+" in "+filename+" is wrong with "+line.length);
                for (String a : line) System.out.print(":"+a);
                System.out.println();
                continue;
            }
            w = new NEWord(new Word(token, pos), null, label);
            NEWord.addTokenToSentence(res, w.form, w.neLabel, params);
        }
        if (res.size() == 0)
            return null;
        return res;
    }

    /*
     * documentName is basically the nickname of the data. It doesn't have to be the physical
     * location of the file on the disk...
     */
    public NERDocument read(String documentName) {
        ArrayList<LinkedVector> res = new ArrayList<>();
        for (LinkedVector vector = (LinkedVector) this.next(); vector != null; vector =
                (LinkedVector) this.next())
            res.add(vector);
        return new NERDocument(res, documentName);
    }
}
