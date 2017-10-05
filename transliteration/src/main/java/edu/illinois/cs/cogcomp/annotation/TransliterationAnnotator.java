/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.annotation;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.transliteration.SPModel;
import edu.illinois.cs.cogcomp.utils.TopList;
import jdk.nashorn.internal.parser.Token;

import java.io.IOException;

public class TransliterationAnnotator extends Annotator {

    SPModel model;

    public TransliterationAnnotator() {
        super(ViewNames.TRANSLITERATION, new String[0]);
    }

    public TransliterationAnnotator(boolean lazilyInitialize) {
        super(ViewNames.TRANSLITERATION, new String[0], lazilyInitialize);
    }

    @Override
    public void initialize(ResourceManager rm) {
        try {
            model = new SPModel(rm.getString(TransliterationConfigurator.MODEL_PATH.key));
            model.setMaxCandidates(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void addView(TextAnnotation ta) throws AnnotatorException {

        View v = new TokenLabelView(ViewNames.TRANSLITERATION, this.getClass().getName(), ta, 1.0);

        int index = 0;
        for(String tok : ta.getTokens()){
            try {
                TopList<Double, String> ll = model.Generate(tok.toLowerCase());
                if(ll.size() > 0) {
                    Pair<Double, String> toppair = ll.getFirst();
                    Constituent c = new Constituent(toppair.getSecond(), toppair.getFirst(), ViewNames.TRANSLITERATION, ta, index, index + 1);
                    v.addConstituent(c);
                }
            } catch (Exception e) {
                // print that this word has failed...
                e.printStackTrace();
            }

            index++;
        }
        ta.addView(ViewNames.TRANSLITERATION, v);
    }
}
