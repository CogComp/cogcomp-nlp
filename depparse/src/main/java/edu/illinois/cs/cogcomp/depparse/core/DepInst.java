package edu.illinois.cs.cogcomp.depparse.core;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.depparse.io.CONLLReader;
import edu.illinois.cs.cogcomp.edison.features.factory.BrownClusterFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.SLParameters;

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
    public String[] strBrown;
    public String[] strChunk;
    public String[] deprels;
    public int[] lemmas;
    public int[] lemmasPrefix;
    public int[] pos;
    public int[] heads;

    public DepInst(String[] forms, String[] lemmas, String[] postags, String[] browns,
            String[] chunks, String[] labs, int[] heads) {
        this.forms = forms;
        this.strLemmas = lemmas;
        this.strPos = postags;
        this.strBrown = browns;
        this.strChunk = chunks;
        this.deprels = labs;
        this.heads = heads;

        this.lemmas = new int[lemmas.length];
        this.lemmasPrefix = new int[lemmas.length];
        this.pos = new int[postags.length];

        for (int i = 0; i < forms.length; i++) {
            this.lemmas[i] = encodeString(lemmas[i]);
            pos[i] = encodeString(postags[i]);
            if (lemmas[i].length() > 5)
                lemmasPrefix[i] = encodeString(lemmas[i].substring(0, 5));
            else
                lemmasPrefix[i] = encodeString(lemmas[i]);
        }
    }

    public DepInst(TextAnnotation annotation) {
        String[] tokens = annotation.getTokens();
        int lenOfTokens = tokens.length;

        forms = new String[lenOfTokens + 1];
        strLemmas = new String[lenOfTokens + 1];
        strPos = new String[lenOfTokens + 1];
        strBrown = new String[lenOfTokens + 1];
        strChunk = new String[lenOfTokens + 1];
        deprels = new String[lenOfTokens + 1];
        pos = new int[lenOfTokens + 1];
        lemmas = new int[lenOfTokens + 1];
        lemmasPrefix = new int[lenOfTokens + 1];
        heads = new int[lenOfTokens + 1];

        forms[0] = "<root>";
        strLemmas[0] = "<root>";
        strPos[0] = "<root-POS>";
        strBrown[0] = "<root-BROWN>";
        strChunk[0] = "<root-CHUNK>";

        // Assume that POS and SHALLOW_PARSE Views exist
        SpanLabelView LemmaView = (SpanLabelView) annotation.getView(ViewNames.LEMMA);
        SpanLabelView POSView = (SpanLabelView) annotation.getView(ViewNames.POS);
        SpanLabelView ChunkView = (SpanLabelView) annotation.getView(ViewNames.SHALLOW_PARSE);

        System.arraycopy(tokens, 0, forms, 1, lenOfTokens);

        for (int i = 1; i < strBrown.length; i++) {
            try {
                strBrown[i] =
                        CONLLReader.getBrownPrefix(BrownClusterFeatureExtractor.instance320
                                .getWordFeatures(annotation, i - 1));
            } catch (EdisonException e) {
                e.printStackTrace();
            }
            strLemmas[i] = LemmaView.getLabel(i - 1);
            strPos[i] = POSView.getLabel(i - 1);
            strChunk[i] = ChunkView.getLabel(i - 1);
        }

        Arrays.fill(deprels, "<no-type>");
        Arrays.fill(heads, -1);

        for (int i = 0; i < lenOfTokens + 1; i++) {
            lemmas[i] = encodeString(strLemmas[i]);
            pos[i] = encodeString(strPos[i]);
            if (strLemmas[i].length() > 5)
                lemmasPrefix[i] = encodeString(strLemmas[i].substring(0, 5));
            else
                lemmasPrefix[i] = encodeString(strLemmas[i]);
        }
    }

    public int size() {
        return forms.length - 1; // this is the true size, after removing the 0 root
    }

    private int encodeString(String str) {
        int hashcode = 0;
        for (byte c : str.getBytes()) {
            hashcode += c;
            hashcode = hashcode * 191;
        }
        return hashcode & SLParameters.HASHING_MASK;
    }
}
