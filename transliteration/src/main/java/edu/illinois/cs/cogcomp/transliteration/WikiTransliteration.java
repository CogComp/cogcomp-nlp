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
import edu.illinois.cs.cogcomp.utils.Dictionaries;
import edu.illinois.cs.cogcomp.utils.InternDictionary;
import edu.illinois.cs.cogcomp.utils.SparseDoubleVector;
import edu.illinois.cs.cogcomp.utils.TopList;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

class WikiTransliteration {

    public class ContextModel {
        public SparseDoubleVector<Pair<Triple<String, String, String>, String>> productionProbs;
        public SparseDoubleVector<Pair<String, String>> segProbs;
        public int segContextSize;
        public int productionContextSize;
        public int maxSubstringLength;
    }

    public enum NormalizationMode {
        None,
        AllProductions,
        BySourceSubstring,
        BySourceSubstringMax,
        BySourceAndTargetSubstring,
        BySourceOverlap,
        ByTargetSubstring
    }

    // FIXME: can remove this?
    public enum AliasType {
        Unknown,
        Link,
        Redirect,
        Title,
        Disambig,
        Interlanguage
    }

    private static HashMap<String, Boolean> languageCodeTable;

    /**
     * For querying wikipedia?
     */
    public static final String[] languageCodes = new String[]{
            "aa", "ab", "ae", "af", "ak", "am", "an", "ar", "as", "av", "ay", "az", "ba", "be", "bg", "bh", "bi", "bm", "bn", "bo", "br", "bs", "ca", "ce", "ch", "co", "cr", "cs", "cu", "cv", "cy", "da", "de", "dv", "dz", "ee", "el", "en", "eo", "es", "et", "eu", "fa", "ff", "fi", "fj", "fo", "fr", "fy", "ga", "gd", "gl", "gn", "gu", "gv", "ha", "he", "hi", "ho", "hr", "ht", "hu", "hy", "hz", "ia", "id", "ie", "ig", "ii", "ik", "io", "is", "it", "iu", "ja", "jv", "ka", "kg", "ki", "kj", "kk", "kl", "km", "kn", "ko", "kr", "ks", "ku", "kv", "kw", "ky", "la", "lb", "lg", "li", "ln", "lo", "lt", "lu", "lv", "mg", "mh", "mi", "mk", "ml", "mn", "mr", "ms", "mt", "my", "na", "nb", "nd", "ne", "ng", "nl", "nn", "no", "nr", "nv", "ny", "oc", "oj", "om", "or", "os", "pa", "pi", "pl", "ps", "pt", "qu", "rm", "rn", "ro", "ru", "rw", "sa", "sc", "sd", "se", "sg", "sh", "si", "sk", "sl", "sm", "sn", "so", "sq", "sr", "ss", "st", "su", "sv", "sw", "ta", "te", "tg", "th", "ti", "tk", "tl", "tn", "to", "tr", "ts", "tt", "tw", "ty", "ug", "uk", "ur", "uz", "ve", "vi", "vo", "wa", "wo", "xh", "yi", "yo", "za", "zh", "zu"
    };

    public static Pair<Double, List<Production>> GetAlignmentProbabilityDebug(String word1, String word2, int maxSubstringLength, HashMap<Production, Double> probs, double minProb) {
        Pair<Double, List<Production>> result = GetAlignmentProbabilityDebug(word1, word2, maxSubstringLength, probs, minProb, new HashMap<Production, Pair<Double, List<Production>>>());
        return result;
    }

    public static Pair<Double, List<Production>> GetAlignmentProbabilityDebug(String word1, String word2, int maxSubstringLength, HashMap<Production, Double> probs) {
        return GetAlignmentProbabilityDebug(word1, word2, maxSubstringLength, probs, 0, new HashMap<Production, Pair<Double, List<Production>>>());
    }

    /**
     * This used to have productions as an output variable. I (SWM) added it as the second element of return pair.
     * @param word1
     * @param word2
     * @param maxSubstringLength
     * @param probs
     * @param floorProb
     * @param memoizationTable
     * @return
     */
    public static Pair<Double, List<Production>> GetAlignmentProbabilityDebug(String word1, String word2, int maxSubstringLength, HashMap<Production, Double> probs, double floorProb, HashMap<Production, Pair<Double, List<Production>>> memoizationTable) {
        List<Production> productions = new ArrayList<>();
        Production bestPair = new Production(null, null);

        if (word1.length() == 0 && word2.length() == 0) return new Pair<>(1.0, productions);
        if (word1.length() * maxSubstringLength < word2.length()) return new Pair<>(0.0, productions); //no alignment possible
        if (word2.length() * maxSubstringLength < word1.length()) return new Pair<>(0.0, productions);

        Pair<Double, List<Production>> cached;
        if (memoizationTable.containsKey(new Production(word1, word2))) {
            cached = memoizationTable.get(new Production(word1, word2));
            productions = cached.getSecond();
            return new Pair<>(cached.getFirst(), productions);
        }

        double maxProb = 0;

        int maxSubstringLength1 = Math.min(word1.length(), maxSubstringLength);
        int maxSubstringLength2 = Math.min(word2.length(), maxSubstringLength);

        for (int i = 1; i <= maxSubstringLength1; i++) {
            String substring1 = word1.substring(0, i);
            for (int j = 0; j <= maxSubstringLength2; j++) {
                double localProb;
                if (probs.containsKey(new Production(substring1, word2.substring(0, j)))) {
                    localProb = probs.get(new Production(substring1, word2.substring(0, j)));
                    //double localProb = ((double)count) / totals[substring1];
                    if (localProb < maxProb || localProb < floorProb)
                        continue; //this is a really bad transition--discard

                    List<Production> outProductions;
                    Pair<Double,List<Production>> ret = GetAlignmentProbabilityDebug(word1.substring(i), word2.substring(j), maxSubstringLength, probs, maxProb / localProb, memoizationTable);
                    outProductions = ret.getSecond();

                    localProb *= ret.getFirst();
                    if (localProb > maxProb) {
                        productions = outProductions;
                        maxProb = localProb;
                        bestPair = new Production(substring1, word2.substring(0, j));
                    }
                }
            }
        }

        productions = new ArrayList<>(productions); //clone it before modifying
        productions.add(0, bestPair);

        memoizationTable.put(new Production(word1, word2), new Pair<>(maxProb, productions));

        return new Pair<>(maxProb, productions);
    }

    /**
     * This is the same as probs, only in a more convenient format. Does not include weights on productions.
     * @param probs production probabilities
     * @return hashmap mapping from Production[0] => Production[1]
     */
    public static HashMap<String, HashSet<String>> GetProbMap(HashMap<Production, Double> probs) {
        HashMap<String, HashSet<String>> result = new HashMap<>();
        for (Production pair : probs.keySet()) {
            if(!result.containsKey(pair.getFirst())){
                result.put(pair.getFirst(), new HashSet<String>());
            }
            HashSet<String> set = result.get(pair.getFirst());
            set.add(pair.getSecond());

            result.put(pair.getFirst(), set);
        }

        return result;
    }


    /**
     * Given a word, ngramProbs, and an ngramSize, this predicts the probability of the word with respect to this language model.
     *
     * Compare this against:
     *
     * @param word
     * @param ngramProbs
     * @param ngramSize
     * @return
     */
    public static double GetLanguageProbability(String word, HashMap<String, Double> ngramProbs, int ngramSize) {
        double probability = 1;
        String paddedExample = StringUtils.repeat('_', ngramSize - 1) + word + StringUtils.repeat('_', ngramSize - 1);
        for (int i = ngramSize - 1; i < paddedExample.length(); i++) {
            int n = ngramSize;
            //while (!ngramProbs.TryGetValue(paddedExample.substring(i - n + 1, n), out localProb)) {

            // This is a backoff procedure.
            String ss = paddedExample.substring(i-n+1, i+1);
            Double localProb = ngramProbs.get(ss);
            while(localProb == null){
                n--;
                if (n == 1)
                    return 0; // final backoff probability. Be careful with this... can result in names with 0 probability if the LM isn't large enough.
                ss = paddedExample.substring(i-n+1, i+1);
                localProb = ngramProbs.get(ss);
            }
            probability *= localProb;
        }

        return probability;
    }


    /**
     * This is used in the generation process.
     * @param topK number of candidates to return
     * @param word1
     * @param maxSubstringLength
     * @param probMap a hashmap for productions, same as probs, but with no weights
     * @param probs
     * @param memoizationTable
     * @param pruneToSize
     * @return
     */
    public static TopList<Double, String> Predict2(int topK, String word1, int maxSubstringLength, Map<String, HashSet<String>> probMap, HashMap<Production, Double> probs, HashMap<String, HashMap<String, Double>> memoizationTable, int pruneToSize) {
        TopList<Double, String> result = new TopList<>(topK);
        // calls a helper function
        HashMap<String, Double> rProbs = Predict2(word1, maxSubstringLength, probMap, probs, memoizationTable, pruneToSize);
        double probSum = 0;

        // gathers the total probability for normalization.
        for (double prob : rProbs.values())
            probSum += prob;

        // this normalizes each value by the total prob.
        for (String key : rProbs.keySet()) {
            Double value = rProbs.get(key);
            result.add(new Pair<>(value / probSum, key));
        }

        return result;
    }

    /**
     * Helper function.
     * @param word1
     * @param maxSubstringLength
     * @param probMap
     * @param probs
     * @param memoizationTable
     * @param pruneToSize
     * @return
     */
    public static HashMap<String, Double> Predict2(String word1, int maxSubstringLength, Map<String, HashSet<String>> probMap, HashMap<Production, Double> probs, HashMap<String, HashMap<String, Double>> memoizationTable, int pruneToSize) {
        HashMap<String, Double> result;
        if (word1.length() == 0) {
            result = new HashMap<>(1);
            result.put("", 1.0);
            return result;
        }

        if (memoizationTable.containsKey(word1)) {
            return memoizationTable.get(word1);
        }

        result = new HashMap<>();

        int maxSubstringLength1 = Math.min(word1.length(), maxSubstringLength);

        for (int i = 1; i <= maxSubstringLength1; i++) {
            String substring1 = word1.substring(0, i);

            if (probMap.containsKey(substring1)) {

                // recursion right here.
                HashMap<String, Double> appends = Predict2(word1.substring(i), maxSubstringLength, probMap, probs, memoizationTable, pruneToSize);

                //int segmentations = Segmentations( word1.Length - i );

                for (String tgt : probMap.get(substring1)) {
                    Production alignment = new Production(substring1, tgt);

                    double alignmentProb = probs.get(alignment);

                    for (String key : appends.keySet()) {
                        Double value = appends.get(key);
                        String word = alignment.getSecond() + key;
                        //double combinedProb = (pair.Value/segmentations) * alignmentProb;
                        double combinedProb = (value) * alignmentProb;

                        // I hope this is an accurate translation...
                        Dictionaries.IncrementOrSet(result, word, combinedProb, combinedProb);
                    }
                }

            }
        }

        if (result.size() > pruneToSize) {
            Double[] valuesArray = result.values().toArray(new Double[result.values().size()]);
            String[] data = result.keySet().toArray(new String[result.size()]);

            //Array.Sort<Double, String> (valuesArray, data);

            TreeMap<Double, String> sorted = new TreeMap<>();
            for(int i = 0 ; i < valuesArray.length; i++){
                sorted.put(valuesArray[i], data[i]);
            }

            // FIXME: is this sorted in the correct order???

            //double sum = 0;
            //for (int i = data.Length - pruneToSize; i < data.Length; i++)
            //    sum += valuesArray[i];

            result = new HashMap<>(pruneToSize);
//            for (int i = data.length - pruneToSize; i < data.length; i++)
//                result.put(data[i], valuesArray[i]);

            int i = 0;
            for(Double d : sorted.descendingKeySet()){
                result.put(sorted.get(d), d);
                if (i++ > pruneToSize){
                    break;
                }
            }
        }

        memoizationTable.put(word1, result);
        return result;
    }

    /**
     *
     * This makes sure to pad front and back of the string, if pad is True.
     *
     * @param n size of ngram
     * @param examples list of examples
     * @param pad whether or not we should use padding.
     * @return
     */
    public static HashMap<String, Integer> GetNgramCounts(int n, Iterable<String> examples, boolean pad) {
        HashMap<String, Integer> result = new HashMap<>();
        for (String example : examples) {
            String padstring = StringUtils.repeat("_", n-1);
            String paddedExample = (pad ? padstring + example + padstring : example);

            for (int i = 0; i <= paddedExample.length() - n; i++) {
                //System.out.println(i + ": " + n);
                Dictionaries.IncrementOrSet(result, paddedExample.substring(i, i+n), 1, 1);
            }
        }

        return result;
    }

    public static HashMap<String, Double> GetFixedSizeNgramProbs(int n, Iterable<String> examples) {
        HashMap<String, Integer> ngramCounts = GetNgramCounts(n, examples, true);
        HashMap<String, Integer> ngramTotals = new HashMap<>();
        for (String key : ngramCounts.keySet()) {
            int v = ngramCounts.get(key);
            Dictionaries.IncrementOrSet(ngramTotals, key.substring(0, n - 1), v, v);
        }

        HashMap<String, Double> result = new HashMap<>(ngramCounts.size());
        for (String key : ngramCounts.keySet()) {
            int v = ngramCounts.get(key);
            result.put(key, ((double) v) / ngramTotals.get(key.substring(0, n - 1)));
        }

        return result;
    }

    public static HashMap<String, Double> GetNgramProbs(int minN, int maxN, Iterable<String> examples) {
        HashMap<String, Double> result = new HashMap<>();

        for (int i = minN; i <= maxN; i++) {
            HashMap<String, Double> map = GetFixedSizeNgramProbs(i, examples);
            for (String key : map.keySet()) {
                double value = map.get(key);
                result.put(key, value);
            }
        }

        return result;
    }

    /**
     * This function loops over all sizes of ngrams, from minN to maxN, and creates
     * an ngram model, and also normalizes it.
     *
     * @param minN minimum size ngram
     * @param maxN maximum size ngram
     * @param examples list of examples
     * @param padding whether or not this should be padded
     * @return a hashmap of ngrams.
     */
    public static HashMap<String, Double> GetNgramCounts(int minN, int maxN, Iterable<String> examples, boolean padding) {
        HashMap<String, Double> result = new HashMap<>();
        for (int i = minN; i <= maxN; i++) {
            HashMap<String, Integer> counts = GetNgramCounts(i, examples, padding);
            int total = 0;
            for (int v : counts.values()) {
                total += v;
            }

            for (String key : counts.keySet()) {
                int value = counts.get(key);
                result.put(key, ((double) value) / total);
            }
        }

        return result;
    }



    /**
     * Given a map of productions and corresponding counts, get the counts of the source word in each
     * production.
     * @param counts production counts
     * @return a map from source strings to counts.
     */
    public static HashMap<String, Double> GetAlignmentTotals1(HashMap<Production, Double> counts) {
        // the string in this map is the source string.
        HashMap<String, Double> result = new HashMap<>();
        for (Production key : counts.keySet()) {
            Double value = counts.get(key);

            String source = key.getFirst();

            // Increment or set
            if(result.containsKey(source)){
                result.put(source, result.get(source) + value);
            }else{
                result.put(source, value);
            }
        }

        return result;
    }

    /**
     * This finds all possible alignments between word1 and word2.
     * @param word1
     * @param word2
     * @param maxSubstringLength1
     * @param maxSubstringLength2
     * @param internTable
     * @param normalization
     * @return
     */
    public static HashMap<Production, Double> FindAlignments(String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, InternDictionary<String> internTable, NormalizationMode normalization) {
        HashMap<Production, Double> alignments = new HashMap<>();

        // this populates the alignments hashmap.
        // FIXME: why not assign to alignments here?
        // FIXME: why is it boolean? Is the value ever false? What does it mean?
        HashSet<Production> memoizationtable = new HashSet<>();
        FindAlignments(word1, word2, maxSubstringLength1, maxSubstringLength2, alignments, memoizationtable);

        // FIXME: probably don't need this? What about interning??
        HashMap<Production, Double> result = new HashMap<>(alignments.size());
        for (Production key : alignments.keySet()) {
            result.put(new Production(internTable.Intern(key.getFirst()), internTable.Intern(key.getSecond())), 1.0);
        }

        return Normalize(word1, word2, result, internTable, normalization);
    }

    /**
     * This does no normalization, but interns the string in each production.
     * @param counts
     * @param internTable
     * @return
     */
    public static HashMap<Production, Double> InternProductions(HashMap<Production, Double> counts, InternDictionary<String> internTable) {
        HashMap<Production, Double> result = new HashMap<>(counts.size());

        for (Production key : counts.keySet()) {
            Double value = counts.get(key);
            result.put(new Production(internTable.Intern(key.getFirst()), internTable.Intern(key.getSecond()), key.getOrigin()), value);
        }

        return result;
    }

    /**
     * This normalizes the raw counts by the counts of the source strings in each production.
     * Example:
     *    Raw counts={ Prod("John", "Yon")=>4, Prod("John","Jon")=>1 }
     *    Normalized={ Prod("John", "Yon")=>4/5, Prod("John","Jon")=>1/5}
     *
     * If source strings only ever show up for one target string, then this does nothing.
     *
     * @param counts raw counts
     * @param internTable
     * @return
     */
    public static HashMap<Production, Double> NormalizeBySourceSubstring(HashMap<Production, Double> counts, InternDictionary<String> internTable) {
        // gets counts by source strings
        HashMap<String, Double> totals = GetAlignmentTotals1(counts);

        HashMap<Production, Double> result = new HashMap<>(counts.size());

        for (Production key : counts.keySet()) {
            Double value = counts.get(key);
            result.put(new Production(internTable.Intern(key.getFirst()), internTable.Intern(key.getSecond())), value / totals.get(key.getFirst()));
        }

        return result;
    }

    public static HashMap<String, Double> GetSourceSubstringMax(HashMap<Pair<String, String>, Double> counts) {
        HashMap<String, Double> result = new HashMap<>(counts.size());
        for (Pair<String, String> key : counts.keySet()) {
            Double value = counts.get(key);
            if (result.containsKey(key.getFirst()))
                result.put(key.getFirst(), Math.max(value, result.get(key.getFirst())));
            else
                result.put(key.getFirst(), value);
        }

        return result;
    }

    public static HashMap<Production, Double> Normalize(String sourceWord, String targetWord, HashMap<Production, Double> counts, InternDictionary<String> internTable, NormalizationMode normalization) {
        if (normalization == NormalizationMode.BySourceSubstring)
            return NormalizeBySourceSubstring(counts, internTable);
//        else if (normalization == NormalizationMode.AllProductions)
//            return NormalizeAllProductions(counts, internTable);
//        else if (normalization == NormalizationMode.BySourceSubstringMax)
//            return NormalizeBySourceSubstringMax(counts, internTable);
//        else if (normalization == NormalizationMode.BySourceAndTargetSubstring)
//            return NormalizeBySourceAndTargetSubstring(counts, internTable);
//        else if (normalization == NormalizationMode.BySourceOverlap)
//            return NormalizeBySourceOverlap(sourceWord, counts, internTable);
//        else if (normalization == NormalizationMode.ByTargetSubstring)
//            return NormalizeByTargetSubstring(counts, internTable);
        else
            return InternProductions(counts, internTable);
    }

    public static HashMap<Production, Double> FindWeightedAlignments(String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Production, Double> probs, InternDictionary<String> internTable, NormalizationMode normalization) {
        HashMap<Production, Double> weights = new HashMap<>();
        FindWeightedAlignments(1, new ArrayList<Production>(), word1, word2, maxSubstringLength1, maxSubstringLength2, probs, weights, new HashMap<Production, Pair<Double, Double>>());

        //CheckDictionary(weights);

        HashMap<Production, Double> weights2 = new HashMap<>(weights.size());
        for (Production wkey : weights.keySet()) {
            weights2.put(wkey, weights.get(wkey) == 0 ? 0 : weights.get(wkey) / probs.get(wkey));
        }
        //weights2[wPair.Key] = weights[wPair.Key] == 0 ? 0 : Math.Pow(weights[wPair.Key], 1d / word1.Length);
        weights = weights2;

        return Normalize(word1, word2, weights, internTable, normalization);
    }

    public static HashMap<Production, Double> FindWeightedAlignmentsAverage(String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Production, Double> probs, InternDictionary<String> internTable, Boolean weightByOthers, NormalizationMode normalization) {
        HashMap<Production, Double> weights = new HashMap<>();
        HashMap<Production, Double> weightCounts = new HashMap<>();
        //FindWeightedAlignmentsAverage(1, new List<Pair<String, String>>(), word1, word2, maxSubstringLength1, maxSubstringLength2, probs, weights, weightCounts, new HashMap<Pair<String, String>, Pair<double, double>>(), weightByOthers);
        FindWeightedAlignmentsAverage(1, new ArrayList<Production>(), word1, word2, maxSubstringLength1, maxSubstringLength2, probs, weights, weightCounts, weightByOthers);

        //CheckDictionary(weights);

        HashMap<Production, Double> weights2 = new HashMap<>(weights.size());
        for (Production wkey : weights.keySet())
            weights2.put(wkey, weights.get(wkey) == 0 ? 0 : weights.get(wkey) / weightCounts.get(wkey));
        weights = weights2;

        return Normalize(word1, word2, weights, internTable, normalization);
    }

    public static double FindWeightedAlignments(double probability, List<Production> productions, String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Production, Double> probs, HashMap<Production, Double> weights, HashMap<Production, Pair<Double, Double>> memoizationTable) {
        if (word1.length() == 0 && word2.length() == 0) //record probabilities
        {
            for (Production production : productions) {
                if(weights.containsKey(production) && weights.get(production) > probability){
                    continue;
                }else{
                    weights.put(production, probability);
                }
            }
            return 1;
        }

        //Check memoization table to see if we can return early
        Pair<Double, Double> probPair;

        if(memoizationTable.containsKey(new Production(word1, word2))){
            probPair = memoizationTable.get(new Production(word1, word2));
            if (probPair.getFirst() >= probability) //we ran against these words with a higher probability before;
            {
                probability *= probPair.getSecond(); //get entire production sequence probability

                for (Production production : productions) {
                    if(weights.containsKey(production) && weights.get(production) > probability){
                        continue;
                    }else{
                        weights.put(production, probability);
                    }
                }

                return probPair.getSecond();
            }
        }

        int maxSubstringLength1f = Math.min(word1.length(), maxSubstringLength1);
        int maxSubstringLength2f = Math.min(word2.length(), maxSubstringLength2);

        double bestProb = 0;

        for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
        {
            String substring1 = word1.substring(0, i);

            for (int j = 1; j <= maxSubstringLength2f; j++) //for possible substring in the second
            {
                if ((word1.length() - i) * maxSubstringLength2 >= word2.length() - j && (word2.length() - j) * maxSubstringLength1 >= word1.length() - i) //if we get rid of these characters, can we still cover the remainder of word2?
                {
                    String substring2 = word2.substring(0, j);
                    Production production = new Production(substring1, substring2);
                    double prob = probs.get(production);

                    productions.add(production);
                    double thisProb = prob * FindWeightedAlignments(probability * prob, productions, word1.substring(i), word2.substring(j), maxSubstringLength1, maxSubstringLength2, probs, weights, memoizationTable);
                    productions.remove(productions.size() - 1);

                    if (thisProb > bestProb) bestProb = thisProb;
                }
            }
        }

        memoizationTable.put(new Production(word1, word2), new Pair<>(probability, bestProb));
        return bestProb;
    }

    public static double FindWeightedAlignmentsAverage(double probability, List<Production> productions, String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Production, Double> probs, HashMap<Production, Double> weights, HashMap<Production, Double> weightCounts, Boolean weightByOthers) {
        if (probability == 0) return 0;

        if (word1.length() == 0 && word2.length() == 0) //record probabilities
        {
            for (Production production : productions) {
                double probValue = weightByOthers ? probability / probs.get(production) : probability;
                //weight the contribution to the average by its probability (square it)
                Dictionaries.IncrementOrSet(weights, production, probValue * probValue, probValue * probValue);
                Dictionaries.IncrementOrSet(weightCounts, production, probValue, probValue);
            }
            return 1;
        }

        int maxSubstringLength1f = Math.min(word1.length(), maxSubstringLength1);
        int maxSubstringLength2f = Math.min(word2.length(), maxSubstringLength2);

        double bestProb = 0;

        for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
        {
            String substring1 = word1.substring(0, i);

            for (int j = 1; j <= maxSubstringLength2f; j++) //for possible substring in the second
            {
                if ((word1.length() - i) * maxSubstringLength2 >= word2.length() - j && (word2.length() - j) * maxSubstringLength1 >= word1.length() - i) //if we get rid of these characters, can we still cover the remainder of word2?
                {
                    String substring2 = word2.substring(0, j);
                    Production production = new Production(substring1, substring2);
                    double prob = probs.get(production);

                    productions.add(production);
                    double thisProb = prob * FindWeightedAlignmentsAverage(probability * prob, productions, word1.substring(i), word2.substring(j), maxSubstringLength1, maxSubstringLength2, probs, weights, weightCounts, weightByOthers);
                    productions.remove(productions.size() - 1);

                    if (thisProb > bestProb) bestProb = thisProb;
                }
            }
        }

        //memoizationTable[new Pair<String, String>(word1, word2)] = new Pair<double, double>(probability, bestProb);
        return bestProb;
    }


    /**
     * Finds the single best alignment for the two words and uses that to increment the counts.
     * WeighByProbability does not use the real, noramalized probability, but rather a proportional probability
     * and is thus not "theoretically valid".
     * @param word1
     * @param word2
     * @param maxSubstringLength
     * @param probs
     * @param internTable
     * @param weighByProbability
     * @return
     */
    public static HashMap<Production, Double> CountMaxAlignments(String word1, String word2, int maxSubstringLength, HashMap<Production, Double> probs, InternDictionary<String> internTable, Boolean weighByProbability) {
        
        Pair<Double,List<Production>> result1 = GetAlignmentProbabilityDebug(word1, word2, maxSubstringLength, probs);
        double prob = result1.getFirst();
        List<Production> productions = result1.getSecond();
        //CheckDictionary(weights);

        HashMap<Production, Double> result = new HashMap<>(productions.size());

        if (prob == 0) //no possible alignment for some reason
        {
            return result; //nothing learned //result.Add(new Pair<String,String>(internTable.Intern(word1),internTable.Intern(word2),
        }

        for (Production production : productions) {
            Dictionaries.IncrementOrSet(result, new Production(internTable.Intern(production.getFirst()), internTable.Intern(production.getSecond())), weighByProbability ? prob : 1, weighByProbability ? prob : 1);
        }


        return result;
    }

    /**
     * What does this do? Largely calls CountWeightedAlignmentsHelper
     * @param word1
     * @param word2
     * @param maxSubstringLength1
     * @param maxSubstringLength2
     * @param probs
     * @param internTable
     * @param normalization
     * @param weightByContextOnly
     * @return
     */
    public static HashMap<Production, Double> CountWeightedAlignments(String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Production, Double> probs, InternDictionary<String> internTable, NormalizationMode normalization, Boolean weightByContextOnly) {
        //HashMap<Pair<String, String>, double> weights = new HashMap<Pair<String, String>, double>();
        //HashMap<Pair<String, String>, double> weightCounts = new HashMap<Pair<String, String>, double>();
        //FindWeightedAlignmentsAverage(1, new List<Pair<String, String>>(), word1, word2, maxSubstringLength1, maxSubstringLength2, probs, weights, weightCounts, new HashMap<Pair<String, String>, Pair<double, double>>(), weightByOthers);
        Pair<HashMap<Production, Double>, Double> Q = CountWeightedAlignmentsHelper(word1, word2, maxSubstringLength1, maxSubstringLength2, probs, new HashMap<Production, Pair<HashMap<Production, Double>, Double>>());
        HashMap<Production, Double> weights = Q.getFirst();
        double probSum = Q.getSecond(); //the sum of the probabilities of all possible alignments

        // this is where the 1/y normalization happens for this word pair.
        HashMap<Production, Double> weights_norm = new HashMap<>(weights.size());
        for (Production key : weights.keySet()) {
            Double value = weights.get(key);
            if (weightByContextOnly) {
                double originalProb = probs.get(key);
                weights_norm.put(key, value == 0 ? 0 : (value / originalProb) / (probSum - value + (value / originalProb)));
            } else
                weights_norm.put(key, value == 0 ? 0 : value / probSum);
        }

        return Normalize(word1, word2, weights_norm, internTable, normalization);
    }

    /**
     * Gets counts for productions by (conceptually) summing over all the possible alignments
     * and weighing each alignment (and its constituent productions) by the given probability table.
     * probSum is important (and memoized for input word pairs)--it keeps track and returns the sum of the
     * probabilities of all possible alignments for the word pair
     *
     * This is Algorithm 3 in the paper.
     *
     * @param word1
     * @param word2
     * @param maxSubstringLength1
     * @param maxSubstringLength2
     * @param probs
     * @param memoizationTable
     * @return a hashmap and double as a pair. The double is y, a normalization constant. The hashmap is a table of substring pairs
     * and their unnormalized counts
     */
    public static Pair<HashMap<Production, Double>, Double> CountWeightedAlignmentsHelper(String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Production, Double> probs, HashMap<Production, Pair<HashMap<Production, Double>, Double>> memoizationTable) {
        double probSum;

        Pair<HashMap<Production, Double>, Double> memoization;
        for(int orig = 0; orig < SPModel.numOrigins; orig++) {
            if (memoizationTable.containsKey(new Production(word1, word2, orig))) {
                memoization = memoizationTable.get(new Production(word1, word2, orig));
                probSum = memoization.getSecond(); //stored probSum
                return new Pair<>(memoization.getFirst(), probSum); //table of probs
            }
        }

        HashMap<Production, Double> result = new HashMap<>(); // this is C in Algorithm 3 in the paper
        probSum = 0; // this is R in Algorithm 3 in the paper

        if (word1.length() == 0 && word2.length() == 0) //record probabilities
        {
            probSum = 1; //null -> null is always a perfect alignment
            return new Pair<>(result,probSum); //end of the line
        }

        int maxSubstringLength1f = Math.min(word1.length(), maxSubstringLength1);
        int maxSubstringLength2f = Math.min(word2.length(), maxSubstringLength2);

        for(int orig = 0; orig < SPModel.numOrigins; orig++) {
            for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
            {
                String substring1 = word1.substring(0, i);

                for (int j = 1; j <= maxSubstringLength2f; j++) //for possible substring in the second
                {
                    if ((word1.length() - i) * maxSubstringLength2 >= word2.length() - j && (word2.length() - j) * maxSubstringLength1 >= word1.length() - i) //if we get rid of these characters, can we still cover the remainder of word2?
                    {
                        String substring2 = word2.substring(0, j);

                        Production production = new Production(substring1, substring2, orig);
                        double prob = probs.get(production);

                        // recurse here. Result is Q in Algorithm 3
                        Pair<HashMap<Production, Double>, Double> Q = CountWeightedAlignmentsHelper(word1.substring(i), word2.substring(j), maxSubstringLength1, maxSubstringLength2, probs, memoizationTable);

                        HashMap<Production, Double> remainderCounts = Q.getFirst();
                        Double remainderProbSum = Q.getSecond();

                        Dictionaries.IncrementOrSet(result, production, prob * remainderProbSum, prob * remainderProbSum);

                        //update our probSum
                        probSum += remainderProbSum * prob;

                        //update all the productions that come later to take into account their preceding production's probability
                        for (Production key : remainderCounts.keySet()) {
                            Double value = remainderCounts.get(key);
                            Dictionaries.IncrementOrSet(result, key, prob * value, prob * value);
                        }
                    }
                }
            }
        }

        for(int orig = 0; orig < SPModel.numOrigins; orig++) {
            memoizationTable.put(new Production(word1, word2, orig), new Pair<>(result, probSum));
        }
        return new Pair<>(result, probSum);
    }

    public static String[] GetLeftFallbackContexts(String word, int position, int contextSize) {
        String[] result = new String[contextSize + 1];
        for (int i = 0; i < result.length; i++)
            result[i] = word.substring(position - i, position);

        return result;
    }

    public static String[] GetRightFallbackContexts(String word, int position, int contextSize) {
        String[] result = new String[contextSize + 1];
        for (int i = 0; i < result.length; i++)
            result[i] = word.substring(position, position+i);

        return result;
    }

    public static String GetLeftContext(String word, int position, int contextSize) {
        return word.substring(position - contextSize, position);
    }

    public static String GetRightContext(String word, int position, int contextSize) {
        return word.substring(position, position+contextSize);
    }

    /**
     * Finds the probability of word1 transliterating to word2 over all possible alignments
     * This is Algorithm 1 in the paper.
     * @param word1 Source word
     * @param word2 Transliterated word
     * @param maxSubstringLength1 constant field from SPModel
     * @param maxSubstringLength2 constant field from SPModel
     * @param probs map from production to weight??
     * @param memoizationTable
     * @param minProductionProbability
     * @return
     */
    public static double GetSummedAlignmentProbability(String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Production, Double> probs, HashMap<Production, Double> memoizationTable, double minProductionProbability, int origin) {

        if(memoizationTable.containsKey(new Production(word1, word2, origin))){
            return memoizationTable.get(new Production(word1, word2, origin));
        }

        if (word1.length() == 0 && word2.length() == 0) //record probabilities
            return 1; //null -> null is always a perfect alignment

        double probSum = 0;

        int maxSubstringLength1f = Math.min(word1.length(), maxSubstringLength1);
        int maxSubstringLength2f = Math.min(word2.length(), maxSubstringLength2);

        double localMinProdProb = 1;
        for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
        {
            localMinProdProb *= minProductionProbability;

            String substring1 = word1.substring(0, i);

            for (int j = 1; j <= maxSubstringLength2f; j++) //for possible substring in the second
            {
                //if we get rid of these characters, can we still cover the remainder of word2?
                if ((word1.length() - i) * maxSubstringLength2 >= word2.length() - j && (word2.length() - j) * maxSubstringLength1 >= word1.length() - i)
                {
                    String substring2 = word2.substring(0, j);
                    Production production = new Production(substring1, substring2, origin);

                    double prob = 0;

                    if(!probs.containsKey(production)){
                        if (localMinProdProb == 0){
                            continue;
                        }
                    }else{
                        prob = probs.get(production);
                    }

                    prob = Math.max(prob, localMinProdProb);

                    double remainderProbSum = GetSummedAlignmentProbability(word1.substring(i), word2.substring(j), maxSubstringLength1, maxSubstringLength2, probs, memoizationTable, minProductionProbability, origin);

                    //update our probSum
                    probSum += remainderProbSum * prob;
                }
            }
        }

        memoizationTable.put(new Production(word1, word2), probSum);
        return probSum;
    }

    /**
     * This recursively finds all possible alignments between word1 and word2 and populates the alignments hashmap with them.
     *
     * @param word1 word or substring of a word
     * @param word2 word or substring of a word
     * @param maxSubstringLength1
     * @param maxSubstringLength2
     * @param alignments this is the result
     * @param memoizationTable
     */
    public static void FindAlignments(String word1, String word2, int maxSubstringLength1, int maxSubstringLength2, HashMap<Production, Double> alignments, HashSet<Production> memoizationTable) {
        if (memoizationTable.contains(new Production(word1, word2)))
            return; //done

        int maxSubstringLength1f = Math.min(word1.length(), maxSubstringLength1);
        int maxSubstringLength2f = Math.min(word2.length(), maxSubstringLength2);

        for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
        {
            String substring1 = word1.substring(0, i);

            for (int j = 1; j <= maxSubstringLength2f; j++) //for possible substring in the second
            {
                //if we get rid of these characters, can we still cover the remainder of word2?
                if ((word1.length() - i) * maxSubstringLength2 >= word2.length() - j && (word2.length() - j) * maxSubstringLength1 >= word1.length() - i)
                {
                    alignments.put(new Production(substring1, word2.substring(0, j)), 1.0);
                    FindAlignments(word1.substring(i), word2.substring(j), maxSubstringLength1, maxSubstringLength2, alignments, memoizationTable);
                }
            }
        }

        memoizationTable.add(new Production(word1, word2));
    }
}
