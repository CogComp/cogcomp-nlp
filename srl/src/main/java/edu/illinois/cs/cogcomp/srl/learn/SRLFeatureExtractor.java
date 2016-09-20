/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.srl.learn;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;
import edu.illinois.cs.cogcomp.srl.experiment.PreExtractor;

/**
 * A wrapper for the feature extractor required by illinois-sl. The real feature extraction happens in
 * {@link PreExtractor#consumeInstance(SRLMulticlassInstance, SRLMulticlassLabel)} during training
 * and {@link SRLPredicateInstance#cacheAllFeatureVectors(boolean)} during testing.
 *
 * @author upadhya3
 */
public class SRLFeatureExtractor extends AbstractFeatureGenerator {

    public SRLFeatureExtractor() {}

    @Override
    public IFeatureVector getFeatureVector(IInstance x, IStructure y) {
        SRLMulticlassInstance mi = (SRLMulticlassInstance) x;
        SRLMulticlassLabel my = (SRLMulticlassLabel) y;
        int label = my.getLabel();
        SRLManager manager = my.getManager();
        Models type = my.getType();
        IFeatureVector fv = mi.getCachedFeatureVector(type);
        FeatureVectorBuffer fvb = new FeatureVectorBuffer(fv);
        fvb.shift(label * manager.getModelInfo(type).getLexicon().size());
        return fvb.toFeatureVector();
    }
}
