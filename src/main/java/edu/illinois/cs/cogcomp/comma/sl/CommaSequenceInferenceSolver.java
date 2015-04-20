package edu.illinois.cs.cogcomp.comma.sl;

import edu.illinois.cs.cogcomp.sl.applications.sequence.SequenceInstance;
import edu.illinois.cs.cogcomp.sl.applications.sequence.SequenceLabel;
import edu.illinois.cs.cogcomp.sl.core.AbstractInferenceSolver;
import edu.illinois.cs.cogcomp.sl.core.IInstance;
import edu.illinois.cs.cogcomp.sl.core.IStructure;
import edu.illinois.cs.cogcomp.sl.util.WeightVector;

/**
 * A Viterbi inference solver
 * 
 * @author kchang10
 */
public class CommaSequenceInferenceSolver extends
		AbstractInferenceSolver {

	private static final long serialVersionUID = 1L;	

	@Override
	public Object clone(){
		return new CommaSequenceInferenceSolver();
	}
	
	@Override
	public IStructure getLossAugmentedBestStructure(
			WeightVector wv, IInstance input, IStructure gold)
			throws Exception {
		SequenceLabel goldLabeledSeq = (SequenceLabel) gold;
		
		// initialization
		SequenceInstance seq = (SequenceInstance) input;
		
		int numOflabels = CommaIOManager.numLabels;
		int numOfTokens = seq.baseFeatures.length;
		int numOfEmissionFeatures = CommaIOManager.numFeatures;
		
		float[][] dpTable = new float[2][numOflabels];
		int[][] path = new int[numOfTokens][numOflabels];
		
		int offset = (numOfEmissionFeatures+1) * numOflabels;
		
		// Viterbi algorithm
		for (int j = 0; j < numOflabels; j++) {
			float priorScore = wv.get(numOfEmissionFeatures * numOflabels + j);
			float zeroOrderScore = wv.dotProduct(seq.baseFeatures[0], j*numOfEmissionFeatures)+
					((gold !=null && j != goldLabeledSeq.tags[0])?1:0);
			dpTable[0][j] = priorScore + zeroOrderScore; 	 
			path[0][j] = -1;
		}
		
		for (int i = 1; i < numOfTokens; i++) {
			for (int j = 0; j < numOflabels; j++) {
				float zeroOrderScore =  wv.dotProduct(seq.baseFeatures[i], j*numOfEmissionFeatures)
						+ ((gold!=null && j != goldLabeledSeq.tags[i])?1:0);
				
				float bestScore = Float.NEGATIVE_INFINITY;
				for (int k = 0; k < numOflabels; k++) {
					float candidateScore = dpTable[(i-1)%2][k] +  wv.get(offset + (k * numOflabels + j));
					if (candidateScore > bestScore) {
						bestScore = candidateScore;
						path[i][j] = k;
					}
				}
				dpTable[i%2][j] = zeroOrderScore + bestScore;
			}
		}
		
		// find the best sequence		
		int[] tags = new int[numOfTokens];
		
		int maxTag = 0;
		for (int i = 0; i < numOflabels; i++)
			if (dpTable[(numOfTokens - 1)%2][i] > dpTable[(numOfTokens - 1)%2][maxTag]) 
				maxTag = i;
		
		tags[numOfTokens - 1] = maxTag;
		
		for (int i = numOfTokens - 1; i >= 1; i--) 
			tags[i-1] = path[i][tags[i]];
		return new SequenceLabel(tags);
	}
		
	@Override
	public IStructure getBestStructure(WeightVector wv,
			IInstance input) throws Exception {
		return getLossAugmentedBestStructure(wv, input, null);
	}
	@Override
	public float getLoss(IInstance ins, IStructure goldStructure,  IStructure structure){
		SequenceLabel goldLabeledSeq = (SequenceLabel) goldStructure;
		float loss = 0;
		for (int i = 0; i < goldLabeledSeq.tags.length; i++)
			if (((SequenceLabel) structure).tags[i] != goldLabeledSeq.tags[i])
				loss += 1.0f;
		return loss;
	}

}
