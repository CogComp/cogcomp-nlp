package edu.illinois.cs.cogcomp.transliteration;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;

class CSPTransliteration
{

    public static SparseDoubleVector<Pair<Triple<String, String, String>, String>> CreateFallback(SparseDoubleVector<Pair<Triple<String, String, String>, String>> counts, InternDictionary<String> internTable)
    {
        SparseDoubleVector<Pair<Triple<String, String, String>, String>> result = new SparseDoubleVector<Pair<Triple<String, String, String>, String>>();
        for (KeyValuePair<Pair<Triple<String, String, String>, String>, Double> pair : counts)
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
        for (KeyValuePair<Triple<String, String, String>, Double> pair : segCounts)
        {
            for (int i = 0; i <= pair.Key.x.Length; i++)
            {
                result[
                        new Triple<String, String, String>(
                                internTable.Intern(pair.Key.x.Substring(i)),
                                internTable.Intern(pair.Key.y),
                                internTable.Intern(pair.Key.z.Substring(0, pair.Key.z.Length - i)))] += pair.Value;
            }
        }

        return result;
    }

    public static CSPModel LearnModel(List<Triple<String, String, Double>> examples, CSPModel model)
    {
        CSPExampleCounts counts = new CSPExampleCounts();
        //CSPExampleCounts intCounts = new CSPExampleCounts();

        counts.productionCounts = new SparseDoubleVector<Pair<Triple<String, String, String>, String>>();
        counts.segCounts = new SparseDoubleVector<Triple<String, String, String>>();

        for (Triple<String, String, Double> example : examples)
        {
            CSPExampleCounts exampleCount = LearnModel(example.x, example.y, model);
            counts.productionCounts.Add(example.z, exampleCount.productionCounts);
            counts.segCounts.Add(example.z,exampleCount.segCounts);

            //intCounts.productionCounts += exampleCount.productionCounts.Sign();
            //intCounts.segCounts += exampleCount.segCounts.Sign();
        }

        CSPModel result = (CSPModel)model.Clone();

        //normalize to get "joint"
        result.productionProbs = counts.productionCounts / counts.productionCounts.PNorm(1);

        InternDictionary<String> internTable = new InternDictionary<String>();

        SparseDoubleVector<Pair<Triple<String, String, String>, String>> oldProbs=null;
        if (model.underflowChecking)
            oldProbs = result.productionProbs;

        //now get producton fallbacks
        result.productionProbs = CreateFallback(result.productionProbs, internTable);

        //finally, make it conditional
        result.productionProbs = PSecondGivenFirst(result.productionProbs);

        if (model.underflowChecking)
        {
            //go through and ensure that Sum_X(P(Y|X)) == 1...or at least > 0!
            SparseDoubleVector<Triple<String, String, String>> sums = new SparseDoubleVector<Triple<String, String, String>>();
            for (KeyValuePair<Pair<Triple<String, String, String>, String>, Double> pair : model.productionProbs)
                sums[pair.Key.x] += pair.Value;

            List<Pair<Triple<String, String, String>, String>> restoreList = new List<Pair<Triple<String,String,String>,String>>();

            for (KeyValuePair<Pair<Triple<String, String, String>, String>, Double> pair : model.productionProbs)
                if (pair.Value == 0 && sums[pair.Key.x] == 0)
                    restoreList.Add(pair.Key);


            for (Pair<Triple<String, String, String>, String> pair : restoreList)
                model.productionProbs[pair] = oldProbs[pair];

        }

        //get conditional segmentation probs
        if (model.segMode == SegMode.Count)
            result.segProbs = CreateFallback(PSegGivenFlatOccurence(counts.segCounts,examples,model.segContextSize),internTable); // counts.segCounts / counts.segCounts.PNorm(1);
        else if (model.segMode == SegMode.Entropy)
        {
            SparseDoubleVector<Triple<String, String, String>> totals = new SparseDoubleVector<Triple<String, String, String>>();
            for (KeyValuePair<Pair<Triple<String, String, String>, String>, Double> pair : result.productionProbs)
            {
                //totals[pair.Key.x] -= pair.Value * Math.Log(pair.Value, 2);
                double logValue = pair.Value * Math.log(pair.Value);

                if (!double.IsNaN(logValue))
                    totals[pair.Key.x] += logValue;
                //totals[pair.Key.x] *= Math.Pow(pair.Value, pair.Value);
            }

            result.segProbs = totals.Exp(); //totals.Max(0.000001).Pow(-1);
        }

        //return the finished model
        return result;
    }

    private static Triple<String, String, String> GetContextTriple(String originalWord, int index, int length, int contextSize)
    {
        return new Triple<String, String, String>(WikiTransliteration.GetLeftContext(originalWord, index, contextSize),
                originalWord.Substring(index, length),
                WikiTransliteration.GetRightContext(originalWord, index + length, contextSize));
    }

    public static SparseDoubleVector<Triple<String, String, String>> PSegGivenFlatOccurence(SparseDoubleVector<Triple<String, String, String>> segCounts, List<Triple<String, String, double>> examples, int contextSize)
    {
        SparseDoubleVector<Triple<String, String, String>> counts = new SparseDoubleVector<Triple<String, String, String>>();
        for (Triple<String, String, Double> example : examples)
        {
            String word = new String('_', contextSize) + example.x + new String('_', contextSize);
            for (int i = contextSize; i < word.Length - contextSize; i++)
                for (int j = i; j < word.Length - contextSize; j++)
                    counts[GetContextTriple(word, i, j - i + 1, contextSize)] += example.z;
        }

        return segCounts / counts;
    }

    public static SparseDoubleVector<Triple<String, String, String>> PSegGivenLength(SparseDoubleVector<Triple<String, String, String>> segCounts)
    {
        SparseDoubleVector<int> sums = new SparseDoubleVector<int>();
        for (KeyValuePair<Triple<String, String, String>, double> pair : segCounts)
        sums[pair.Key.y.Length] += pair.Value;

        SparseDoubleVector<Triple<String, String, String>> result = new SparseDoubleVector<Triple<String,String,String>>(segCounts.Count);
        for (KeyValuePair<Triple<String, String, String>, double> pair : segCounts)
        result[pair.Key] = pair.Value / sums[pair.Key.y.Length];

        return result;
    }

    public static SparseDoubleVector<Pair<Triple<String, String, String>, String>> PSecondGivenFirst(SparseDoubleVector<Pair<Triple<String, String, String>, String>> counts)
    {
        SparseDoubleVector<Triple<String, String, String>> totals = new SparseDoubleVector<Triple<String, String, String>>();
        for (KeyValuePair<Pair<Triple<String, String, String>, String>, Double> pair : counts)
        totals[pair.Key.x] += pair.Value;

        SparseDoubleVector<Pair<Triple<String, String, String>, String>> result = new SparseDoubleVector<Pair<Triple<String, String, String>, String>>(counts.Count);
        for (KeyValuePair<Pair<Triple<String, String, String>, String>, Double> pair : counts)
        {
            double total = totals[pair.Key.x];
            result[pair.Key] = total == 0 ? 0 : pair.Value / total;
        }

        return result;
    }

    public static CSPExampleCounts LearnModel(String word1, String word2, CSPModel model)
    {
        CSPExampleCounts result;

        int paddingSize = Math.max(model.productionContextSize, model.segContextSize);
        String paddedWord = new String('_', paddingSize) + word1 + new String('_', paddingSize);
        Pair<SparseDoubleVector<Triple<Integer, String, String>>, Double> raw = LearnModel(paddingSize, paddedWord, word1, word2, model, new Dictionary<Triple<int,String,String>,Pair<SparseDoubleVector<Triple<int,String,String>>,double>>());

        if (raw.y == 0)
            raw.x = new SparseDoubleVector<Triple<Integer, String, String>>();
        else
            //raw.x = Program.segSums[Math.Min(39,word1.Length-1)][Math.Min(39,word2.Length-1)] * (raw.x);
            raw.x /= raw.y;
        //raw.x = raw.y >= 1 ? raw.x : raw.x / raw.y;

        if (model.emMode == EMMode.MaxSourceSeg)
        {
            Dictionary<Pair<Integer, String>, Triple<Integer, String, String>> bestProdProbs = new Dictionary<Pair<Integer, String>, Triple<Integer, String, String>>();
            SparseDoubleVector<Pair<Integer, String>> maxProdProbs = new SparseDoubleVector<Pair<Integer, String>>();
            for (KeyValuePair<Triple<Integer, String, String>, Double> pair : raw.x) {
                if (maxProdProbs[pair.Key.XY] < pair.Value) {
                    bestProdProbs[pair.Key.XY] = pair.Key;
                    maxProdProbs[pair.Key.XY] = pair.Value;
                }
            }

            raw.x.Clear();
            for (Triple<Integer, String, String> triple : bestProdProbs.Values)
            raw.x[triple] = 1;

        }
        else if (model.emMode == EMMode.BySourceSeg)
        {
            //Dictionary<Pair<int, String>, Triple<int, String, String>> bestProdProbs = new Dictionary<Pair<int, String>, Triple<int, String, String>>();
            SparseDoubleVector<Pair<Integer, String>> sumProdProbs = new SparseDoubleVector<Pair<Integer, String>>();
            for (KeyValuePair<Triple<Integer, String, String>, Double> pair : raw.x)
            sumProdProbs[pair.Key.XY] += pair.Value;

            SparseDoubleVector<Triple<Integer, String, String>> newCounts = new SparseDoubleVector<Triple<Integer, String, String>>(raw.x.Count);
            for (KeyValuePair<Triple<Integer, String, String>, Double> pair : raw.x)
            newCounts[pair.Key] = pair.Value / sumProdProbs[pair.Key.XY];

            raw.x = newCounts;
        }

        result.productionCounts = new SparseDoubleVector<Pair<Triple<String, String, String>, String>>(raw.x.Count);
        result.segCounts = new SparseDoubleVector<Triple<String, String, String>>(raw.x.Count);

        for (KeyValuePair<Triple<int, String, String>, double> pair in raw.x)
        {
            result.productionCounts[new Pair<Triple<String, String, String>, String>(
                    new Triple<String, String, String>(WikiTransliteration.GetLeftContext(paddedWord, pair.Key.x, model.productionContextSize), pair.Key.y, WikiTransliteration.GetRightContext(paddedWord, pair.Key.x + pair.Key.y.Length, model.productionContextSize))
                    , pair.Key.z)] += pair.Value;

            result.segCounts[new Triple<String, String, String>(WikiTransliteration.GetLeftContext(paddedWord, pair.Key.x, model.segContextSize), pair.Key.y, WikiTransliteration.GetRightContext(paddedWord, pair.Key.x + pair.Key.y.Length, model.segContextSize))]
                    += pair.Value;
        }

        return result;
    }

    public static char[] vowels = new char[] { 'a', 'e', 'i', 'o', 'u', 'y' };

    //Gets counts for productions by (conceptually) summing over all the possible alignments
    //and weighing each alignment (and its constituent productions) by the given probability table.
    //probSum is important (and memoized for input word pairs)--it keeps track and returns the sum of the probabilities of all possible alignments for the word pair
    public static Pair<SparseDoubleVector<Triple<Integer, String, String>>, Double> LearnModel(int position, String originalWord1, String word1, String word2, CSPModel model, Dictionary<Triple<int, String, String>, Pair<SparseDoubleVector<Triple<int, String, String>>, double>> memoizationTable)
    {
        Pair<SparseDoubleVector<Triple<Integer, String, String>>, Double> memoization;
        if (memoizationTable.TryGetValue(new Triple<Integer, String, String>(position, word1, word2), out memoization))
            return memoization; //we've been down this road before

        Pair<SparseDoubleVector<Triple<Integer, String, String>>, Double> result
                = new Pair<SparseDoubleVector<Triple<Integer, String, String>>, Double>(new SparseDoubleVector<Triple<Integer,String,String>>(),0);

        if (word1.Length == 0 && word2.Length == 0) //record probabilities
        {
            result.y = 1; //null -> null is always a perfect alignment
            return result; //end of the line
        }

        int maxSubstringLength1f = Math.Min(word1.Length, model.maxSubstringLength);
        int maxSubstringLength2f = Math.Min(word2.Length, model.maxSubstringLength);

        String[] leftContexts = WikiTransliteration.GetLeftFallbackContexts(originalWord1,position, Math.max(model.segContextSize, model.productionContextSize));

        int firstVowel = -1; int secondVowel = -1;
        if (model.syllabic)
        {
            for (int i = 0; i < word1.Length; i++)
                if (Array.IndexOf<char>(vowels, word1[i]) >= 0)
            firstVowel = i;
            else if (firstVowel >= 0)
            break;

            if (firstVowel == -1)
                firstVowel = word1.Length - 1; //no vowels!

            for (int i = firstVowel + 1; i < word1.Length; i++)
                if (Array.IndexOf<char>(vowels, word1[i]) >= 0)
            {
                secondVowel = i;
                break;
            }

            if (secondVowel == -1 || (secondVowel == word1.Length - 1 && word1[secondVowel] == 'e')) //if only one vowel, only consider the entire thing; note consideration of silent 'e' at end of words
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
            String[] rightContexts = WikiTransliteration.GetRightFallbackContexts(originalWord1, position + i, Math.Max(model.segContextSize, model.productionContextSize));

            double segProb;
            if (model.segProbs.Count == 0)
                segProb = 1;
            else
            {
                segProb = 0;
                int minK = model.fallbackStrategy == FallbackStrategy.NotDuringTraining ? model.segContextSize : 0;
                for (int k = model.segContextSize; k >= minK; k--)
                    if (model.segProbs.TryGetValue(new Triple<String, String, String>(leftContexts[k], substring1, rightContexts[k]), out segProb))
                        break;
            }

            for (int j = 1; j <= maxSubstringLength2f; j++) //foreach possible substring in the second
            {
                if ((word1.Length - i) * model.maxSubstringLength >= word2.Length - j && (word2.Length - j) * model.maxSubstringLength >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
                {
                    String substring2 = word2.substring(0, j);
                    //Pair<Triple<String, String, String>, String> production = new Pair<Triple<String, String, String>, String>(new Triple<String, String, String>(leftProductionContext, substring1, rightProductionContext), substring2);

                    double prob;
                    if (model.productionProbs.Count == 0)
                        prob = 1;
                    else
                    {
                        prob = 0;
                        int minK = model.fallbackStrategy == FallbackStrategy.NotDuringTraining ? model.productionContextSize : 0;
                        for (int k = model.productionContextSize; k >= minK; k--)
                            if (model.productionProbs.TryGetValue(new Pair<Triple<String, String, String>, String>(new Triple<String, String, String>(leftContexts[k], substring1, rightContexts[k]), substring2), out prob))
                                break;

                        if (model.emMode == EMMode.Smoothed) prob = Math.Max(model.minProductionProbability, prob);
                    }

                    Pair<SparseDoubleVector<Triple<Integer, String, String>>, Double> remainder = LearnModel(position + i, originalWord1, word1.substring(i), word2.substring(j), model, memoizationTable);

                    double cProb = prob * segProb;

                    //record this production in our results

                    result.x.Add(cProb, remainder.x);
                    result.y += remainder.y * cProb;

                    result.x[new Triple<Integer, String, String>(position, substring1, substring2)] += cProb * remainder.y;
                }
            }
        }

        memoizationTable[new Triple<Integer, String, String>(position, word1, word2)] = result;
        return result;
    }






























    public static double GetProbability(String word1, String word2, CSPModel model)
    {
        int paddingSize = Math.Max(model.productionContextSize, model.segContextSize);
        String paddedWord = new String('_', paddingSize) + word1 + new String('_', paddingSize);

        if (model.segMode != SegMode.Best)
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

        if (word1.Length == 0 && word2.Length == 0)
            return 1; //perfect null-to-null alignment


        int maxSubstringLength1f = Math.Min(word1.Length, model.maxSubstringLength);
        int maxSubstringLength2f = Math.Min(word2.Length, model.maxSubstringLength);

        String[] leftContexts = WikiTransliteration.GetLeftFallbackContexts(originalWord1, position, Math.Max(model.segContextSize, model.productionContextSize));

        double minProductionProbability1 = 1;

        for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
        {
            minProductionProbability1 *= model.minProductionProbability;

            String substring1 = word1.Substring(0, i);
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

                if ((word1.Length - i) * model.maxSubstringLength >= word2.Length - j && (word2.Length - j) * model.maxSubstringLength >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
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

        if (word1.Length == 0 && word2.Length == 0) //record probabilities
        {
            result.x = 1; //null -> null is always a perfect alignment
            result.y = 1;
            return result; //end of the line
        }

        int maxSubstringLength1f = Math.min(word1.Length, model.maxSubstringLength);
        int maxSubstringLength2f = Math.min(word2.Length, model.maxSubstringLength);

        String[] leftContexts = WikiTransliteration.GetLeftFallbackContexts(originalWord1, position, Math.Max(model.segContextSize, model.productionContextSize));

        double minProductionProbability1 = 1;

        for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
        {
            minProductionProbability1 *= model.minProductionProbability;

            String substring1 = word1.Substring(0, i);
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

                if ((word1.Length - i) * model.maxSubstringLength >= word2.Length - j && (word2.Length - j) * model.maxSubstringLength >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
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

                    Pair<double, double> remainder = GetProbability(position + i, originalWord1, word1.Substring(i), word2.Substring(j), model, memoizationTable);

                    //record this remainder in our results
                    result.x += remainder.x * prob * segProb;
                    result.y += remainder.y * segProb;
                }
            }
        }

        memoizationTable[new Triple<int, String, String>(position, word1, word2)] = result;
        return result;
    }
    
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

        public CSPModel() { }

        /// <summary>
        ///
        /// </summary>
        /// <param name="maxSubstringLength"></param>
        /// <param name="segContextSize"></param>
        /// <param name="productionContextSize"></param>
        /// <param name="minProductionProbability"></param>
        /// <param name="segMode"></param>
        /// <param name="syllabic"></param>
        /// <param name="smoothMode"></param>
        /// <param name="fallbackStrategy"></param>
        /// <param name="emMode"></param>
        /// <param name="underflowChecking">True to check for \sum_t P(t|s) == 0 after normalizing production counts, where t and s are segments of the target and source word, respectively.  If such (total) underflow occurs, the previous iteration's conditional probabilities are used instead.</param>
        public CSPModel(int maxSubstringLength, int segContextSize, int productionContextSize, double minProductionProbability, SegMode segMode, bool syllabic, SmoothMode smoothMode, FallbackStrategy fallbackStrategy, EMMode emMode, bool underflowChecking)
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

        public Object Clone()
        {
            return this.MemberwiseClone();
        }


        @Override
        public double GetProbability(String word1, String word2)
        {
            return CSPTransliteration.GetProbability(word1, word2, this);
        }

        @Override
        public  TransliterationModel LearnModel(List<Triple<String, String, double>> examples)
        {
            return CSPTransliteration.LearnModel(examples, this);
        }

    }

    class CSPExampleCounts
    {
        public SparseDoubleVector<Pair<Triple<String, String, String>, String>> productionCounts;
        public SparseDoubleVector<Triple<String, String, String>> segCounts;
    }

}



