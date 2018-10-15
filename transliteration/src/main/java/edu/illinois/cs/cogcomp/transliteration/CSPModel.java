/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.org/
 */
package edu.illinois.cs.cogcomp.transliteration;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.Triple;
import edu.illinois.cs.cogcomp.utils.SparseDoubleVector;

import java.util.List;

/**
 * Created by stephen on 9/25/15.
 */
// also extends iCloneable. Hmm.
class CSPModel extends TransliterationModel
{

    enum SegMode
    {
        None,
        Count,
        Entropy,
        Best
    }

    enum SmoothMode
    {
        BySource, //smooth based on length of the source substring only
        ByMax, //smooth based on the maximum of lengths of source and target substrings
        BySum //smooth based on the sum of lengths of the source and target substrings
    }

    enum EMMode
    {
        Normal,
        MaxSourceSeg, //assume every source segment is valid (ex.: not true for "p" or "h" in "phone") and, in each example, find the "true" generated target language substring by giving a weight of 1 to the most likely production, and 0 to everything else
        Smoothed, //apply smoothing in EM
        BySourceSeg
    }

    public CSPModel() {
    }

    /**
     *
     * @param maxSubstringLength
     * @param segContextSize
     * @param productionContextSize
     * @param minProductionProbability
     * @param segMode
     * @param syllabic
     * @param smoothMode
     * @param fallbackStrategy
     * @param emMode
     * @param underflowChecking True to check for \sum_t P(t|s) == 0 after normalizing production counts, where t and s are segments of the target and source word, respectively.  If such (total) underflow occurs, the previous iteration's conditional probabilities are used instead.
     */
    public CSPModel(int maxSubstringLength, int segContextSize, int productionContextSize, double minProductionProbability, SegMode segMode, boolean syllabic, SmoothMode smoothMode, FallbackStrategy fallbackStrategy, EMMode emMode, boolean underflowChecking)
    {
        this.maxSubstringLength = maxSubstringLength;
        this.segContextSize = segContextSize;
        this.productionContextSize = productionContextSize;
        this.minProductionProbability = minProductionProbability;
        this.fallbackStrategy = fallbackStrategy;
        this.syllabic = syllabic;
        //this.updateSegProbs = updateSegProbs;
        this.segMode = segMode;
        this.smoothMode = smoothMode;
        this.emMode = emMode;
        this.underflowChecking = underflowChecking;
    }

    public Boolean underflowChecking;

    public EMMode emMode;
    public SegMode segMode;
    public SmoothMode smoothMode;

    //public bool updateSegProbs;
    public Boolean syllabic;

    public SparseDoubleVector<Pair<Triple<String, String, String>, String>> productionProbs;
    public SparseDoubleVector<Triple<String, String,String>> segProbs;

    //public SparseDoubleVector<Pair<Triple<String, String, String>, String>> productionCounts;
    //public SparseDoubleVector<Triple<String, String, String>> segCounts;

    public int segContextSize;
    public int productionContextSize;
    public int maxSubstringLength;

    public double minProductionProbability;

    public FallbackStrategy fallbackStrategy;

    public Object clone()
    {
        return this.clone();
    }


    @Override
    public double GetProbability(String word1, String word2)
    {
        return CSPTransliteration.GetProbability(word1, word2, this);
    }

    @Override
    public TransliterationModel LearnModel(List<Triple<String, String, Double>> examples) {
        return CSPTransliteration.LearnModel(examples, this);
    }


}
