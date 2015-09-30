package edu.illinois.cs.cogcomp.srl.jlis;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;

/**
 * Created by upadhya3 on 9/29/15.
 */
public class SRLFeatureExtractor extends AbstractFeatureGenerator {

    public SRLFeatureExtractor()
    {

    }
    @Override
    public IFeatureVector getFeatureVector(IInstance x, IStructure y) {
        SRLMulticlassInstance mi = (SRLMulticlassInstance) x;
        SRLMulticlassLabel my = (SRLMulticlassLabel) y;
        int label = my.getLabel();
        (mi.getCachedFeatureVector(type),label * manager.getModelInfo(type).getLexicon().size())
        return mi.getCachedFeatureVector();
    }
}
