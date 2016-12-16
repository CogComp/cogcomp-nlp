package edu.illinois.cs.cogcomp.quant.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.factory.SpanFeaturesOrdered;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.LinkedHashSet;
import java.util.Set;

public class POSBigrams extends LBJavaFeatureExtractor {

    private static final long serialVersionUID = 1L;

    @Override
    public Set<Feature> getFeatures(Constituent instance) throws EdisonException {
        Set<Feature> features = new LinkedHashSet<Feature>();
        features.addAll(SpanFeaturesOrdered.POS_UNIGRAMS.getFeatures(instance));
        features.addAll(SpanFeaturesOrdered.POS_BIGRAMS.getFeatures(instance));
        return features;
    }
}
