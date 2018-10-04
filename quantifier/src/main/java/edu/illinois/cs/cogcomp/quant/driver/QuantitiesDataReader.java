/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.quant.driver;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link TextAnnotation}-based BIO data reader. <b>NB:</b> This reader creates a
 * {@link TokenLabelView} (meaning that each token will get a separate BIO label).
 */
public class QuantitiesDataReader extends DataReader {

    public QuantitiesDataReader(String file, String corpusName) {
        super(file, corpusName, ViewNames.QUANTITIES);
    }

    public List<TextAnnotation> readData() {
        String corpusId = IOUtils.getFileName(file);
        List<String> lines;
        try {
            lines = LineIO.read(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Couldn't read " + file);
        }
        List<TextAnnotation> textAnnotations = new ArrayList<TextAnnotation>();
        // token POS label
        List<String> tokens = new ArrayList<String>();
        List<String> labels = new ArrayList<String>();
        int taId = 0;
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                List<String[]> tokenizedSentence =
                        Collections.singletonList(tokens.toArray(new String[tokens.size()]));
                TextAnnotation ta =
                        BasicTextAnnotationBuilder.createTextAnnotationFromTokens(corpusId,
                                String.valueOf(taId), tokenizedSentence);
                addGoldView(ta, labels);
                textAnnotations.add(ta);
                tokens.clear();
                labels.clear();
                taId++;
            } else {
                String[] split = line.split("\\s+");
                tokens.add(split[0]);
                labels.add(split[2]);
            }
        }
        return textAnnotations;
    }

    @Override
    public List<Constituent> candidateGenerator(TextAnnotation ta) {
        return getFinalCandidates(ta.getView(viewName), ta.getView(ViewNames.TOKENS)
                .getConstituents());
    }

    protected void addGoldView(TextAnnotation ta, List<String> labels) {
        TokenLabelView posView = new TokenLabelView(viewName, ta);
        List<Constituent> constituents = ta.getView(ViewNames.TOKENS).getConstituents();
        for (int i = 0; i < constituents.size(); ++i) {
            Constituent constituent = (Constituent) constituents.get(i);
            posView.addTokenLabel(constituent.getStartSpan(), labels.get(i), 1.0D);
        }
        ta.addView(viewName, posView);
    }
}
