/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.factory;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.PredicateArgumentView;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordHelpers;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;
import edu.illinois.cs.cogcomp.edison.utilities.VerbClassDictionary;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Adds a collection of features that correspond to Levin's verb classes, as defined in
 * "English Verb Classes And Alternations: A Preliminary Investigation"
 * <p>
 * This class assumes the existence of a file called <i>verbClass.txt</i> in the class path. If the
 * input constituent has an attribute called {@link PredicateArgumentView#LemmaIdentifier}, it
 * treats the attribute as the lemma of the verb. This lemma is used to index Levin's list and the
 * corresponding verb classes are returned as discrete features. If the lemma is not present in the
 * list, then the feature set contains a single feature: "unknown". If the input constituent doesn't
 * have the required attribute, an empty feature set is returned.
 *
 * @author Vivek Srikumar
 */
public class LevinVerbClassFeature implements FeatureExtractor {

    private static final DiscreteFeature UNKNOWN = DiscreteFeature.create("*");
    public static LevinVerbClassFeature instance = new LevinVerbClassFeature();

    VerbClassDictionary dictionary = VerbClassDictionary.getDictionaryFromDatastore();

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        Set<Feature> features = new LinkedHashSet<>();

        String verb = getLemma(c);

        if (verb != null) {
            List<String> allVerbClasses = dictionary.getClass(verb);

            if (allVerbClasses.size() > 0) {
                for (String s : allVerbClasses) {
                    features.add(DiscreteFeature.create(s.trim()));
                }
            } else {
                features.add(UNKNOWN);
            }
        } else
            features.add(UNKNOWN);

        return features;
    }

    private String getLemma(Constituent c) {
        if (c.hasAttribute(PredicateArgumentView.LemmaIdentifier)) {
            return c.getAttribute(PredicateArgumentView.LemmaIdentifier);
        } else if (c.getTextAnnotation().hasView(ViewNames.LEMMA)) {
            return WordHelpers.getLemma(c.getTextAnnotation(), c.getEndSpan() - 1);
        } else
            return null;
    }

    @Override
    public String getName() {
        return "#levin-vb-class#";
    }

}
