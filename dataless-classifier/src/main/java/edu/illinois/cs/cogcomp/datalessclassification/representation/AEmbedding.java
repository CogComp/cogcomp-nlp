package edu.illinois.cs.cogcomp.datalessclassification.representation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.datalessclassification.util.DenseVector;
import edu.illinois.cs.cogcomp.datalessclassification.util.DenseVectorOperations;
import edu.illinois.cs.cogcomp.datalessclassification.util.QueryPreProcessor;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVector;

/**
 * @author yqsong@illinois.edu
 * @author shashank
 */

public abstract class AEmbedding<T extends Serializable> {
	
	/**
	 * Override this function to get your desired query segmentation (simpleSegmentation being one example)
	 */
	public List<String> getTerms (String query) {
		return simpleSegmentation(query);
	}
	
	final private List<String> simpleSegmentation (String query) {
		query = QueryPreProcessor.process(query);
		String[] terms = query.split("\\s+");
		
		List<String> termList = Arrays.asList(terms);
		
		return termList;
	}
	
	/**
	 * Override this function to get your desired term processing module (simpleProcessTerm being one example)
	 */
	public String processTerm (String s) {
		return simpleProcessTerm(s);
	}
	
	final private String simpleProcessTerm (String s) {
		return s.toLowerCase().trim();
	}

	/**
	 * Implement this function for returning the default vector in case no word in the query could be found in the index
	 * @throws IOException 
	 */
	abstract public SparseVector<T> getDefaultConceptVectorMap ();
	
	/**
	 * Intended to output the vector for the most basic unit
	 * Should internally process the term using the processTerm function
	 * @throws IOException 
	 */
	abstract public SparseVector<T> getTermConceptVectorMap (String term);
	
	/**
	 * Intended to output the vector for any arbitrary query
	 * @throws IOException 
	 */
	abstract public SparseVector<T> getVector (String query);
	
	/**
	 * Override this function to select given number of dimensions
	 */
	public SparseVector<T> getVector (String query, int numConcepts) {
		return getVectorIgnoreSize(query);
	}
	
	private final SparseVector<T> getVectorIgnoreSize (String query) {
		return getVector(query);
	}
	
	public SparseVector<T> getConceptVectorBasedonSegmentation (String query) {
		return getConceptVectorBasedonSegmentation(query, false);
	}
	
	/**
	 * Switching off ignoreTermFreq will return a simple averaging over all the terms found in the index
	 * @throws IOException 
	 */
	public SparseVector<T> getConceptVectorBasedonSegmentation (String query, boolean ignoreTermFreq) {
		Map<String, Double> termWeights = new HashMap<>();
		List<String> terms = getTerms(query);
		
		for (String term : terms) {
			if (termWeights.containsKey(term) == false) {
				termWeights.put(term, 1.0);
			} 
			else {
				if (ignoreTermFreq == false)
					termWeights.put(term, termWeights.get(term) + 1.0);
				else
					continue;
			}
		}
		
		return getConceptVectorBasedonTermWeights(termWeights);
	}
	
	public SparseVector<T> getConceptVectorBasedonTermWeights (Map<String, Double> termWeights) {
		if (termWeights.size() == 0)
			return new SparseVector<>();
		
		Map<T, Double> finalMap = new HashMap<T, Double>();
		
    	try {
    		double sumWeight = 0;
    		
	        for (String term : termWeights.keySet()) {
	        	SparseVector<T> vec = getTermConceptVectorMap(term);
	        	
	        	if (vec == null)
	        		continue;
	        	
	        	if (vec.size() == 0)
	        		continue;
    			
    			Map<T, Double> map = vec.getKeyValueMap();
    			
    			for (T index : map.keySet()) {
    				double score = map.get(index);
    				
    				score *= termWeights.get(term);
    				
    				if (finalMap.containsKey(index) == false) {
    					finalMap.put(index, score);
					} 
    				else {
    					finalMap.put(index, finalMap.get(index) + score);
					}
    			}
    			
    			sumWeight += termWeights.get(term);
		    }
	        
	        if (finalMap.isEmpty())
	        	return new SparseVector<>();
	        
	        for (T k : finalMap.keySet()) {
	        	finalMap.put(k, finalMap.get(k) / sumWeight);
	        }

    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	SparseVector<T> sparseVector = new SparseVector<>(finalMap);
		
		return sparseVector;
	}
	
	public SparseVector<Integer> getMapFromDenseVector (DenseVector denseVector) {
		return DenseVectorOperations.getSparseVector(denseVector);
	}
}