/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TreeView;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This abstract feature extractor checks if the constituent's label in the parse tree satisfies
 * some property. If so, it adds the string specified in the constructor as a feature.
 *
 * @author Vivek Srikumar
 */
public abstract class ParseLabelIdentifier implements FeatureExtractor {

    private String label;
    private String parseViewName;

    public ParseLabelIdentifier(String parseViewName, String label) {
        this.parseViewName = parseViewName;
        this.label = label;

    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        TextAnnotation ta = c.getTextAnnotation();
        TreeView parse = (TreeView) ta.getView(parseViewName);

        String l;
        try {
            l = parse.getParsePhrase(c).getLabel();
        } catch (Exception e) {
            throw new EdisonException(e);
        }

        boolean found = isLabelValid(l);

        Set<Feature> features = new LinkedHashSet<>();
        if (found) {
            features.add(DiscreteFeature.create(label));
        }

        return features;

    }

    protected abstract boolean isLabelValid(String l);

    @Override
    public String getName() {
        return "#" + parseViewName + "label?#";
    }

}
