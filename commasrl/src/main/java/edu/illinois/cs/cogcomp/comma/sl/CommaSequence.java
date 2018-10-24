/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.comma.sl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.illinois.cs.cogcomp.comma.datastructures.Comma;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class CommaSequence implements IInstance {
    public final List<Comma> sortedCommas;
    public final IFeatureVector baseFeatures[];

    public CommaSequence(List<Comma> commas, Lexiconer lexicon, List<Classifier> lbjExtractors) {
        Collections.sort(commas, new Comparator<Comma>() {
            @Override
            public int compare(Comma o1, Comma o2) {
                return o1.getPosition() - o2.getPosition();
            }
        });
        this.sortedCommas = commas;

        baseFeatures = new IFeatureVector[sortedCommas.size()];
        for (int i = 0; i < sortedCommas.size(); i++) {
            FeatureVector lbjFeatureVector = new FeatureVector();
            for (Classifier lbjExtractor : lbjExtractors)
                lbjFeatureVector.addFeatures(lbjExtractor.classify(sortedCommas.get(i)));
            FeatureVectorBuffer slFeatureVectorBuffer = new FeatureVectorBuffer();
            for (int j = 0; j < lbjFeatureVector.featuresSize(); j++) {
                String featureString = lbjFeatureVector.getFeature(j).toString();
                if (lexicon.isAllowNewFeatures())
                    lexicon.addFeature(featureString);
                if (lexicon.containFeature(featureString))
                    slFeatureVectorBuffer.addFeature(lexicon.getFeatureId(featureString), 1);
                else
                    slFeatureVectorBuffer.addFeature(
                            lexicon.getFeatureId(CommaIOManager.unknownFeature), 1);
            }
            baseFeatures[i] = slFeatureVectorBuffer.toFeatureVector();
        }
    }
}
