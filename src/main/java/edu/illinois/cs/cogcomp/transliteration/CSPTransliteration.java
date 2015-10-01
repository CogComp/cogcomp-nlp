package edu.illinois.cs.cogcomp.transliteration;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.Triple;
import edu.illinois.cs.cogcomp.utils.InternDictionary;
import edu.illinois.cs.cogcomp.utils.SparseDoubleVector;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

class CSPTransliteration
{

    public static SparseDoubleVector<Pair<Triple<String, String, String>, String>> CreateFallback(SparseDoubleVector<Pair<Triple<String, String, String>, String>> counts, InternDictionary<String> internTable)
    {
        SparseDoubleVector<Pair<Triple<String, String, String>, String>> result = new SparseDoubleVector<Pair<Triple<String, String, String>, String>>();
        for (Pair<Pair<Triple<String, String, String>, String>, Double> pair : counts)
        {
            for (int i = 0; i <= pair.Key.x.x.Length; i++)
            {
                result[new Pair<Triple<String, String, String>, String>(
                        new Triple<String, String, String>(
                                internTable.Intern(pair.Key.x.x.Substring(i)),
                                internTable.Intern(pair.Key.x.y),
                                internTable.Intern(pair.Key.x.z.Substring(0, pair.Key.x.z.Length - i))), pair.Key.y)] += pair.Value;
            }
        }

        return result;
    }

    public static SparseDoubleVector<Triple<String, String, String>> CreateFallback(SparseDoubleVector<Triple<String, String, String>> segCounts, InternDictionary<String> internTable)
    {
        SparseDoubleVector<Triple<String, String, String>> result = new SparseDoubleVector<Triple<String, String, String>>();
        for (Pair<Triple<String, String, String>, Double> pair : segCounts)
        {
            for (int i = 0; i <= pair.Key.x.Length; i++)
            {
                result[
                        new Triple<>(
                                internTable.Intern(pair.Key.x.Substring(i)),
                                internTable.Intern(pair.Key.y),
                                internTable.Intern(pair.Key.z.Substring(0, pair.Key.z.Length - i)))] += pair.Value;
            }
        }

        return result;
    }

    public static CSPModel LearnModel(List<Triple<String, String, Double>> examples, CSPModel model)
    {
        // odd notation...
        CSPExampleCounts counts = new CSPTransliteration().new CSPExampleCounts();
        //CSPExampleCounts intCounts = new CSPExampleCounts();

        counts.productionCounts = new SparseDoubleVector<>();
        counts.segCounts = new SparseDoubleVector<>();

        for (Triple<String, String, Double> example : examples)
        {
            CSPExampleCounts exampleCount = LearnModel(example.getFirst(), example.getSecond(), model);
            counts.productionCounts.put(example.getThird(), exampleCount.productionCounts);
            counts.segCounts.put(example.getThird(), exampleCount.segCounts);

            //intCounts.productionCounts += exampleCount.productionCounts.Sign();
            //intCounts.segCounts += exampleCount.segCounts.Sign();
        }

        CSPModel result = (CSPModel)model.clone();

        //normalize to get "joint"
        result.productionProbs = counts.productionCounts.divide(counts.productionCounts.PNorm(1));

        InternDictionary<String> internTable = new InternDictionary<>();

        SparseDoubleVector<Pair<Triple<String, String, String>, String>> oldProbs=null;
        if (model.underflowChecking)
            oldProbs = result.productionProbs;

        //now get production fallbacks
        result.productionProbs = CreateFallback(result.productionProbs, internTable);

        //finally, make it conditional
        result.productionProbs = PSecondGivenFirst(result.productionProbs);

        if (model.underflowChecking)
        {
            //go through and ensure that Sum_X(P(Y|X)) == 1...or at least > 0!
            SparseDoubleVector<Triple<String, String, String>> sums = new SparseDoubleVector<>();
            for (Pair<Triple<String, String, String>, String> key : model.productionProbs.keySet()) {
                Double value = model.productionProbs.get(key);

                Triple<String, String, String> ff = key.getFirst();

                Double val = sums.get(ff);
                sums.put(ff, val + value);
            }

            List<Pair<Triple<String, String, String>, String>> restoreList = new ArrayList<>();

            for (Pair<Triple<String, String, String>, String> key : model.productionProbs.keySet()) {
                Double value = model.productionProbs.get(key);

                if (value == 0 && sums.get(key.getFirst()) == 0)
                    restoreList.add(key);
            }


            for (Pair<Triple<String, String, String>, String> pair : restoreList) {
                //model.productionProbs[pair] = oldProbs[pair];
                model.productionProbs.put(pair, oldProbs.get(pair));
            }

        }

        //get conditional segmentation probs
        if (model.segMode == CSPModel.SegMode.Count)
            result.segProbs = CreateFallback(PSegGivenFlatOccurence(counts.segCounts,examples,model.segContextSize),internTable); // counts.segCounts / counts.segCounts.PNorm(1);
        else if (model.segMode == CSPModel.SegMode.Entropy)
        {
            SparseDoubleVector<Triple<String, String, String>> totals = new SparseDoubleVector<Triple<String, String, String>>();
            for (Pair<Triple<String, String, String>, String> key : result.productionProbs.keySet())
            {
                Double value = result.productionProbs.get(key);

                //totals[pair.Key.x] -= pair.Value * Math.Log(pair.Value, 2);
                double logValue = value * Math.log(value);

                if (!Double.isNaN(logValue)) {
                    Double lv = totals.get(key.getFirst());
                    totals.put(key.getFirst(), lv + logValue);
                }
                //totals[pair.Key.x] *= Math.Pow(pair.Value, pair.Value);
            }

            result.segProbs = totals.Exp(); //totals.Max(0.000001).Pow(-1);
        }

        //return the finished model
        return result;
    }

    private static Triple<String, String, String> GetContextTriple(String originalWord, int index, int length, int contextSize)
    {
        return new Triple<>(WikiTransliteration.GetLeftContext(originalWord, index, contextSize),
                originalWord.substring(index, length),
                WikiTransliteration.GetRightContext(originalWord, index + length, contextSize));
    }

    public static SparseDoubleVector<Triple<String, String, String>> PSegGivenFlatOccurence(SparseDoubleVector<Triple<String, String, String>> segCounts, List<Triple<String, String, Double>> examples, int contextSize)
    {
        SparseDoubleVector<Triple<String, String, String>> counts = new SparseDoubleVector<Triple<String, String, String>>();
        for (Triple<String, String, Double> example : examples)
        {
            String word = StringUtils.repeat('_', contextSize) + example.getFirst() + StringUtils.repeat('_', contextSize);
            for (int i = contextSize; i < word.length() - contextSize; i++)
                for (int j = i; j < word.length() - contextSize; j++)
                    counts[GetContextTriple(word, i, j - i + 1, contextSize)] += example.getThird();
        }

        return segCounts / counts;
    }

    public static SparseDoubleVector<Triple<String, String, String>> PSegGivenLength(SparseDoubleVector<Triple<String, String, String>> segCounts)
    {
        SparseDoubleVector<Integer> sums = new SparseDoubleVector<>();
        for (Pair<Triple<String, String, String>, Double> pair : segCounts)
        sums[pair.Key.y.Length] += pair.Value;

        SparseDoubleVector<Triple<String, String, String>> result = new SparseDoubleVector<Triple<String,String,String>>(segCounts.Count);
        for (Pair<Triple<String, String, String>, Double> pair : segCounts)
        result[pair.Key] = pair.Value / sums[pair.Key.y.Length];

        return result;
    }

    public static SparseDoubleVector<Pair<Triple<String, String, String>, String>> PSecondGivenFirst(SparseDoubleVector<Pair<Triple<String, String, String>, String>> counts)
    {
        SparseDoubleVector<Triple<String, String, String>> totals = new SparseDoubleVector<Triple<String, String, String>>();
        for (Pair<Pair<Triple<String, String, String>, String>, Double> pair : counts)
        totals[pair.Key.x] += pair.Value;

        SparseDoubleVector<Pair<Triple<String, String, String>, String>> result = new SparseDoubleVector<Pair<Triple<String, String, String>, String>>(counts.Count);
        for (Pair<Pair<Triple<String, String, String>, String>, Double> pair : counts)
        {
            double total = totals[pair.Key.x];
            result[pair.Key] = total == 0 ? 0 : pair.Value / total;
        }

        return result;
    }

    public static CSPExampleCounts LearnModel(String word1, String word2, CSPModel model)
    {
        CSPExampleCounts result = new CSPTransliteration().new CSPExampleCounts();

        int paddingSize = Math.max(model.productionContextSize, model.segContextSize);
        String paddedWord = StringUtils.repeat('_', paddingSize) + word1 + StringUtils.repeat('_', paddingSize);
        HashMap<Triple<Double,String,String>,Pair<SparseDoubleVector<Triple<Integer,String,String>>,Double>> lastArg = new HashMap<>();

        Pair<SparseDoubleVector<Triple<Integer, String, String>>, Double> raw = LearnModel(paddingSize, paddedWord, word1, word2, model, lastArg);

        if (raw.getSecond() == 0)
            raw.setFirst(new SparseDoubleVector<Triple<Integer, String, String>>());
        else
            //raw.x = Program.segSums[Math.Min(39,word1.length()-1)][Math.Min(39,word2.length()-1)] * (raw.x);
            raw.setFirst( raw.getFirst().divide(raw.getSecond()) );
        //raw.x = raw.y >= 1 ? raw.x : raw.x / raw.y;

        if (model.emMode == CSPModel.EMMode.MaxSourceSeg)
        {
            HashMap<Pair<Integer, String>, Triple<Integer, String, String>> bestProdProbs = new HashMap<>();
            SparseDoubleVector<Pair<Integer, String>> maxProdProbs = new SparseDoubleVector<>();
            for (Triple<Integer, String, String> key : raw.getFirst().keySet()) {
                Double value = raw.getFirst().get(key);

                Pair<Integer, String> keyXY = new Pair<>(key.getFirst(), key.getSecond());

                if (maxProdProbs.get(keyXY) < value) {
                    bestProdProbs.put(keyXY, key);
                    maxProdProbs.put(keyXY, value);
                }
            }

            raw.getFirst().Clear();
            for (Triple<Integer, String, String> triple : bestProdProbs.values())
                raw.getFirst().put(triple, 1.0);

        }
        else if (model.emMode == CSPModel.EMMode.BySourceSeg)
        {
            //Dictionary<Pair<int, String>, Triple<int, String, String>> bestProdProbs = new Dictionary<Pair<int, String>, Triple<int, String, String>>();
            SparseDoubleVector<Pair<Integer, String>> sumProdProbs = new SparseDoubleVector<>();
            for (Triple<Integer, String, String> key : raw.getFirst().keySet()) {
                Double value = raw.getFirst().get(key);
                Pair<Integer, String> keyXY = new Pair<>(key.getFirst(), key.getSecond());
                sumProdProbs.put(keyXY, sumProdProbs.get(keyXY) + value);
            }

            SparseDoubleVector<Triple<Integer, String, String>> newCounts = new SparseDoubleVector<>(raw.getFirst().size());
            for (Triple<Integer, String, String> key : raw.getFirst().keySet()) {
                Double value = raw.getFirst().get(key);
                Pair<Integer, String> keyXY = new Pair<>(key.getFirst(), key.getSecond());
                newCounts.put(key, value / sumProdProbs.get(keyXY));
            }
            raw.setFirst(newCounts);
        }

        result.productionCounts = new SparseDoubleVector<>(raw.getFirst().size());
        result.segCounts = new SparseDoubleVector<>(raw.getFirst().size());

        for (Triple<Integer, String, String> key : raw.getFirst().keySet()) {
            Double value = raw.getFirst().get(key);
            Pair<Triple<String, String, String>, String> pckey = new Pair<>(new Triple<>(WikiTransliteration.GetLeftContext(paddedWord, key.getFirst(), model.productionContextSize), key.getSecond(), WikiTransliteration.GetRightContext(paddedWord, key.getFirst() + key.getSecond().length(), model.productionContextSize)), key.getThird());
            result.productionCounts.put(pckey, result.productionCounts.get(pckey) + value);

            Triple<String, String, String> sckey = new Triple<>(WikiTransliteration.GetLeftContext(paddedWord, key.getFirst(), model.segContextSize), key.getSecond(), WikiTransliteration.GetRightContext(paddedWord, key.getFirst() + key.getSecond().length(), model.segContextSize));
            result.segCounts.put(sckey, result.segCounts.get(sckey) + value);
        }

        return result;
    }

    public static char[] vowels = new char[] { 'a', 'e', 'i', 'o', 'u', 'y' };

    /**
     * Gets counts for productions by (conceptually) summing over all the possible alignments
     * and weighing each alignment (and its constituent productions) by the given probability table.
     * probSum is important (and memoized for input word pairs)--it keeps track and returns the sum of the probabilities of all possible alignments for the word pair
     * @param position
     * @param originalWord1
     * @param word1
     * @param word2
     * @param model
     * @param memoizationTable
     * @return
     */
     public static Pair<SparseDoubleVector<Triple<Integer, String, String>>, Double> LearnModel(int position, String originalWord1, String word1, String word2, CSPModel model, HashMap<Triple<Double, String, String>, Pair<SparseDoubleVector<Triple<Integer, String, String>>, Double>> memoizationTable)
    {
        Pair<SparseDoubleVector<Triple<Integer, String, String>>, Double> memoization;

        Triple check = new Triple<>(position, word1, word2);
        if(memoizationTable.containsKey(check)){
            return memoizationTable.get(check);
        }

        Pair<SparseDoubleVector<Triple<Integer, String, String>>, Double> result
                = new Pair<>(new SparseDoubleVector<Triple<Integer, String, String>>(), 0.0);

        if (word1.length() == 0 && word2.length() == 0) //record probabilities
        {
            result.setSecond(1.0); //null -> null is always a perfect alignment
            return result; //end of the line
        }

        int maxSubstringLength1f = Math.min(word1.length(), model.maxSubstringLength);
        int maxSubstringLength2f = Math.min(word2.length(), model.maxSubstringLength);

        String[] leftContexts = WikiTransliteration.GetLeftFallbackContexts(originalWord1,position, Math.max(model.segContextSize, model.productionContextSize));

        int firstVowel = -1; int secondVowel = -1;
        if (model.syllabic)
        {
            for (int i = 0; i < word1.length(); i++) {

                if (Arrays.asList(vowels).contains(word1.charAt(i))){
                    firstVowel = i;
                }else if(firstVowel >= 0){
                    break;
                }
            }

            if (firstVowel == -1)
                firstVowel = word1.length() - 1; //no vowels!

            for (int i = firstVowel + 1; i < word1.length(); i++) {
                if (Arrays.asList(vowels).contains(word1.charAt(i))) {
                    secondVowel = i;
                    break;
                }
            }

            if (secondVowel == -1 || (secondVowel == word1.length() - 1 && word1.charAt(secondVowel) == 'e')) //if only one vowel, only consider the entire thing; note consideration of silent 'e' at end of words
            {
                firstVowel = maxSubstringLength1f - 1;
                secondVowel = maxSubstringLength1f;
            }
        }
        else
        {
            firstVowel = 0;
            secondVowel = maxSubstringLength1f;
        }

        for (int i = firstVowel + 1; i <= secondVowel; i++)
        //for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
        {
            String substring1 = word1.substring(0, i);
            String[] rightContexts = WikiTransliteration.GetRightFallbackContexts(originalWord1, position + i, Math.max(model.segContextSize, model.productionContextSize));

            double segProb;
            if (model.segProbs.size() == 0)
                segProb = 1;
            else
            {
                segProb = 0;
                int minK = model.fallbackStrategy == FallbackStrategy.NotDuringTraining ? model.segContextSize : 0;
                for (int k = model.segContextSize; k >= minK; k--){
                    if (model.segProbs.containsKey(new Triple<>(leftContexts[k], substring1, rightContexts[k]))){
                        segProb = model.segProbs.get(new Triple<>(leftContexts[k], substring1, rightContexts[k]));
                        break;
                    }
                }
            }

            for (int j = 1; j <= maxSubstringLength2f; j++) //foreach possible substring in the second
            {
                if ((word1.length() - i) * model.maxSubstringLength >= word2.length() - j && (word2.length() - j) * model.maxSubstringLength >= word1.length() - i) //if we get rid of these characters, can we still cover the remainder of word2?
                {
                    String substring2 = word2.substring(0, j);
                    //Pair<Triple<String, String, String>, String> production = new Pair<Triple<String, String, String>, String>(new Triple<String, String, String>(leftProductionContext, substring1, rightProductionContext), substring2);

                    double prob;
                    if (model.productionProbs.size() == 0)
                        prob = 1;
                    else
                    {
                        prob = 0;
                        int minK = model.fallbackStrategy == FallbackStrategy.NotDuringTraining ? model.productionContextSize : 0;
                        for (int k = model.productionContextSize; k >= minK; k--) {
                            Pair<Triple<String, String, String>, String> v = new Pair<>(new Triple<>(leftContexts[k], substring1, rightContexts[k]), substring2);
                            if (model.productionProbs.containsKey(v)) {
                                prob = model.productionProbs.get(v);
                                break;
                            }
                        }

                        if (model.emMode == CSPModel.EMMode.Smoothed) prob = Math.max(model.minProductionProbability, prob);
                    }

                    Pair<SparseDoubleVector<Triple<Integer, String, String>>, Double> remainder = LearnModel(position + i, originalWord1, word1.substring(i), word2.substring(j), model, memoizationTable);

                    double cProb = prob * segProb;

                    //record this production in our results

                    result.getFirst().put(cProb, remainder.getFirst());
                    result.setSecond(result.getSecond() + remainder.getSecond() * cProb);

                    result.getFirst()[new Triple<>(position, substring1, substring2)] += cProb * remainder.getSecond();
                }
            }
        }

        memoizationTable.put(new Triple<>((double)position, word1, word2), result);
        return result;
    }


    public static double GetProbability(String word1, String word2, CSPModel model)
    {
        int paddingSize = Math.max(model.productionContextSize, model.segContextSize);
        String paddedWord = StringUtils.repeat('_', paddingSize) + word1 + StringUtils.repeat('_', paddingSize);

        if (model.segMode != CSPModel.SegMode.Best)
        {
            Pair<double, double> raw = GetProbability(paddingSize, paddedWord, word1, word2, model, new Dictionary<Triple<int, String, String>, Pair<double, double>>());
            return raw.x / raw.y; //normalize the segmentation probabilities by dividing by the sum of probabilities for all segmentations
        }
        else
            return GetBestProbability(paddingSize, paddedWord, word1, word2, model, new Dictionary<Triple<int, String, String>, double>());
    }

    //Gets the "best" alignment for a given word pair, defined as max P(s,t|S,T).
    public static double GetBestProbability(int position, String originalWord1, String word1, String word2, CSPModel model, Dictionary<Triple<int, String, String>, double> memoizationTable)
    {
        double result;
        if (memoizationTable.TryGetValue(new Triple<int, String, String>(position, word1, word2), out result))
            return result; //we've been down this road before

        result = 0;

        if (word1.length() == 0 && word2.length() == 0)
            return 1; //perfect null-to-null alignment


        int maxSubstringLength1f = Math.min(word1.length(), model.maxSubstringLength);
        int maxSubstringLength2f = Math.min(word2.length(), model.maxSubstringLength);

        String[] leftContexts = WikiTransliteration.GetLeftFallbackContexts(originalWord1, position, Math.max(model.segContextSize, model.productionContextSize));

        double minProductionProbability1 = 1;

        for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
        {
            minProductionProbability1 *= model.minProductionProbability;

            String substring1 = word1.substring(0, i);
            String[] rightContexts = WikiTransliteration.GetRightFallbackContexts(originalWord1, position + i, Math.max(model.segContextSize, model.productionContextSize));

            double segProb;
            if (model.segProbs.Count == 0)
                segProb = 1;
            else
            {
                segProb = 0;
                for (int k = model.productionContextSize; k >= 0; k--)
                    if (model.segProbs.TryGetValue(new Triple<String, String, String>(leftContexts[k], substring1, rightContexts[k]), out segProb))
                        break;
            }

            double minProductionProbability2 = 1;
            for (int j = 1; j <= maxSubstringLength2f; j++) //foreach possible substring in the second
            {
                minProductionProbability2 *= model.minProductionProbability;

                if ((word1.length() - i) * model.maxSubstringLength >= word2.length() - j && (word2.length() - j) * model.maxSubstringLength >= word1.length() - i) //if we get rid of these characters, can we still cover the remainder of word2?
                {
                    double minProductionProbability;
                    if (model.smoothMode == SmoothMode.BySource)
                        minProductionProbability = minProductionProbability1;
                    else if (model.smoothMode == SmoothMode.ByMax)
                        minProductionProbability = Math.Min(minProductionProbability1, minProductionProbability2);
                    else //if (model.smoothMode == SmoothMode.BySum)
                        minProductionProbability = minProductionProbability1 * minProductionProbability2;

                    String substring2 = word2.Substring(0, j);
                    //Pair<Triple<String, String, String>, String> production = new Pair<Triple<String, String, String>, String>(new Triple<String, String, String>(leftProductionContext, substring1, rightProductionContext), substring2);

                    double prob;
                    if (model.productionProbs.Count == 0)
                        prob = 1;
                    else
                    {
                        prob = 0;
                        for (int k = model.productionContextSize; k >= 0; k--)
                            if (model.productionProbs.TryGetValue(new Pair<Triple<String, String, String>, String>(new Triple<String, String, String>(leftContexts[k], substring1, rightContexts[k]), substring2), out prob))
                                break;

                        prob = Math.Max(prob, minProductionProbability);
                    }

                    double remainder = prob * GetBestProbability(position + i, originalWord1, word1.Substring(i), word2.Substring(j), model, memoizationTable);

                    if (remainder > result) result = remainder; //maximize

                    //record this remainder in our results
                    //result.x += remainder.x * prob * segProb;
                    //result.y += remainder.y * segProb;
                }
            }
        }

        memoizationTable[new Triple<Integer, String, String>(position, word1, word2)] = result;
        return result;
    }

    //Gets counts for productions by (conceptually) summing over all the possible alignments
    //and weighing each alignment (and its constituent productions) by the given probability table.
    //probSum is important (and memoized for input word pairs)--it keeps track and returns the sum of the probabilities of all possible alignments for the word pair
    public static Pair<double, double> GetProbability(int position, String originalWord1, String word1, String word2, CSPModel model, Dictionary<Triple<int, String, String>, Pair<double, double>> memoizationTable)
    {
        Pair<double, double> result;
        if (memoizationTable.TryGetValue(new Triple<int, String, String>(position, word1, word2), out result))
            return result; //we've been down this road before

        result = new Pair<Double, Double>(0, 0);

        if (word1.length() == 0 && word2.length() == 0) //record probabilities
        {
            result.x = 1; //null -> null is always a perfect alignment
            result.y = 1;
            return result; //end of the line
        }

        int maxSubstringLength1f = Math.min(word1.length(), model.maxSubstringLength);
        int maxSubstringLength2f = Math.min(word2.length(), model.maxSubstringLength);

        String[] leftContexts = WikiTransliteration.GetLeftFallbackContexts(originalWord1, position, Math.Max(model.segContextSize, model.productionContextSize));

        double minProductionProbability1 = 1;

        for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
        {
            minProductionProbability1 *= model.minProductionProbability;

            String substring1 = word1.substring(0, i);
            String[] rightContexts = WikiTransliteration.GetRightFallbackContexts(originalWord1, position + i, Math.Max(model.segContextSize, model.productionContextSize));

            double segProb;
            if (model.segProbs.Count == 0)
                segProb = 1;
            else
            {
                segProb = 0;
                for (int k = model.productionContextSize; k >= 0; k--)
                    if (model.segProbs.TryGetValue(new Triple<String, String, String>(leftContexts[k], substring1, rightContexts[k]), out segProb))
                        break;
            }

            double minProductionProbability2 = 1;
            for (int j = 1; j <= maxSubstringLength2f; j++) //foreach possible substring in the second
            {
                minProductionProbability2 *= model.minProductionProbability;

                if ((word1.length() - i) * model.maxSubstringLength >= word2.length() - j && (word2.length() - j) * model.maxSubstringLength >= word1.length() - i) //if we get rid of these characters, can we still cover the remainder of word2?
                {
                    double minProductionProbability;
                    if (model.smoothMode == SmoothMode.BySource)
                        minProductionProbability = minProductionProbability1;
                    else if (model.smoothMode == SmoothMode.ByMax)
                        minProductionProbability = Math.Min(minProductionProbability1, minProductionProbability2);
                    else //if (model.smoothMode == SmoothMode.BySum)
                        minProductionProbability = minProductionProbability1 * minProductionProbability2;

                    String substring2 = word2.Substring(0, j);
                    //Pair<Triple<String, String, String>, String> production = new Pair<Triple<String, String, String>, String>(new Triple<String, String, String>(leftProductionContext, substring1, rightProductionContext), substring2);

                    double prob;
                    if (model.productionProbs.Count == 0)
                        prob = 1;
                    else
                    {
                        prob = 0;
                        for (int k = model.productionContextSize; k >= 0; k--)
                            if (model.productionProbs.TryGetValue(new Pair<>(new Triple<>(leftContexts[k], substring1, rightContexts[k]), substring2), out prob))
                                break;

                        prob = Math.max(prob, minProductionProbability);
                    }

                    Pair<Double, Double> remainder = GetProbability(position + i, originalWord1, word1.substring(i), word2.substring(j), model, memoizationTable);

                    //record this remainder in our results
                    result.x += remainder.x * prob * segProb;
                    result.y += remainder.y * segProb;
                }
            }
        }

        memoizationTable[new Triple<>(position, word1, word2)] = result;
        return result;
    }
    


    class CSPExampleCounts
    {
        public SparseDoubleVector<Pair<Triple<String, String, String>, String>> productionCounts;
        public SparseDoubleVector<Triple<String, String, String>> segCounts;
    }

}



