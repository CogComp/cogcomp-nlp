package edu.illinois.cs.cogcomp.edison.features.lrec.ner;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.edison.features.DiscreteFeature;
import edu.illinois.cs.cogcomp.edison.features.Feature;
import edu.illinois.cs.cogcomp.edison.features.FeatureExtractor;
import edu.illinois.cs.cogcomp.edison.features.helpers.FeatureCreatorUtil;
import edu.illinois.cs.cogcomp.edison.features.helpers.WordEmbeddings;
import edu.illinois.cs.cogcomp.edison.utilities.EdisonException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by mssammon on 5/10/16.
 */
public class WordEmbeddingWindow implements FeatureExtractor {

    private final int windowStart;
    private final int windowEnd;
    private final boolean ignoreSentenceBoundaries;


    public WordEmbeddingWindow(int windowSize, boolean ignoreSentenceBoundaries ) throws IOException {
        this.windowStart = 0 - windowSize;
        this.windowEnd = windowSize;
        this.ignoreSentenceBoundaries = ignoreSentenceBoundaries;

        WordEmbeddings.initWithDefaults();
    }



    @Override
    public Set<Feature> getFeatures(Constituent c) throws EdisonException {
        Set<Feature> features = new HashSet<>();

        // get allowable window given position in text

        IntPair relativeWindow = FeatureCreatorUtil.getWindowSpan( c, windowStart, windowEnd, ignoreSentenceBoundaries );


        for ( int i = relativeWindow.getFirst(); i <= relativeWindow.getSecond(); ++i )
        {
            double[] embedding = WordEmbeddings.getEmbedding(c);
            if (embedding != null)
            {
                for (int dim = 0; dim < embedding.length; dim++)
                {
                    final String[] pieces = { getName(), ":", "place", Integer.toString(i), "dim",
                            Integer.toString(dim), ":", Double.toString(embedding[dim]) };
                    features.add(FeatureCreatorUtil.createFeatureFromArray(pieces));
                }
            }
            i++;
        }

        return features;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }
}
