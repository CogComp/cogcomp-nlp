/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.depparse.core;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.sl.core.IInstance;

import java.util.Arrays;

/**
 * Represents a sentence which is to be parsed, with some additional features (pos,lemmas)
 * 
 * @author Shyam
 *
 */
public class DepInst implements IInstance {
    // all these fields have length # of tokens +1
    public String[] forms;
    public String[] strLemmas;
    public String[] strPos;
    public String[] strChunk;
    public String[] deprels;
    public int[] heads;

    public DepInst(String[] forms, String[] lemmas, String[] postags, String[] chunks,
            String[] labs, int[] heads) {
        this.forms = forms;
        this.strLemmas = lemmas;
        this.strPos = postags;
        this.strChunk = chunks;
        this.deprels = labs;
        this.heads = heads;
    }

    public DepInst(TextAnnotation annotation) {
        String[] tokens = annotation.getTokens();
        int lenOfTokens = tokens.length;

        forms = new String[lenOfTokens + 1];
        strLemmas = new String[lenOfTokens + 1];
        strPos = new String[lenOfTokens + 1];
        strChunk = new String[lenOfTokens + 1];
        deprels = new String[lenOfTokens + 1];
        heads = new int[lenOfTokens + 1];

        forms[0] = "<root>";
        strLemmas[0] = "<root>";
        strPos[0] = "<root-POS>";
        strChunk[0] = "<root-CHUNK>";

        // Assume that POS and SHALLOW_PARSE Views exist
        SpanLabelView LemmaView = (SpanLabelView) annotation.getView(ViewNames.LEMMA);
        SpanLabelView POSView = (SpanLabelView) annotation.getView(ViewNames.POS);
        SpanLabelView ChunkView = (SpanLabelView) annotation.getView(ViewNames.SHALLOW_PARSE);

        System.arraycopy(tokens, 0, forms, 1, lenOfTokens);

        for (int i = 1; i < strLemmas.length; i++) {
            strLemmas[i] = LemmaView.getLabel(i - 1);
            strPos[i] = POSView.getLabel(i - 1);
            strChunk[i] = ChunkView.getLabel(i - 1);
        }

        Arrays.fill(deprels, "<no-type>");
        Arrays.fill(heads, -1);
    }

    public int size() {
        return forms.length - 1; // this is the true size, after removing the 0 root
    }
}
