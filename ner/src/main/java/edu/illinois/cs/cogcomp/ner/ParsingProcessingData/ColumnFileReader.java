/**
 * This software is released under the University of Illinois/Research and
 *  Academic Use License. See the LICENSE file in the root folder for details.
 * Copyright (c) 2016
 *
 * Developed by:
 * The Cognitive Computation Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.ner.ParsingProcessingData;


import edu.illinois.cs.cogcomp.ner.LbjTagger.NERDocument;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;
import edu.illinois.cs.cogcomp.lbjava.nlp.ColumnFormat;
import edu.illinois.cs.cogcomp.lbjava.nlp.Word;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;

import java.util.ArrayList;


class ColumnFileReader extends ColumnFormat {
    String filename = null;

    public ColumnFileReader(String file) {
        super(file);
        filename = file;
    }

    public Object next() {
        // System.out.println("next");
        String[] line = (String[]) super.next();
        while (line != null && (line.length == 0 || line[4].equals("-X-")))
            line = (String[]) super.next();
        if (line == null)
            return null;

        LinkedVector res = new LinkedVector();
        NEWord w = new NEWord(new Word(line[5], line[4]), null, line[0]);
        NEWord.addTokenToSentence(res, w.form, w.neLabel);

        for (line = (String[]) super.next(); line != null && line.length > 0; line =
                (String[]) super.next()) {
            w = new NEWord(new Word(line[5], line[4]), null, line[0]);
            NEWord.addTokenToSentence(res, w.form, w.neLabel);
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
