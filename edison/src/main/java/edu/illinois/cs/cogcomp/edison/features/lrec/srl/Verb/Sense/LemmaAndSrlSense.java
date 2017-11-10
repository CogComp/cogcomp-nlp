/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Verb.Sense;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.*;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.features.lrec.srl.generic.SrlSenseFeatures;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * Extracts conjunction of lemma and {@link SrlSenseFeatures}.
 *
 *
 * @author Xinbo Wu
 */
public class LemmaAndSrlSense implements FeatureExtractor<Constituent> {
    private final FeatureCollection base = new FeatureCollection(this.getName());

    public LemmaAndSrlSense() throws Exception {
        this.base.addFeatureExtractor(FeatureUtilities.conjoin(WordFeatureExtractorFactory.lemma,
                new SrlSenseFeatures()));
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        return base.getFeatures(c);
    }

    @Override
    public String getName() {
        return "#VerbSense#";
    }
}
