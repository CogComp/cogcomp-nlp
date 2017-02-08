/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.cs.cogcomp.edison.features.lrec.srl.generic;

import edu.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.cs.cogcomp.edison.features.ContextFeatureExtractor;
import edu.cs.cogcomp.edison.features.Feature;
import edu.cs.cogcomp.edison.features.FeatureCollection;
import edu.cs.cogcomp.edison.features.FeatureExtractor;
import edu.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * Extracts words in a context window of size 2 using {@link WordFeatureExtractorFactory}.word
 *
 * @keywords SRL, generic, word, context
 * @author Xinbo Wu
 */
public class WordContextWindowTwo implements FeatureExtractor {
    private final FeatureCollection base;
    private final String name;

    public WordContextWindowTwo() {
        this("#wordContext#");
    }

    public WordContextWindowTwo(String name) {
        // ContextFeatureExtractor context = new ContextFeatureExtractor(2, true, true);
        this.name = name;
        this.base = new FeatureCollection(this.getName());

        ContextFeatureExtractor context = new ContextFeatureExtractor(2, true, true);
        context.addFeatureExtractor(WordFeatureExtractorFactory.word);
        base.addFeatureExtractor(context);
    }

    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        return base.getFeatures(c);
    }

    @Override
    public String getName() {
        return this.name;
    }
}
