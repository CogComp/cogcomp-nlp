/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Queries;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.RealFeature;
import edu.illinois.cs.cogcomp.edison.features.helpers.SpanLabelsHelper;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.*;

/**
 * 
 * Returns a set of prefixes of length 3 and 4 and suffixes of length 1, 2, 3, and 4. This feature
 * extractor assumes that the TOKEN View have been generated. It generates features related to the
 * prefixes and suffixes of the given constituent. This constituent may not be a single word.
 *
 * @keywords affix, prefix, suffix, token
 * @author Paul Vijayakumar, Mazin Bokhari
 *
 */
public class Affixes implements FeatureExtractor {

    public static View TOKENS;

    private final String viewName;

    public Affixes(String viewName) {
        this.viewName = viewName;
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {

        String classifier = "Affixes";

        TextAnnotation ta = c.getTextAnnotation();

        TOKENS = ta.getView(ViewNames.TOKENS);

        Set<Feature> result = new LinkedHashSet<Feature>();

        String id;
        String value;
        String word = c.getSurfaceForm();

        for (int i = 3; i <= 4; ++i) {
            if (word.length() > i) {
                id = "p|";
                value = "" + (word.substring(0, i));
                result.add(new DiscreteFeature(classifier + ":" + id + "(" + value + ")"));

            }
        }
        for (int i = 1; i <= 4; ++i) {
            if (word.length() > i) {
                id = "s|";
                value = "" + (word.substring(word.length() - i));
                result.add(new DiscreteFeature(classifier + ":" + id + "(" + value + ")"));

            }
        }

        return result;
    }

    @Override
    public String getName() {
        return "#path#" + viewName;
    }
}
