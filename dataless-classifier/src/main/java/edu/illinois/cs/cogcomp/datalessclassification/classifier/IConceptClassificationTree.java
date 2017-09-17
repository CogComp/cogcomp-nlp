/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.datalessclassification.classifier;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.cs.cogcomp.datalessclassification.util.LabelScorePair;
import edu.illinois.cs.cogcomp.datalessclassification.util.LabelResultML;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVector;

/**
 * @author yqsong@illinois.edu
 * @author shashank
 */

public interface IConceptClassificationTree<T extends Serializable> {

	/**
	 * TODO: Why is {@link LabelResultML} not returned here?  
	 */
	public Map<Integer, List<LabelScorePair>> getFullPredictions (SparseVector<T> vector);
	
	public Map<Integer, Set<String>> getDepthPredictions (SparseVector<T> docVector, int topK);
	
	public Set<String> getFlatPredictions (SparseVector<T> docVector, int topK);
	
//	@Deprecated
//	/**
//	 * TODO: Why is {@link LabelResultML} not returned here?  
//	 */
//	/**
//	 * Ultimately this function needs to go -- Hierarchical Classification should be agnostic to similarity functions, embeddings and datasets
//	 */
//	public Map<Integer, List<LabelKeyValuePair>> labelDocumentW2V (String docContent);

//	/**
//	 * TODO: Why is {@link LabelResultML} not returned here?  
//	 */
//	public HashMap<Integer, List<LabelKeyValuePair>> labelDocumentDense (SparseSimilarityCondensation vectorCondensation,String docContent);
}