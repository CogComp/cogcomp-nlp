/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.comma.sl;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.sl.util.Lexiconer;

public class CommaSequenceFeatureGenerator extends AbstractFeatureGenerator {
    private static final long serialVersionUID = 1L;
    public final Lexiconer lexicon;

    public CommaSequenceFeatureGenerator(Lexiconer lexicon) {
        this.lexicon = lexicon;
    }

    /**
     * This function returns a feature vector \Phi(x,y) based on an instance-structure pair.
     * 
     * @return Feature Vector \Phi(x,y), where x is the input instance and y is the output structure
     */

    @Override
    public IFeatureVector getFeatureVector(IInstance x, IStructure y) {
        // lexicon should have been completely built while reading the problem instances itself
        assert !lexicon.isAllowNewFeatures();

        CommaSequence commaSequence = (CommaSequence) x;
        CommaLabelSequence commaLabelSequence = (CommaLabelSequence) y;

        FeatureVectorBuffer fv = new FeatureVectorBuffer();
        int len = commaSequence.sortedCommas.size();
        /*
         * for(Comma comma : commaSequence.sortedCommas){ FeatureVector lbjFeatureVector =
         * lbjExtractor.classify(comma); for(int i=0; i<lbjFeatureVector.featuresSize(); i++){
         * String emittedFeatureString = lbjFeatureVector.getFeature(i).toString();
         * lexicon.addFeature(emittedFeatureString);
         * fv.addFeature(lexicon.getFeatureId(emittedFeatureString), 1); } }
         * 
         * String startLabel = commaLabelSequence.commaLabels.get(0);
         * lexicon.addFeature(startLabel); fv.addFeature(lexicon.getFeatureId(startLabel), 1);
         * 
         * for(int i=1; i<commaLabelSequence.commaLabels.size(); i++){ String previousLabel =
         * commaLabelSequence.commaLabels.get(i-1); String currentLabel =
         * commaLabelSequence.commaLabels.get(i); String transitionFeatureString = previousLabel +
         * "---" + currentLabel; lexicon.addFeature(transitionFeatureString);
         * fv.addFeature(lexicon.getFeatureId(transitionFeatureString), 1); }
         */


        int[] tags = commaLabelSequence.labelIds;
        IFeatureVector[] baseFeatures = commaSequence.baseFeatures;
        int numOfEmissionFeatures = lexicon.getNumOfFeature();
        int numOfLabels = lexicon.getNumOfLabels();

        // add emission features.....
        for (int i = 0; i < len; i++) {
            fv.addFeature(baseFeatures[i], numOfEmissionFeatures * tags[i]);
        }

        // add prior feature
        int emissionOffset = numOfEmissionFeatures * numOfLabels;
        fv.addFeature(emissionOffset + tags[0], 1.0f);

        // add transition features
        int priorEmissionOffset = emissionOffset + numOfLabels;
        // calculate transition features
        for (int i = 1; i < len; i++) {
            fv.addFeature(priorEmissionOffset + (tags[i - 1] * // TODO can't allow label-id of 0
                                                               // because if either label is 0, the
                                                               // product will be 0
                    numOfLabels + tags[i]), 1.0f);
        }

        return fv.toFeatureVector();
    }

}
