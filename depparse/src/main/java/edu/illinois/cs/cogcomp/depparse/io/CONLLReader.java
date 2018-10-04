/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.depparse.io;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.depparse.core.DepInst;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CONLLReader {
    // The column indices of the various items
    private static final int FORM_INDEX = 1;
    private int POS_INDEX = 3;
    private int HEAD_INDEX = 6;
    private int DEP_INDEX = 7;

    private BufferedReader inputReader;
    private Preprocessor preprocessor;
    private boolean useGoldPOS;

    private static final String CORPUS_ID = "CoNLL";
    private int sentId;

    public CONLLReader(Preprocessor preprocessor, boolean useGoldPOS, int offset) {
        this.preprocessor = preprocessor;
        this.useGoldPOS = useGoldPOS;
        this.sentId = 0;
        POS_INDEX += offset;
        HEAD_INDEX += offset;
        DEP_INDEX += offset;
    }

    public DepInst getNext() throws IOException, AnnotatorException, EdisonException {
        ArrayList<String[]> lineList = new ArrayList<>();

        String line = inputReader.readLine();
        while (line != null && !line.equals("") && !line.startsWith("*")) {
            lineList.add(line.split("\\s+"));
            line = inputReader.readLine();
            sentId++;
        }

        int length = lineList.size();

        if (length == 0) {
            inputReader.close();
            return null;
        }

        String[] forms = new String[length + 1]; // +1 for the 0 root
        String[] lemmas = new String[length + 1];
        String[] pos = new String[length + 1];
        String[] chunks = new String[length + 1];
        String[] deprels = new String[length + 1];
        int[] heads = new int[length + 1];

        forms[0] = "<root>";
        pos[0] = "<root-POS>";
        deprels[0] = "<no-type>";
        heads[0] = -1;
        lemmas[0] = "<root>";
        pos[0] = "<root-POS>";
        chunks[0] = "<root-CHUNK>";

        for (int i = 0; i < length; i++) {
            String[] info = lineList.get(i);
            forms[i + 1] = normalize(info[FORM_INDEX]);
            pos[i + 1] = info[POS_INDEX];
            deprels[i + 1] = info[DEP_INDEX];
            heads[i + 1] = Integer.parseInt(info[HEAD_INDEX]);
        }

        TextAnnotation annotation = preprocessor.annotate(CORPUS_ID, String.valueOf(sentId), forms);

        SpanLabelView lemmaView = (SpanLabelView) annotation.getView(ViewNames.LEMMA);
        SpanLabelView posView = (SpanLabelView) annotation.getView(ViewNames.POS);
        SpanLabelView chunkView = (SpanLabelView) annotation.getView(ViewNames.SHALLOW_PARSE);
        for (int i = 0; i < chunks.length - 1; i++) {
            lemmas[i + 1] = lemmaView.getLabel(i);
            if (!useGoldPOS)
                pos[i + 1] = posView.getLabel(i);
            chunks[i + 1] = chunkView.getLabel(i);
        }

        return new DepInst(forms, lemmas, pos, chunks, deprels, heads);
    }

    private String normalize(String s) {
        if (s.matches("[0-9]+|[0-9]+\\.[0-9]+|[0-9]+[0-9,]+"))
            return "<num>";

        return s;
    }

    public void startReading(String file) throws IOException {
        inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
    }
}
