//using System;
//using System.Collections.Generic;
//using System.Text;
//using Pasternack.Collections.Generic;
//using Pasternack;
//using Pasternack.Utility;
//using Pasternack.Collections.Generic.Specialized;

//namespace WikiTransliteration
//{

//    public class MultiSegModel : TransliterationModel, ICloneable
//    {

//        public MultiSegModel() { }
//        public MultiSegModel(int maxSubstringLength, int segContextSize, int productionContextSize, double minProductionProbability, SegMode segMode, SmoothMode smoothMode, FallbackStrategy fallbackStrategy, EMMode emMode)
//        {
//            this.maxSubstringLength = maxSubstringLength;
//            this.segContextSize = segContextSize;
//            this.productionContextSize = productionContextSize;
//            this.minProductionProbability = minProductionProbability;
//            this.fallbackStrategy = fallbackStrategy;
//            //this.updateSegProbs = updateSegProbs;
//            this.segMode = segMode;
//            this.smoothMode = smoothMode;
//            this.emMode = emMode;
//        }

//        public EMMode emMode;
//        public SegMode segMode;
//        public SmoothMode smoothMode;

//        public SparseDoubleVector<Pair<Triple<string, string, string>, string>> productionProbs;
//        public SparseDoubleVector<Triple<string, string,string>> segProbs;

//        //public SparseDoubleVector<Pair<Triple<string, string, string>, string>> productionCounts;
//        //public SparseDoubleVector<Triple<string, string, string>> segCounts;

//        public int segContextSize;
//        public int productionContextSize;
//        public int maxSubstringLength;

//        public double minProductionProbability;

//        public FallbackStrategy fallbackStrategy;

//        #region ICloneable Members

//        public object Clone()
//        {
//            return this.MemberwiseClone();
//        }

//        #endregion

//        #region ITransliterationModel Members

//        public override double GetProbability(string word1, string word2)
//        {
//            return CSPTransliteration.GetProbability(word1, word2, this);
//        }

//        public override TransliterationModel LearnModel(List<Triple<string, string, double>> examples)
//        {
//            return MultiSegTransliteration.LearnModel(examples, this);
//        }

//        #endregion
//    }

//    public struct CSPExampleCounts
//    {
//        public SparseDoubleVector<Pair<Triple<string, string, string>, string>> productionCounts;
//        public SparseDoubleVector<Triple<string, string, string>> segCounts;
//    }

//    public static class MultiSegTransliteration
//    {

//        public static SparseDoubleVector<Pair<Triple<string, string, string>, string>> CreateFallback(SparseDoubleVector<Pair<Triple<string, string, string>, string>> counts, InternDictionary<string> internTable)
//        {
//            SparseDoubleVector<Pair<Triple<string, string, string>, string>> result = new SparseDoubleVector<Pair<Triple<string, string, string>, string>>();
//            foreach (KeyValuePair<Pair<Triple<string, string, string>, string>, double> pair in counts)
//            {
//                for (int i = 0; i <= pair.Key.x.x.Length; i++)
//                {
//                    result[new Pair<Triple<string, string, string>, string>(
//                        new Triple<string, string, string>(
//                            internTable.Intern(pair.Key.x.x.Substring(i)),
//                            internTable.Intern(pair.Key.x.y),
//                            internTable.Intern(pair.Key.x.z.Substring(0, pair.Key.x.z.Length - i))), pair.Key.y)] += pair.Value;
//                }
//            }

//            return result;
//        }

//        public static SparseDoubleVector<Triple<string, string, string>> CreateFallback(SparseDoubleVector<Triple<string, string, string>> segCounts, InternDictionary<string> internTable)
//        {
//            SparseDoubleVector<Triple<string, string, string>> result = new SparseDoubleVector<Triple<string, string, string>>();
//            foreach (KeyValuePair<Triple<string, string, string>, double> pair in segCounts)
//            {
//                for (int i = 0; i <= pair.Key.x.Length; i++)
//                {
//                    result[
//                        new Triple<string, string, string>(
//                            internTable.Intern(pair.Key.x.Substring(i)),
//                            internTable.Intern(pair.Key.y),
//                            internTable.Intern(pair.Key.z.Substring(0, pair.Key.z.Length - i)))] += pair.Value;
//                }
//            }

//            return result;
//        }

//        public static CSPModel LearnModel(List<Triple<string, string, double>> examples, CSPModel model)
//        {
//            CSPExampleCounts counts = new CSPExampleCounts();
//            //CSPExampleCounts intCounts = new CSPExampleCounts();

//            counts.productionCounts = new SparseDoubleVector<Pair<Triple<string, string, string>, string>>();
//            counts.segCounts = new SparseDoubleVector<Triple<string, string, string>>();

//            foreach (Triple<string, string, double> example in examples)
//            {
//                CSPExampleCounts exampleCount = LearnModel(example.x, example.y, model);
//                counts.productionCounts.Add(example.z, exampleCount.productionCounts);
//                counts.segCounts.Add(example.z,exampleCount.segCounts);
                
//                //intCounts.productionCounts += exampleCount.productionCounts.Sign();
//                //intCounts.segCounts += exampleCount.segCounts.Sign();
//            }

//            CSPModel result = (CSPModel)model.Clone();

//            //normalize to get "joint"
//            result.productionProbs = counts.productionCounts / counts.productionCounts.PNorm(1);

//            InternDictionary<string> internTable = new InternDictionary<string>();

//            //now get producton fallbacks            
//            result.productionProbs = CreateFallback(result.productionProbs, internTable);

//            //finally, make it conditional
//            result.productionProbs = PSecondGivenFirst(result.productionProbs);

//            //get conditional segmentation probs
//            if (model.segMode == SegMode.Count)
//                result.segProbs = CreateFallback(PSegGivenFlatOccurence(counts.segCounts,examples,model.segContextSize),internTable); // counts.segCounts / counts.segCounts.PNorm(1);
//            else if (model.segMode == SegMode.Entropy)
//            {
//                SparseDoubleVector<Triple<string, string, string>> totals = new SparseDoubleVector<Triple<string, string, string>>();
//                foreach (KeyValuePair<Pair<Triple<string, string, string>, string>, double> pair in result.productionProbs)
//                {
//                    //totals[pair.Key.x] -= pair.Value * Math.Log(pair.Value, 2);
//                    double logValue = pair.Value * Math.Log(pair.Value);

//                    if (!double.IsNaN(logValue))
//                        totals[pair.Key.x] += logValue;
//                    //totals[pair.Key.x] *= Math.Pow(pair.Value, pair.Value);
//                }

//                result.segProbs = totals.Exp(); //totals.Max(0.000001).Pow(-1);
//            }

//            //return the finished model
//            return result;
//        }

//        private static Triple<string, string, string> GetContextTriple(string originalWord, int index, int length, int contextSize)
//        {
//            return new Triple<string, string, string>(WikiTransliteration.GetLeftContext(originalWord, index, contextSize),
//                originalWord.Substring(index, length),
//                WikiTransliteration.GetRightContext(originalWord, index + length, contextSize));
//        }

//        public static SparseDoubleVector<Triple<string, string, string>> PSegGivenFlatOccurence(SparseDoubleVector<Triple<string, string, string>> segCounts, List<Triple<string, string, double>> examples, int contextSize)
//        {
//            SparseDoubleVector<Triple<string, string, string>> counts = new SparseDoubleVector<Triple<string, string, string>>();
//            foreach (Triple<string, string, double> example in examples)
//            {
//                string word = new string('_', contextSize) + example.x + new string('_', contextSize);
//                for (int i = contextSize; i < word.Length - contextSize; i++)
//                    for (int j = i; j < word.Length - contextSize; j++)
//                        counts[GetContextTriple(word, i, j - i + 1, contextSize)] += example.z;
//            }

//            return segCounts / counts;
//        }

//        public static SparseDoubleVector<Triple<string, string, string>> PSegGivenLength(SparseDoubleVector<Triple<string, string, string>> segCounts)
//        {
//            SparseDoubleVector<int> sums = new SparseDoubleVector<int>();
//            foreach (KeyValuePair<Triple<string, string, string>, double> pair in segCounts)
//                sums[pair.Key.y.Length] += pair.Value;

//            SparseDoubleVector<Triple<string, string, string>> result = new SparseDoubleVector<Triple<string,string,string>>(segCounts.Count);
//            foreach (KeyValuePair<Triple<string, string, string>, double> pair in segCounts)
//                result[pair.Key] = pair.Value / sums[pair.Key.y.Length];

//            return result;
//        }

//        public static SparseDoubleVector<Pair<Triple<string, string, string>, string>> PSecondGivenFirst(SparseDoubleVector<Pair<Triple<string, string, string>, string>> counts)
//        {
//            SparseDoubleVector<Triple<string, string, string>> totals = new SparseDoubleVector<Triple<string, string, string>>();
//            foreach (KeyValuePair<Pair<Triple<string, string, string>, string>, double> pair in counts)
//                totals[pair.Key.x] += pair.Value;

//            SparseDoubleVector<Pair<Triple<string, string, string>, string>> result = new SparseDoubleVector<Pair<Triple<string, string, string>, string>>(counts.Count);
//            foreach (KeyValuePair<Pair<Triple<string, string, string>, string>, double> pair in counts)
//            {
//                double total = totals[pair.Key.x];
//                result[pair.Key] = total == 0 ? 0 : pair.Value / total;
//            }

//            return result;
//        }

//        public static CSPExampleCounts LearnModel(string word1, string word2, CSPModel model)
//        {
//            CSPExampleCounts result;

//            int paddingSize = Math.Max(model.productionContextSize, model.segContextSize);
//            string paddedWord = new string('_', paddingSize) + word1 + new string('_', paddingSize);
//            Pair<SparseDoubleVector<Triple<int, string, string>>, double> raw = LearnModel(paddingSize, paddedWord, word1, word2, model, new Dictionary<Triple<int,string,string>,Pair<SparseDoubleVector<Triple<int,string,string>>,double>>());

//            if (raw.y == 0)
//                raw.x = new SparseDoubleVector<Triple<int, string, string>>();
//            else
//                //raw.x = Program.segSums[Math.Min(39,word1.Length-1)][Math.Min(39,word2.Length-1)] * (raw.x);
//                raw.x /= raw.y;
//                //raw.x = raw.y >= 1 ? raw.x : raw.x / raw.y;

//            if (model.emMode == EMMode.MaxSourceSeg)
//            {
//                Dictionary<Pair<int, string>, Triple<int, string, string>> bestProdProbs = new Dictionary<Pair<int, string>, Triple<int, string, string>>();
//                SparseDoubleVector<Pair<int, string>> maxProdProbs = new SparseDoubleVector<Pair<int, string>>();
//                foreach (KeyValuePair<Triple<int, string, string>, double> pair in raw.x)
//                    if (maxProdProbs[pair.Key.XY] < pair.Value)
//                    {
//                        bestProdProbs[pair.Key.XY] = pair.Key;
//                        maxProdProbs[pair.Key.XY] = pair.Value;
//                    }

//                raw.x.Clear();
//                foreach (Triple<int, string, string> triple in bestProdProbs.Values)
//                    raw.x[triple] = 1;

//            }

//            result.productionCounts = new SparseDoubleVector<Pair<Triple<string, string, string>, string>>(raw.x.Count);
//            result.segCounts = new SparseDoubleVector<Triple<string, string, string>>(raw.x.Count);

//            foreach (KeyValuePair<Triple<int, string, string>, double> pair in raw.x)
//            {
//                result.productionCounts[new Pair<Triple<string, string, string>, string>(
//                            new Triple<string, string, string>(WikiTransliteration.GetLeftContext(paddedWord, pair.Key.x, model.productionContextSize), pair.Key.y, WikiTransliteration.GetRightContext(paddedWord, pair.Key.x + pair.Key.y.Length, model.productionContextSize))
//                            , pair.Key.z)] += pair.Value;

//                result.segCounts[new Triple<string, string, string>(WikiTransliteration.GetLeftContext(paddedWord, pair.Key.x, model.segContextSize), pair.Key.y, WikiTransliteration.GetRightContext(paddedWord, pair.Key.x + pair.Key.y.Length, model.segContextSize))]
//                             += pair.Value;
//            }                       

//            return result;
//        }

//        public static char[] vowels = new char[] { 'a', 'e', 'i', 'o', 'u', 'y' };

//        //Gets counts for productions by (conceptually) summing over all the possible alignments
//        //and weighing each alignment (and its constituent productions) by the given probability table.
//        //probSum is important (and memoized for input word pairs)--it keeps track and returns the sum of the probabilities of all possible alignments for the word pair
//        public static Pair<SparseDoubleVector<Triple<int, string, string>>, double> LearnModel(int position, string originalWord1, string word1, string word2, CSPModel model, Dictionary<Triple<int, string, string>, Pair<SparseDoubleVector<Triple<int, string, string>>, double>> memoizationTable)
//        {
//            Pair<SparseDoubleVector<Triple<int, string, string>>, double> memoization;
//            if (memoizationTable.TryGetValue(new Triple<int, string, string>(position, word1, word2), out memoization))
//                return memoization; //we've been down this road before            

//            Pair<SparseDoubleVector<Triple<int, string, string>>, double> result
//                = new Pair<SparseDoubleVector<Triple<int, string, string>>, double>(new SparseDoubleVector<Triple<int,string,string>>(),0);

//            if (word1.Length == 0 && word2.Length == 0) //record probabilities
//            {
//                result.y = 1; //null -> null is always a perfect alignment
//                return result; //end of the line            
//            }

//            int maxSubstringLength1f = Math.Min(word1.Length, model.maxSubstringLength);
//            int maxSubstringLength2f = Math.Min(word2.Length, model.maxSubstringLength);

//            string[] leftContexts = WikiTransliteration.GetLeftFallbackContexts(originalWord1,position, Math.Max(model.segContextSize, model.productionContextSize));

//            int firstVowel = -1; int secondVowel = -1;
//            if (model.syllabic)
//            {
//                for (int i = 0; i < word1.Length; i++)
//                    if (Array.IndexOf<char>(vowels, word1[i]) >= 0)
//                        firstVowel = i;
//                    else if (firstVowel >= 0)
//                        break;

//                if (firstVowel == -1)
//                    firstVowel = word1.Length - 1; //no vowels!

//                for (int i = firstVowel + 1; i < word1.Length; i++)
//                    if (Array.IndexOf<char>(vowels, word1[i]) >= 0)
//                    {
//                        secondVowel = i;
//                        break;
//                    }

//                if (secondVowel == -1 || (secondVowel == word1.Length - 1 && word1[secondVowel] == 'e')) //if only one vowel, only consider the entire thing; note consideration of silent 'e' at end of words
//                {
//                    firstVowel = maxSubstringLength1f - 1;
//                    secondVowel = maxSubstringLength1f;
//                }
//            }
//            else
//            {
//                firstVowel = 0;
//                secondVowel = maxSubstringLength1f;
//            }

//            for (int i = firstVowel + 1; i <= secondVowel; i++)
//            //for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
//            {                
//                string substring1 = word1.Substring(0, i);
//                string[] rightContexts = WikiTransliteration.GetRightFallbackContexts(originalWord1, position + i, Math.Max(model.segContextSize, model.productionContextSize));

//                double segProb;
//                if (model.segProbs.Count == 0)
//                    segProb = 1;
//                else
//                {
//                    segProb = 0;
//                    int minK = model.fallbackStrategy == FallbackStrategy.NotDuringTraining ? model.segContextSize : 0;
//                    for (int k = model.segContextSize; k >= minK; k--)
//                        if (model.segProbs.TryGetValue(new Triple<string, string, string>(leftContexts[k], substring1, rightContexts[k]), out segProb))
//                            break;
//                }

//                for (int j = 1; j <= maxSubstringLength2f; j++) //foreach possible substring in the second
//                {
//                    if ((word1.Length - i) * model.maxSubstringLength >= word2.Length - j && (word2.Length - j) * model.maxSubstringLength >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
//                    {
//                        string substring2 = word2.Substring(0, j);
//                        //Pair<Triple<string, string, string>, string> production = new Pair<Triple<string, string, string>, string>(new Triple<string, string, string>(leftProductionContext, substring1, rightProductionContext), substring2);                        

//                        double prob;
//                        if (model.productionProbs.Count == 0)
//                            prob = 1;
//                        else
//                        {
//                            prob = 0;
//                            int minK = model.fallbackStrategy == FallbackStrategy.NotDuringTraining ? model.productionContextSize : 0;
//                            for (int k = model.productionContextSize; k >= minK; k--)
//                                if (model.productionProbs.TryGetValue(new Pair<Triple<string, string, string>, string>(new Triple<string, string, string>(leftContexts[k], substring1, rightContexts[k]), substring2), out prob))
//                                    break;

//                            if (model.emMode == EMMode.Smoothed) prob = Math.Max(model.minProductionProbability, prob);
//                        }

//                        Pair<SparseDoubleVector<Triple<int, string, string>>, double> remainder = LearnModel(position + i, originalWord1, word1.Substring(i), word2.Substring(j), model, memoizationTable);

//                        double cProb = prob * segProb;

//                        //record this production in our results

//                        result.x.Add(cProb, remainder.x);                        
//                        result.y += remainder.y * cProb;

//                        result.x[new Triple<int, string, string>(position, substring1, substring2)] += cProb * remainder.y;
//                    }
//                }
//            }

//            memoizationTable[new Triple<int, string, string>(position, word1, word2)] = result;
//            return result;
//        }






























//        public static double GetProbability(string word1, string word2, CSPModel model)
//        {
//            int paddingSize = Math.Max(model.productionContextSize, model.segContextSize);
//            string paddedWord = new string('_', paddingSize) + word1 + new string('_', paddingSize);

//            if (model.segMode != SegMode.Best)
//            {
//                Pair<double, double> raw = GetProbability(paddingSize, paddedWord, word1, word2, model, new Dictionary<Triple<int, string, string>, Pair<double, double>>());
//                return raw.x / raw.y; //normalize the segmentation probabilities by dividing by the sum of probabilities for all segmentations
//            }
//            else
//                return GetBestProbability(paddingSize, paddedWord, word1, word2, model, new Dictionary<Triple<int, string, string>, double>());
//        }

//        //Gets the "best" alignment for a given word pair, defined as max P(s,t|S,T).
//        public static double GetBestProbability(int position, string originalWord1, string word1, string word2, CSPModel model, Dictionary<Triple<int, string, string>, double> memoizationTable)
//        {
//            double result;
//            if (memoizationTable.TryGetValue(new Triple<int, string, string>(position, word1, word2), out result))
//                return result; //we've been down this road before            

//            result = 0;

//            if (word1.Length == 0 && word2.Length == 0)            
//                return 1; //perfect null-to-null alignment
            

//            int maxSubstringLength1f = Math.Min(word1.Length, model.maxSubstringLength);
//            int maxSubstringLength2f = Math.Min(word2.Length, model.maxSubstringLength);

//            string[] leftContexts = WikiTransliteration.GetLeftFallbackContexts(originalWord1, position, Math.Max(model.segContextSize, model.productionContextSize));

//            double minProductionProbability1 = 1;

//            for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
//            {
//                minProductionProbability1 *= model.minProductionProbability;

//                string substring1 = word1.Substring(0, i);
//                string[] rightContexts = WikiTransliteration.GetRightFallbackContexts(originalWord1, position + i, Math.Max(model.segContextSize, model.productionContextSize));

//                double segProb;
//                if (model.segProbs.Count == 0)
//                    segProb = 1;
//                else
//                {
//                    segProb = 0;
//                    for (int k = model.productionContextSize; k >= 0; k--)
//                        if (model.segProbs.TryGetValue(new Triple<string, string, string>(leftContexts[k], substring1, rightContexts[k]), out segProb))
//                            break;
//                }

//                double minProductionProbability2 = 1;
//                for (int j = 1; j <= maxSubstringLength2f; j++) //foreach possible substring in the second
//                {
//                    minProductionProbability2 *= model.minProductionProbability;

//                    if ((word1.Length - i) * model.maxSubstringLength >= word2.Length - j && (word2.Length - j) * model.maxSubstringLength >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
//                    {
//                        double minProductionProbability;
//                        if (model.smoothMode == SmoothMode.BySource)
//                            minProductionProbability = minProductionProbability1;
//                        else if (model.smoothMode == SmoothMode.ByMax)
//                            minProductionProbability = Math.Min(minProductionProbability1, minProductionProbability2);
//                        else //if (model.smoothMode == SmoothMode.BySum)
//                            minProductionProbability = minProductionProbability1 * minProductionProbability2;

//                        string substring2 = word2.Substring(0, j);
//                        //Pair<Triple<string, string, string>, string> production = new Pair<Triple<string, string, string>, string>(new Triple<string, string, string>(leftProductionContext, substring1, rightProductionContext), substring2);                        

//                        double prob;
//                        if (model.productionProbs.Count == 0)
//                            prob = 1;
//                        else
//                        {
//                            prob = 0;
//                            for (int k = model.productionContextSize; k >= 0; k--)
//                                if (model.productionProbs.TryGetValue(new Pair<Triple<string, string, string>, string>(new Triple<string, string, string>(leftContexts[k], substring1, rightContexts[k]), substring2), out prob))
//                                    break;

//                            prob = Math.Max(prob, minProductionProbability);
//                        }

//                        double remainder = prob * GetBestProbability(position + i, originalWord1, word1.Substring(i), word2.Substring(j), model, memoizationTable);

//                        if (remainder > result) result = remainder; //maximize

//                        //record this remainder in our results
//                        //result.x += remainder.x * prob * segProb;
//                        //result.y += remainder.y * segProb;
//                    }
//                }
//            }

//            memoizationTable[new Triple<int, string, string>(position, word1, word2)] = result;
//            return result;
//        }

//        //Gets counts for productions by (conceptually) summing over all the possible alignments
//        //and weighing each alignment (and its constituent productions) by the given probability table.
//        //probSum is important (and memoized for input word pairs)--it keeps track and returns the sum of the probabilities of all possible alignments for the word pair
//        public static Pair<double, double> GetProbability(int position, string originalWord1, string word1, string word2, CSPModel model, Dictionary<Triple<int, string, string>, Pair<double, double>> memoizationTable)
//        {
//            Pair<double, double> result;
//            if (memoizationTable.TryGetValue(new Triple<int, string, string>(position, word1, word2), out result))
//                return result; //we've been down this road before            

//            result = new Pair<double, double>(0, 0);

//            if (word1.Length == 0 && word2.Length == 0) //record probabilities
//            {
//                result.x = 1; //null -> null is always a perfect alignment
//                result.y = 1;
//                return result; //end of the line            
//            }

//            int maxSubstringLength1f = Math.Min(word1.Length, model.maxSubstringLength);
//            int maxSubstringLength2f = Math.Min(word2.Length, model.maxSubstringLength);

//            string[] leftContexts = WikiTransliteration.GetLeftFallbackContexts(originalWord1, position, Math.Max(model.segContextSize, model.productionContextSize));

//            double minProductionProbability1 = 1;

//            for (int i = 1; i <= maxSubstringLength1f; i++) //for each possible substring in the first word...
//            {
//                minProductionProbability1 *= model.minProductionProbability;

//                string substring1 = word1.Substring(0, i);
//                string[] rightContexts = WikiTransliteration.GetRightFallbackContexts(originalWord1, position + i, Math.Max(model.segContextSize, model.productionContextSize));

//                double segProb;
//                if (model.segProbs.Count == 0)
//                    segProb = 1;
//                else
//                {
//                    segProb = 0;
//                    for (int k = model.productionContextSize; k >= 0; k--)
//                        if (model.segProbs.TryGetValue(new Triple<string, string, string>(leftContexts[k], substring1, rightContexts[k]), out segProb))
//                            break;
//                }

//                double minProductionProbability2 = 1;
//                for (int j = 1; j <= maxSubstringLength2f; j++) //foreach possible substring in the second
//                {
//                    minProductionProbability2 *= model.minProductionProbability;

//                    if ((word1.Length - i) * model.maxSubstringLength >= word2.Length - j && (word2.Length - j) * model.maxSubstringLength >= word1.Length - i) //if we get rid of these characters, can we still cover the remainder of word2?
//                    {
//                        double minProductionProbability;
//                        if (model.smoothMode == SmoothMode.BySource)
//                            minProductionProbability = minProductionProbability1;
//                        else if (model.smoothMode == SmoothMode.ByMax)
//                            minProductionProbability = Math.Min(minProductionProbability1, minProductionProbability2);
//                        else //if (model.smoothMode == SmoothMode.BySum)
//                            minProductionProbability = minProductionProbability1 * minProductionProbability2;

//                        string substring2 = word2.Substring(0, j);
//                        //Pair<Triple<string, string, string>, string> production = new Pair<Triple<string, string, string>, string>(new Triple<string, string, string>(leftProductionContext, substring1, rightProductionContext), substring2);                        

//                        double prob;
//                        if (model.productionProbs.Count == 0)
//                            prob = 1;
//                        else
//                        {
//                            prob = 0;
//                            for (int k = model.productionContextSize; k >= 0; k--)
//                                if (model.productionProbs.TryGetValue(new Pair<Triple<string, string, string>, string>(new Triple<string, string, string>(leftContexts[k], substring1, rightContexts[k]), substring2), out prob))
//                                    break;

//                            prob = Math.Max(prob, minProductionProbability);
//                        }

//                        Pair<double, double> remainder = GetProbability(position + i, originalWord1, word1.Substring(i), word2.Substring(j), model, memoizationTable);

//                        //record this remainder in our results
//                        result.x += remainder.x * prob * segProb;
//                        result.y += remainder.y * segProb;                        
//                    }
//                }
//            }

//            memoizationTable[new Triple<int, string, string>(position, word1, word2)] = result;
//            return result;
//        }


//    }
//}
