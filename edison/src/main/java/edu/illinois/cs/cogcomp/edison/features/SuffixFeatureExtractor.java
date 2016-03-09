package edu.illinois.cs.cogcomp.edison.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Vivek Srikumar
 */
public class SuffixFeatureExtractor extends WordFeatureExtractor {

    private final Feature featureName;
    protected List<String> suffixes;

    public SuffixFeatureExtractor(List<String> deAdjSuffixes, String featureName,
            boolean useLastWordOfMultiwordConstituents) {
        super(useLastWordOfMultiwordConstituents);
        this.suffixes = deAdjSuffixes;
        this.featureName = DiscreteFeature.create(featureName);
    }

    @Override
    public Set<Feature> getWordFeatures(TextAnnotation ta, int wordPosition) throws EdisonException {

        String word = ta.getToken(wordPosition).toLowerCase();

        Set<Feature> feats = new LinkedHashSet<>();

        boolean found = false;
        for (String s : suffixes) {
            if (word.endsWith(s)) {
                feats.add(DiscreteFeature.create(featureName.getName() + ":" + s));
                found = true;
            }
        }

        if (found) {
            feats.add(featureName);
        }

        return feats;
    }

    @Override
    public String getName() {
        return "#suffix#" + featureName;
    }
}
