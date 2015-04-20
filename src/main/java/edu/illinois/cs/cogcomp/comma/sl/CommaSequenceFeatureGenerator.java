package edu.illinois.cs.cogcomp.comma.sl;

import edu.illinois.cs.cogcomp.sl.applications.sequence.SequenceIOManager;
import edu.illinois.cs.cogcomp.sl.applications.sequence.SequenceInstance;
import edu.illinois.cs.cogcomp.sl.applications.sequence.SequenceLabel;
import edu.illinois.cs.cogcomp.sl.core.AbstractFeatureGenerator;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.FeatureVectorBuffer;
import edu.illinois.cs.cogcomp.sl.util.IFeatureVector;

public class CommaSequenceFeatureGenerator extends AbstractFeatureGenerator {
	/**
	 * This function returns a feature vector \Phi(x,y) based on an instance-structure pair.
	 * 
	 * @return Feature Vector \Phi(x,y), where x is the input instance and y is the
	 *         output structure
	 */

	@Override
	public IFeatureVector getFeatureVector(IInstance x, IStructure y) {
		FeatureVectorBuffer fv = new FeatureVectorBuffer();
		SequenceInstance ins = (SequenceInstance) x;
		int[] tags = ((SequenceLabel) y).tags;
		
		int len = ins.size();
		
		// add emission features.....
		int numOfEmissionFeatures = CommaIOManager.numFeatures;
		for (int i = 0; i < len; i++) {
			fv.addFeature(ins.baseFeatures[i], numOfEmissionFeatures * tags[i]);
		}

		// add prior features
		int numOfLabels = CommaIOManager.numLabels;
		int emissionOffset = numOfEmissionFeatures * numOfLabels;
		fv.addFeature(emissionOffset + tags[0], 1.0);

		// add transition features
		int priorEmissionOffset = emissionOffset + numOfLabels;
		// calculate transition features
		for (int i = 1; i < len; i++) {
			fv.addFeature(priorEmissionOffset + (tags[i - 1] * 
					numOfLabels + tags[i]), 1.0f);					
		}
		return fv.toFeatureVector(); 		
	}

}