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
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.utils.Dictionaries;
import edu.illinois.cs.cogcomp.utils.InternDictionary;
import edu.illinois.cs.cogcomp.utils.SparseDoubleVector;
import net.sourceforge.pinyin4j.PinyinHelper;

import java.io.FileNotFoundException;
import java.text.Normalizer;
import java.util.*;

class Program {

    static void main(String[] args) throws FileNotFoundException {
        //RussianDiscoveryTest(); return;
        //ChineseDiscoveryTest(); return;
        HebrewDiscoveryTest();
    }

    public static String StripAccent(String stIn) {
        String strNFD = Normalizer.normalize(stIn, Normalizer.Form.NFD);
        StringBuilder sb = new StringBuilder();
        for (char ch : strNFD.toCharArray()) {
            if (Character.getType(ch) != Character.NON_SPACING_MARK) {
                sb.append(ch);
            }
        }
        return sb.toString();

    }


    public static void HebrewDiscoveryTest() throws FileNotFoundException {
        List<Pair<String, String>> wordList = NormalizeHebrew(GetTabDelimitedPairs("/path/to/res/res/Hebrew/evalwords.txt"));
        List<Pair<String, String>> trainList = NormalizeHebrew(GetTabDelimitedPairs("/path/to/res/res/Hebrew/train_EnglishHebrew.txt"));
        List<Pair<String, String>> trainList2 = NormalizeHebrew(GetTabDelimitedPairs("/path/to/WikiTransliteration/Aliases/heExamples.txt"));
        //List<Pair<String, String>> trainList2 = RemoveVeryLong(NormalizeHebrew(GetTabDelimitedPairs(@"C:\Data\WikiTransliteration\Aliases\heExamples-Lax.txt")), 20);

        List<Pair<String, String>> wordAndTrain = new ArrayList<>(wordList);
        wordAndTrain.addAll(trainList);

        HashMap<String, Boolean> usedExamples = new HashMap<>();
        for (Pair<String, String> pair : wordAndTrain) {
            usedExamples.put(pair.getFirst(), true);
        }
        //trainList2 = TruncateList(trainList2, 2000);
        for (Pair<String, String> pair : trainList2) {
            if (!usedExamples.containsKey(pair.getFirst()))
                trainList.add(pair);
        }

        //DiscoveryTestDual(RemoveDuplicates(GetListValues(wordList)), trainList, LiftPairList(wordList), 15, 15);
        //TestXMLData(trainList, wordList, 15, 15);


        List<String> candidateList = GetListValues(wordList);
        //wordList = GetRandomPartOfList(trainList, 50, 31);
        candidateList.addAll(GetListValues(wordList));

        DiscoveryEM(200, RemoveDuplicates(candidateList), trainList, LiftPairList(wordList), new CSPModel(40, 0, 0, 0.000000000000001, CSPModel.SegMode.None, false, CSPModel.SmoothMode.BySum, FallbackStrategy.NotDuringTraining, CSPModel.EMMode.Normal, false));
        //DiscoveryEM(200, RemoveDuplicates(candidateList), trainList, LiftPairList(wordList), new CSPModel(40, 0, 0, 0, FallbackStrategy.Standard));

        //DiscoveryTestDual(RemoveDuplicates(candidateList), trainList, LiftPairList(wordList), 40, 40);
        //DiscoveryTest(RemoveDuplicates(candidateList), trainList, LiftPairList(wordList), 40, 40);
    }

    public static void ChineseDiscoveryTest() throws FileNotFoundException {

        //List<Pair<String, String>> trainList = CharifyTargetWords(GetTabDelimitedPairs(@"C:\Users\jpaster2\Desktop\res\res\Chinese\chinese_full"),chMap);
        List<Pair<String, String>> trainList = UndotTargetWords(GetTabDelimitedPairs("/path/to/res/res/Chinese/chinese_full"));
        List<Pair<String, String>> wordList = GetRandomPartOfList(trainList, 700, 123);

        //StreamWriter writer = new StreamWriter(@"C:\Users\jpaster2\Desktop\res\res\Chinese\chinese_test_pairs.txt");
        //for (Pair<String,String> pair in wordList)
        //    writer.WriteLine(pair.Key + "\t" + pair.Value);

        //writer.Close();

        List<String> candidates = GetListValues(wordList);
        //wordList.RemoveRange(600, 100);
        wordList = wordList.subList(0, 600);

        //DiscoveryTestDual(RemoveDuplicates(candidates), trainList, LiftPairList(wordList), 15, 15);
        DiscoveryEM(200, RemoveDuplicates(candidates), trainList, LiftPairList(wordList), new CSPModel(40, 0, 0, 0.000000000000001, CSPModel.SegMode.Entropy, false, CSPModel.SmoothMode.BySource, FallbackStrategy.Standard, CSPModel.EMMode.Normal, false));
        //TestXMLData(trainList, wordList, 15, 15);


    }

    public static void RussianDiscoveryTest() throws FileNotFoundException {

        List<String> candidateList = LineIO.read("/path/to/res/res/Russian/RussianWords");
        for (int i = 0; i < candidateList.size(); i++)
            candidateList.set(i, candidateList.get(i).toLowerCase());
        //candidateList.Clear();

        //HashMap<String, List<String>> evalList = GetAlexData(@"C:\Users\jpaster2\Desktop\res\res\Russian\evalpairs.txt");//@"C:\Users\jpaster2\Desktop\res\res\Russian\evalpairs.txt");
        HashMap<String, List<String>> evalList = GetAlexData("/path/to/res/res/Russian/evalpairsShort.txt");//@"C:\Users\jpaster2\Desktop\res\res\Russian\evalpairs.txt");

        //List<Pair<String, String>> trainList = NormalizeHebrew(GetTabDelimitedPairs(@"C:\Users\jpaster2\Desktop\res\res\Hebrew\train_EnglishHebrew.txt"));
        List<Pair<String, String>> trainList = GetTabDelimitedPairs("/path/to/WikiTransliteration/Aliases/ruExamples.txt");
        //List<Pair<String, String>> trainList2 = RemoveVeryLong(NormalizeHebrew(GetTabDelimitedPairs(@"C:\Data\WikiTransliteration\Aliases\heExamples-Lax.txt")), 20);
        List<Pair<String, String>> trainList2 = new ArrayList<>(trainList.size());

        HashMap<String, Boolean> usedExamples = new HashMap<>();
        for (String s : evalList.keySet())
            usedExamples.put(s, true);

        //trainList2 = TruncateList(trainList2, 2000);
        for (Pair<String, String> pair : trainList)
            if (!usedExamples.containsKey(pair.getFirst())) trainList2.add(pair);

        DiscoveryEM(200, RemoveDuplicates(GetWords(evalList)), trainList2, evalList, new CSPModel(40, 0, 0, 0.000000000000001, CSPModel.SegMode.Entropy, false, CSPModel.SmoothMode.BySource, FallbackStrategy.Standard, CSPModel.EMMode.Normal, false));
        //DiscoveryTestDual(RemoveDuplicates(candidateList), trainList2, evalList, 15, 15);
        //DiscoveryTestDual(RemoveDuplicates(GetWords(evalList)), trainList2, evalList, 15, 15);

    }

    public static List<String> GetWords(HashMap<String, List<String>> dict) {
        List<String> result = new ArrayList<>();
        for (List<String> list : dict.values())
            result.addAll(list);
        return result;
    }

    public static HashMap<String, List<String>> LiftPairList(List<Pair<String, String>> list) {
        HashMap<String, List<String>> result = new HashMap<>(list.size());
        for (Pair<String, String> pair : list) {
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add(pair.getSecond());
            result.put(pair.getFirst(), tmp);
        }

        return result;
    }

    private static List<Pair<String, String>> UndotTargetWords(List<Pair<String, String>> list) {
        List<Pair<String, String>> result = new ArrayList<>(list.size());
        for (Pair<String, String> pair : list) {
            result.add(new Pair<>(pair.getFirst(), pair.getSecond().replace(".", "")));
        }

        return result;
    }


    private static List<String> RemoveDuplicates(List<String> list) {
        List<String> result = new ArrayList<>(list.size());
        HashMap<String, Boolean> seen = new HashMap<>();
        for (String s : list) {
            if (seen.containsKey(s)) continue;
            seen.put(s, true);
            result.add(s);
        }

        return result;
    }

    private static List<Triple<String, String, Double>> ConvertExamples(List<Pair<String, String>> examples) {
        List<Triple<String, String, Double>> fExamples = new ArrayList<>(examples.size());
        for (Pair<String, String> pair : examples)
            fExamples.add(new Triple<>(pair.getFirst(), pair.getSecond(), 1.0));

        return fExamples;
    }

    /**
     * Calculates a probability table for P(String2 | String1)
     * This normalizes by source counts for each production.
     * <p>
     * FIXME: This is identical to... WikiTransliteration.NormalizeBySourceSubstring
     *
     * @param ?
     * @return
     */
    public static HashMap<Production, Double> PSecondGivenFirst(HashMap<Production, Double> counts) {
        // counts of first words in productions.
        HashMap<String, Double> totals1 = WikiTransliteration.GetAlignmentTotals1(counts);

        HashMap<Production, Double> result = new HashMap<>(counts.size());
        for (Production prod : counts.keySet()) // loop over all productions
        {
            double prodcount = counts.get(prod);
            double sourcecounts = totals1.get(prod.getFirst()); // be careful of unboxing!
            double value = sourcecounts == 0 ? 0 : (prodcount / sourcecounts);
            result.put(prod, value);
        }

        return result;
    }


    private static HashMap<Production, Double> SumNormalize(HashMap<Production, Double> vector) {
        HashMap<Production, Double> result = new HashMap<>(vector.size());
        double sum = 0;
        for (double value : vector.values()) {
            sum += value;
        }

        for (Production key : vector.keySet()) {
            Double value = vector.get(key);
            result.put(key, value / sum);
        }

        return result;
    }


    /**
     * Does this not need to have access to ngramsize? No. It gets all ngrams so it can backoff.
     * <p>
     * By default, this includes padding.
     *
     * @param examples
     * @param maxSubstringLength
     * @return
     */
    public static HashMap<String, Double> GetNgramCounts(List<String> examples, int maxSubstringLength) {
        return WikiTransliteration.GetNgramCounts(1, maxSubstringLength, examples, true);
    }

    /**
     * Given a wikidata file, this gets all the words in the foreign language for the language model.
     *
     * @param fname
     * @return
     */
    public static List<String> getForeignWords(String fname) throws FileNotFoundException {
        List<String> lines = LineIO.read(fname);
        List<String> words = new ArrayList<>();

        for (String line : lines) {
            String[] parts = line.trim().split("\t");
            String foreign = parts[0];

            String[] fsplit = foreign.split(" ");
            for (String word : fsplit) {
                words.add(word);
            }
        }

        return words;
    }

    /**
     * Given a wikidata file, this gets all the words in the foreign language for the language model.
     *
     * @return
     */
    public static List<String> getForeignWords(List<Example> examples) throws FileNotFoundException {

        List<String> words = new ArrayList<>();

        for (Example e : examples) {
            words.add(e.getTransliteratedWord());
        }

        return words;
    }


    public static List<Pair<String, String>> GetTabDelimitedPairs(String filename) throws FileNotFoundException {
        List<Pair<String, String>> result = new ArrayList<>();

        for (String line : LineIO.read(filename)) {
            String[] pair = line.trim().split("\t");
            if (pair.length != 2) continue;
            result.add(new Pair<>(pair[0].trim().toLowerCase(), pair[1].trim().toLowerCase()));
        }

        return result;
    }


    static HashMap<Production, HashMap<Production, Double>> maxCache = new HashMap<>();

    /**
     * This returns a map of productions to counts. These are counts over the entire training corpus. These are all possible
     * productions seen in training data. If a production does not show up in training, it will not be seen here.
     * <p>
     * normalization parameter decides if it is normalized (typically not).
     *
     * @param maxSubstringLength1
     * @param maxSubstringLength2
     * @param examples
     * @param probs
     * @param weightingMode
     * @param normalization
     * @param getExampleCounts
     * @return
     */
    static HashMap<Production, Double> MakeRawAlignmentTable(int maxSubstringLength1, int maxSubstringLength2, List<Triple<String, String, Double>> examples, HashMap<Production, Double> probs, WeightingMode weightingMode, WikiTransliteration.NormalizationMode normalization, boolean getExampleCounts) {
        InternDictionary<String> internTable = new InternDictionary<>();
        HashMap<Production, Double> counts = new HashMap<>();

        List<List<Pair<Production, Double>>> exampleCounts = (getExampleCounts ? new ArrayList<List<Pair<Production, Double>>>(examples.size()) : null);

        int alignmentCount = 0;
        for (Triple<String, String, Double> example : examples) {
            String sourceWord = example.getFirst();
            String bestWord = example.getSecond(); // bestWord? Shouldn't it be target word?
            if (sourceWord.length() * maxSubstringLength2 >= bestWord.length() && bestWord.length() * maxSubstringLength1 >= sourceWord.length()) {
                alignmentCount++;

                HashMap<Production, Double> wordCounts;

                if (weightingMode == WeightingMode.FindWeighted && probs != null)
                    wordCounts = WikiTransliteration.FindWeightedAlignments(sourceWord, bestWord, maxSubstringLength1, maxSubstringLength2, probs, internTable, normalization);
                    //wordCounts = WikiTransliteration.FindWeightedAlignmentsAverage(sourceWord, bestWord, maxSubstringLength1, maxSubstringLength2, probs, internTable, true, normalization);
                else if (weightingMode == WeightingMode.CountWeighted)
                    wordCounts = WikiTransliteration.CountWeightedAlignments(sourceWord, bestWord, maxSubstringLength1, maxSubstringLength2, probs, internTable, normalization, false);
                else if (weightingMode == WeightingMode.MaxAlignment) {

                    HashMap<Production, Double> cached = new HashMap<>();
                    Production p = new Production(sourceWord, bestWord);
                    if (maxCache.containsKey(p)) {
                        cached = maxCache.get(p);
                    }

                    Dictionaries.AddTo(probs, cached, -1.);

                    wordCounts = WikiTransliteration.CountMaxAlignments(sourceWord, bestWord, maxSubstringLength1, probs, internTable, false);
                    maxCache.put(new Production(sourceWord, bestWord), wordCounts);

                    Dictionaries.AddTo(probs, cached, 1);
                } else if (weightingMode == WeightingMode.MaxAlignmentWeighted)
                    wordCounts = WikiTransliteration.CountMaxAlignments(sourceWord, bestWord, maxSubstringLength1, probs, internTable, true);
                else {//if (weightingMode == WeightingMode.None || weightingMode == WeightingMode.SuperficiallyWeighted)
                    // This executes if probs is null
                    wordCounts = WikiTransliteration.FindAlignments(sourceWord, bestWord, maxSubstringLength1, maxSubstringLength2, internTable, normalization);
                }

                if (weightingMode == WeightingMode.SuperficiallyWeighted && probs != null) {
                    wordCounts = SumNormalize(Dictionaries.MultiplyDouble(wordCounts, probs));
                }

                Dictionaries.AddTo(counts, wordCounts, example.getThird());

                if (getExampleCounts) {
                    List<Pair<Production, Double>> curExampleCounts = new ArrayList<>(wordCounts.size());
                    for (Production key : wordCounts.keySet()) {
                        Double value = wordCounts.get(key);
                        curExampleCounts.add(new Pair<>(key, value));
                    }

                    exampleCounts.add(curExampleCounts);
                }
            } else if (getExampleCounts) {
                exampleCounts.add(null);
            }
        }

        return counts;
    }


    public static SparseDoubleVector<Pair<Triple<String, String, String>, String>> PSecondGivenFirst(SparseDoubleVector<Pair<Triple<String, String, String>, String>> productionProbs) {
        SparseDoubleVector<Pair<Triple<String, String, String>, String>> result = new SparseDoubleVector<>();
        SparseDoubleVector<Triple<String, String, String>> totals = new SparseDoubleVector<>();
        for (Pair<Triple<String, String, String>, String> key : productionProbs.keySet()) {
            Double value = productionProbs.get(key);
            totals.put(key.getFirst(), totals.get(key.getFirst()) + value);
        }

        for (Pair<Triple<String, String, String>, String> key : productionProbs.keySet()) {
            Double value = productionProbs.get(key);
            result.put(key, value / totals.get(key.getFirst()));
        }

        return result;

    }


    /**
     * Calculates a probability table for P(String2 | String1)
     *
     * @param counts
     * @return
     */
    public static SparseDoubleVector<Pair<Triple<String, String, String>, String>> PSecondGivenFirstTriple(SparseDoubleVector<Pair<Triple<String, String, String>, String>> counts) {
        SparseDoubleVector<Triple<String, String, String>> totals = new SparseDoubleVector<>();
        for (Pair<Triple<String, String, String>, String> key : counts.keySet()) {
            Double value = counts.get(key);
            totals.put(key.getFirst(), totals.get(key.getFirst()) + value);
        }

        SparseDoubleVector<Pair<Triple<String, String, String>, String>> result = new SparseDoubleVector<Pair<Triple<String, String, String>, String>>(counts.size());
        for (Pair<Triple<String, String, String>, String> key : counts.keySet()) {
            Double value = counts.get(key);
            double total = totals.get(key.getFirst());
            result.put(key, total == 0 ? 0 : value / total);
        }

        return result;
    }

    /**
     * Returns a random subset of a list; the list provided is modified to remove the selected items.
     *
     * @param wordList
     * @param count
     * @param seed
     * @return
     */
    public static List<Pair<String, String>> GetRandomPartOfList(List<Pair<String, String>> wordList, int count, int seed) {
        Random r = new Random(seed);

        List<Pair<String, String>> randomList = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {

            int index = r.nextInt(wordList.size()); //r.Next(wordList.size());
            randomList.add(wordList.get(index));
            wordList.remove(index);
        }

        return randomList;
    }

    public static List<String> GetListValues(List<Pair<String, String>> wordList) {
        List<String> valueList = new ArrayList<>(wordList.size());
        for (Pair<String, String> pair : wordList)
            valueList.add(pair.getSecond());

        return valueList;
    }


    static double Choose(double n, double k) {
        double result = 1;

        for (double i = Math.max(k, n - k) + 1; i <= n; ++i)
            result *= i;

        for (double i = 2; i <= Math.min(k, n - k); ++i)
            result /= i;

        return result;
    }

    public static double[][] SegmentationCounts(int maxLength) {
        double[][] result = new double[maxLength][];
        for (int i = 0; i < maxLength; i++) {
            result[i] = new double[i + 1];
            for (int j = 0; j <= i; j++)
                result[i][j] = Choose(i, j);
        }

        return result;
    }

    public static double[][] SegSums(int maxLength) {
        double[][] segmentationCounts = SegmentationCounts(maxLength);
        double[][] result = new double[maxLength][];
        for (int i = 0; i < maxLength; i++) {
            result[i] = new double[maxLength];
            for (int j = 0; j < maxLength; j++) {
                int minIJ = Math.min(i, j);
                for (int k = 0; k <= minIJ; k++)
                    result[i][j] += segmentationCounts[i][k] * segmentationCounts[j][k];// *Math.Pow(0.5, k + 1);
            }
        }

        return result;
    }

    /**
     * Number of possible segmentations.
     */
    public static double[][] segSums = SegSums(40);

    public static void DiscoveryEM(int iterations, List<String> candidateWords, List<Pair<String, String>> trainingPairs, HashMap<String, List<String>> testingPairs, TransliterationModel model) {
        List<Triple<String, String, Double>> trainingTriples = ConvertExamples(trainingPairs);

        for (int i = 0; i < iterations; i++) {
            System.out.println("Iteration #" + i);

            long startTime = System.nanoTime();
            System.out.print("Training...");
            model = model.LearnModel(trainingTriples);
            long endTime = System.nanoTime();
            System.out.println("Finished in " + (startTime - endTime) / (1000000 * 1000) + " seconds.");

            DiscoveryEvaluation(testingPairs, candidateWords, model);
        }

        System.out.println("Finished.");
    }


    public static String NormalizeHebrew(String word) {
        word = word.replace('ן', 'נ');
        word = word.replace('ך', 'כ');
        word = word.replace('ץ', 'צ');
        word = word.replace('ם', 'מ');
        word = word.replace('ף', 'פ');

        return word;
    }

    public static List<Pair<String, String>> NormalizeHebrew(List<Pair<String, String>> pairs) {
        List<Pair<String, String>> result = new ArrayList<>(pairs.size());

        for (int i = 0; i < pairs.size(); i++)
            result.add(new Pair<>(pairs.get(i).getFirst(), NormalizeHebrew(pairs.get(i).getSecond())));

        return result;
    }

    /**
     * This is still broken...
     *
     * @param path
     * @return
     * @throws FileNotFoundException
     */
    static HashMap<String, List<String>> GetAlexData(String path) throws FileNotFoundException {

        HashMap<String, List<String>> result = new HashMap<>();

        // TODO: this is all broken.
//            ArrayList<String> data = LineIO.read(path);
//
//            for (String line : data)
//            {
//                if (line.length() == 0) continue;
//
//                Match match = Regex.Match(line, "(?<eng>\\w+)\t(?<rroot>\\w+)(?: {(?:-(?<rsuf>\\w*?)(?:(?:, )|}))+)?", RegexOptions.Compiled);
//
//                String russianRoot = match.Groups["rroot"].Value;
//                if (russianRoot.length() == 0)
//                    System.out.println("Parse error");
//
//                List<String> russianList = new ArrayList<>();
//
//                //if (match.Groups["rsuf"].Captures.Count == 0)
//                    russianList.Add(russianRoot.toLower()); //root only
//                //else
//                    for (Capture capture : match.Groups["rsuf"].Captures)
//                        russianList.add((russianRoot + capture.Value).ToLower());
//
//                result[match.Groups["eng"].Value.ToLower()] = russianList;
//
//            }

        return result;
    }


    public static HashMap<Production, Double> PruneProbs(int topK, HashMap<Production, Double> probs) {
        HashMap<String, List<Pair<String, Double>>> lists = new HashMap<>();
        for (Production key : probs.keySet()) {
            Double value = probs.get(key);

            if (!lists.containsKey(key.getFirst())) {
                lists.put(key.getFirst(), new ArrayList<Pair<String, Double>>());
            }

            lists.get(key.getFirst()).add(new Pair<>(key.getSecond(), value));
        }

        HashMap<Production, Double> result = new HashMap<>();
        for (String key : lists.keySet()) {
            List<Pair<String, Double>> value = lists.get(key);
            Collections.sort(value, new Comparator<Pair<String, Double>>() {
                @Override
                public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                    double v = o2.getSecond() - o1.getSecond();
                    if (v > 0) {
                        return 1;
                    } else if (v < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
            int toAdd = Math.min(topK, value.size());
            for (int i = 0; i < toAdd; i++) {
                result.put(new Production(key, value.get(i).getFirst()), value.get(i).getSecond());
            }
        }

        return result;
    }

    private static void DiscoveryEvaluation(HashMap<String, List<String>> testingPairs, List<String> candidates, TransliterationModel model) {
        int correct = 0;
        //int contained = 0;
        double mrr = 0;
        int misses = 0;

        for (String key : testingPairs.keySet()) {
            List<String> value = testingPairs.get(key);

            //double[] scores = new double[candidates.size()];
            final List<Double> scores = new ArrayList<>(candidates.size());
            String[] words = candidates.toArray(new String[candidates.size()]);

            final List<String> fakewords = new ArrayList<>(candidates);

            for (int i = 0; i < words.length; i++)
                scores.set(i, model.GetProbability(key, words[i]));

            // sort the words according to the scores. Assume that the indices match up
            //Array.Sort<double, String>(scores, words);
            Collections.sort(candidates, new Comparator<String>() {
                public int compare(String left, String right) {
                    return Double.compare(scores.get(fakewords.indexOf(left)), scores.get(fakewords.indexOf(right)));
                }
            });

            int index = 0;
            for (int i = words.length - 1; i >= 0; i--)
                if (value.contains(words[i])) {
                    index = i;
                    break;
                }

            index = words.length - index;

            if (index == 1)
                correct++;
            else
                misses++;
            mrr += ((double) 1) / index;
        }

        mrr /= testingPairs.size();

        System.out.println(testingPairs.size() + " pairs tested in total; " + candidates.size() + " candidates.");
        //System.out.println(contained + " predictions contained (" + (((double)contained) / testingPairs.Count) + ")");
        System.out.println(correct + " predictions exactly correct (" + (((double) correct) / testingPairs.size()) + ")");
        System.out.println("MRR: " + mrr);
    }


    public static SparseDoubleVector<Production> InitializeWithRomanization(SparseDoubleVector<Production> probs, List<Triple<String, String, Double>> trainingTriples, List<Example> testing) {

//        List<String> hebtable;
//        try {
//             hebtable = LineIO.read("hebrewromanization.txt");
//        } catch (FileNotFoundException e) {
//            return probs;
//        }
//
//        for(String line : hebtable){
//            String[] sline = line.split(" ");
//            String heb = sline[0];
//            String eng = sline[1];
//
//            probs.put(new Production(eng, heb), 1.0);
//        }

        // get all values from training.
        for (Triple<String, String, Double> t : trainingTriples) {
            String chinese = t.getSecond();
            for (char c : chinese.toCharArray()) {
                // CHINESE
                String[] res = PinyinHelper.toHanyuPinyinStringArray(c);
                for (String s : res) {
                    // FIXME: strip number from s?
                    String ss = s.substring(0, s.length() - 1);
                    probs.put(new Production(ss, c + ""), 1.);
                }
            }
        }

        // get all values from testing also
        for (MultiExample t : testing) {
            List<String> chineseWords = t.getTransliteratedWords();
            for (String chinese : chineseWords) {
                for (char c : chinese.toCharArray()) {
                    // CHINESE
                    String[] res = PinyinHelper.toHanyuPinyinStringArray(c);
                    for (String s : res) {
                        // FIXME: strip number from s?
                        String sss = s.substring(0, s.length() - 1);
                        probs.put(new Production(sss, c + ""), 1.);
                    }
                }
            }
        }


        return probs;
    }


    /**
     * Convert each production into a set of productions with different origins
     *
     * @param probs
     * @param numOrigins
     * @return
     */
    public static SparseDoubleVector<Production> SplitIntoOrigins(SparseDoubleVector<Production> probs, int numOrigins) {

        SparseDoubleVector<Production> newprobs = new SparseDoubleVector<>();

        for (Production p : probs.keySet()) {
            double prob = probs.get(p);
            for (int i = 0; i < numOrigins; i++) {
                Production po = new Production(p.segS, p.segT, i);
                newprobs.put(po, prob / numOrigins);
            }
        }

        return newprobs;
    }


    public enum WeightingMode {
        None, FindWeighted, SuperficiallyWeighted, CountWeighted, MaxAlignment, MaxAlignmentWeighted
    }

}
