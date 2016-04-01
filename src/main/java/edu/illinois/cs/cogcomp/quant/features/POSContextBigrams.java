package edu.illinois.cs.cogcomp.quant.features;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.ContextFeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.factory.WordFeatureExtractorFactory;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.util.Set;

public class POSContextBigrams extends LBJavaFeatureExtractor {

	private static final long serialVersionUID = 1L;

	@Override
    public Set<Feature> getFeatures(Constituent instance) throws EdisonException {
        ContextFeatureExtractor f = new ContextFeatureExtractor(2, true, true);
        f.addFeatureExtractor(WordFeatureExtractorFactory.pos);
        return f.getFeatures(instance);
    }
}
