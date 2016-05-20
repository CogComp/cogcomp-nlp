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
package edu.illinois.cs.cogcomp.edison.annotators;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

/**
 * Adds a {@link ViewNames#LEMMA} view using the Porter stemming algorithm
 *
 * @author Vivek Srikumar
 * @deprecated Use {@code illinois-lemmatizer} instead
 */
public class PorterStemmer extends Annotator {

    private static PorterStemmer instance = null; // = new PorterStemmer();

    private final static SnowballStemmer stemmer = new englishStemmer();

    public static PorterStemmer getInstance() {
        if (null == instance)
            instance = new PorterStemmer(ViewNames.LEMMA, new String[] {});
        return instance;
    }

    private PorterStemmer(String viewName, String[] prerequisiteViews) {
        super(viewName, prerequisiteViews);
    }

    @Override
    public void addView(TextAnnotation input) {
        TokenLabelView view = new TokenLabelView(getViewName(), "PorterStemmer", input, 1.0);

        synchronized (instance) {
            for (int i = 0; i < input.size(); i++) {
                stemmer.setCurrent(input.getToken(i));

                stemmer.stem();

                view.addTokenLabel(i, stemmer.getCurrent(), 1.0);
            }
        }
        input.addView(getViewName(), view);
    }


}
