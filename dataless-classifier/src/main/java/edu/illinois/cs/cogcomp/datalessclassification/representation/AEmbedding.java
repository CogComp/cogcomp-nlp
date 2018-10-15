/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.datalessclassification.representation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.illinois.cs.cogcomp.datalessclassification.util.QueryPreProcessor;
import edu.illinois.cs.cogcomp.datalessclassification.util.SparseVector;

/**
 * Abstract class for all Embeddings
 *
 * @author yqsong@illinois.edu
 * @author shashank
 */

public abstract class AEmbedding<T extends Serializable> {

    /**
     * Override this function to get your desired query segmentation (simpleSegmentation being one
     * example)
     */
    public List<String> getTerms(String query) {
        return simpleSegmentation(query);
    }

    /**
     * A simple white-space based tokenizer
     */
    private List<String> simpleSegmentation(String query) {
        query = QueryPreProcessor.process(query);
        String[] terms = query.split("\\s+");

        List<String> termList = Arrays.asList(terms);

        return termList;
    }

    /**
     * Override this function to get your desired term processing module (simpleProcessTerm being
     * one example)
     */
    public String processTerm(String s) {
        return simpleProcessTerm(s);
    }

    final private String simpleProcessTerm(String s) {
        return s.toLowerCase().trim();
    }

    /**
     * Implement this function for returning the default vector in case no word in the query could
     * be found in the index
     */
    abstract public SparseVector<T> getDefaultConceptVectorMap();

    /**
     * Intended to output the vector for the most basic unit; Should internally process the term
     * using the processTerm function
     */
    abstract public SparseVector<T> getTermConceptVectorMap(String term);

    /**
     * Intended to output the vector for any arbitrary query; Should internally segment/tokenize the query,
     * process it, and then return a composed vector.
     *
     */
    abstract public SparseVector<T> getVector(String query);

    /**
     * Override this function to select given number of dimensions
     */
    public SparseVector<T> getVector(String query, int numConcepts) {
        return getVectorIgnoreSize(query);
    }

    private SparseVector<T> getVectorIgnoreSize(String query) {
        return getVector(query);
    }

    /**
     * This function converts the given query into a list of terms using the getTerms() function,
     * and returns a vector which is an average of the vectors of the individual terms.
     */
    public SparseVector<T> getConceptVectorBasedOnSegmentation(String query) {
        return getConceptVectorBasedOnSegmentation(query, false);
    }

    /**
     * This function overloads getConceptVectorBasedOnSegmentation to provide support for
     * switching on/off weighing the individual term vectors with their frequencies in the query.
     *
     * Setting ignoreTermFreq = True will return a simple averaging over all the terms in the query
     */
    public SparseVector<T> getConceptVectorBasedOnSegmentation(String query, boolean ignoreTermFreq) {
        Map<String, Double> termWeights = new HashMap<>();
        List<String> terms = getTerms(query);

        for (String term : terms) {
            if (!termWeights.containsKey(term)) {
                termWeights.put(term, 1.0);
            } else {
                if (!ignoreTermFreq)
                    termWeights.put(term, termWeights.get(term) + 1.0);
            }
        }

        return getConceptVectorBasedOnTermWeights(termWeights);
    }

    /**
     * This function takes a "Term - Count" map as input and outputs a vector which is the
     * weighted average of the vectors of individual terms.
     */
    public SparseVector<T> getConceptVectorBasedOnTermWeights(Map<String, Double> termWeights) {
        if (termWeights.size() == 0)
            return new SparseVector<>();

        Map<T, Double> finalMap = new HashMap<>();

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

                if (!finalMap.containsKey(index)) {
                    finalMap.put(index, score);
                } else {
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

        SparseVector<T> sparseVector = new SparseVector<>(finalMap);

        return sparseVector;
    }
}
