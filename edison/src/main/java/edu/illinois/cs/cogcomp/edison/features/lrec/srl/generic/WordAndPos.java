/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.generic;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;


/**
 * Extracts a conjunction of Word and Part of Speech.
 *
 * @keywords word, pos, part of speech, conjunction
 * @author Xinbo Wu
 */
public class WordAndPos implements FeatureExtractor {
    private final String name;
    private final FeatureCollection base;

    public WordAndPos() {
        this("#wordPos#");
    }

    public WordAndPos(String name) {
        this.name = name;
        this.base = new FeatureCollection(this.getName());

        this.base.addFeatureExtractor(WordFeatureExtractorFactory.word);
        this.base.addFeatureExtractor(WordFeatureExtractorFactory.pos);
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
