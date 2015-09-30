package edu.illinois.cs.cogcomp.srl.jlis;

import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;
import edu.illinois.cs.cogcomp.srl.core.Models;
import edu.illinois.cs.cogcomp.srl.core.SRLManager;

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
        SRLManager manager = my.getManager();
        Models type = my.getType();
        IFeatureVector fv = mi.getCachedFeatureVector(type);
        FeatureVectorBuffer fvb = new FeatureVectorBuffer(fv);
        fvb.shift(label * manager.getModelInfo(type).getLexicon().size());
        IFeatureVector ans = fvb.toFeatureVector();
        return ans;
    }
}
