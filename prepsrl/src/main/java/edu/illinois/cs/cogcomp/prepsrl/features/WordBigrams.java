/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.prepsrl.features;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotationUtilities;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.transformers.ITransformer;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.helpers.FeatureNGramUtility;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class WordBigrams extends LBJavaFeatureExtractor {
    @Override
    public Set<Feature> getFeatures(Constituent instance) throws EdisonException {
        Set<Feature> features = new LinkedHashSet<>();
        View tokens = instance.getTextAnnotation().getView(ViewNames.TOKENS);
        List<Constituent> list =
                tokens.getConstituentsCoveringSpan(instance.getStartSpan(), instance.getEndSpan());

        list.sort(TextAnnotationUtilities.constituentStartComparator);
        ITransformer<Constituent, String> surfaceFormTransformer =
                new ITransformer<Constituent, String>() {
                    public String transform(Constituent input) {
                        return input.getSurfaceForm();
                    }
                };
        features.addAll(FeatureNGramUtility.getNgramsOrdered(list, 1, surfaceFormTransformer));
        features.addAll(FeatureNGramUtility.getNgramsOrdered(list, 2, surfaceFormTransformer));
        return features;
    }

    @Override
    public String getName() {
        return "#word-2gram";
    }
}
