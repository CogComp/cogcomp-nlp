/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.edison.features.lrec.srl.Nom.Identifier;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureCollection;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.ParseHeadWordFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.factory.ListFeatureFactory;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.ArrayList;
import java.util.Set;


/**
 * Extracts lexical and parse structure features for identifying Nom SRL arguments. Combines
 * {@link WordFeatureExtractorFactory} word, pos, numberNormalizer, gerundMarker,
 * nominalizationMarker, and dateMarker; {@link ListFeatureFactory} daysOfTheWeek and months; and
 * {@link ParseHeadWordFeatureExtractor}.
 *
 * @keywords SRL, Nom, nominalization, nominal, identifier, argument
 * @author Xinbo Wu
 */
public class SrlNomHeadWordFeatures implements FeatureExtractor {
    private final String name;
    private final FeatureCollection base;

    public SrlNomHeadWordFeatures() {
        this("#HeadWordFeatures#");
    }

    public SrlNomHeadWordFeatures(String name) {
        this.name = name;
        this.base = new FeatureCollection(this.getName());

        ArrayList<FeatureCollection> tmp = new ArrayList<FeatureCollection>();

        tmp.add(new FeatureCollection(""));
        tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.word);
        tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.pos);
        tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.numberNormalizer);
        tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.gerundMarker);
        tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.nominalizationMarker);
        tmp.get(0).addFeatureExtractor(ListFeatureFactory.daysOfTheWeek);
        tmp.get(0).addFeatureExtractor(ListFeatureFactory.months);
        tmp.get(0).addFeatureExtractor(WordFeatureExtractorFactory.dateMarker);

        this.base.addFeatureExtractor(new ParseHeadWordFeatureExtractor(ViewNames.PARSE_STANFORD,
                tmp.get(0)));
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
