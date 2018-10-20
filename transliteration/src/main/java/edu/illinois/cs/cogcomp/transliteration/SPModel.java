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
import edu.illinois.cs.cogcomp.utils.SparseDoubleVector;
import edu.illinois.cs.cogcomp.utils.TopList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.util.*;

/**
 * <p>Segmentation-Production Model for generating and discovery transliterations.
 * Generation is the process of creating the transliteration of a word in a target language given the word in the source language.
 * Discovery is the process of identifying a transliteration from a list of candidate words in the target language (this
 * is facilitated here by obtaining the probability P(T|S)).</p>
 *
 * <p>
 * One useful idea (used in our paper) is that you can find Sqrt(P(T|S)*P(S|T)) rather than just P(T|S) alone, since
 * you don't necessarily know a priori in what direction the word was originally translated (e.g. from English to Russian or Russian to English?).
 * In our discovery experiments this geometric mean performed substantially better, although of course your results may vary.
 * </p>
 */
public class SPModel
    {
        private Logger logger = LoggerFactory.getLogger(SPModel.class);
        double minProductionProbability = 0.000000000000001;
        int maxSubstringLength1 = 4;
        int maxSubstringLength2 = 4;

        /**
         * How many origins do we expect there to be? This should probably be about 50 or less.
         */
        public static final int numOrigins = 1;

        private HashMap<String, Double> languageModel = null;
        //private Dictionary<String, double> languageModelDual=null;

        private List<Triple<String,String,Double>> trainingExamples;
        //private List<Triple<String,String,double>> trainingExamplesDual;

        /**
         * This is the production probabilities table.
         */
        private SparseDoubleVector<Production> probs = null;

        public SparseDoubleVector<Production> getProbs(){
            return probs;
        }

        /**
         * This is the production probabilities table multiplied by the segmentfactor.
         */
        private SparseDoubleVector<Production> multiprobs = null;

        private HashMap<Production, Double> pruned = null; // used to be a SparseDoubleVector
        //private SparseDoubleVector<Pair<String, String>> probsDual = null;
        //private SparseDoubleVector<Pair<String, String>> prunedDual = null;
        private HashMap<String, HashSet<String>> probMap = null;
        //private Map<String, String> probMapDual = null;

        /**
         * The maximum number of candidate transliterations returned by Generate, as well as preserved in intermediate steps.
         * Larger values will possibly yield better transliterations, but will be more computationally expensive.
         * The default value is 100.
         */
        private int maxCandidates=100;

        public int getMaxCandidates(){
            return this.maxCandidates;
        }

        public void setMaxCandidates(int value){
            this.maxCandidates = value;
        }

        /**
         * The (0,1] segment factor determines the preference for longer segments by effectively penalizing the total number of segments used.
         * The probability of a transliteration is multiplied by SegmentFactor^[#segments].  Lower values prefer longer segments.  A value of 0.5, the default, is better for generation,
         * since productions from longer segments are more likely to be exactly correct; conversely, a value of 1 is better for discrimination.
         * Set SegmentFactor before training begins.  Setting it after this time effectively changes the model while keeping the same parameters, which isn't a good idea.</para>
         */
        private double segmentFactor = 0.5;

        public double getSegmentFactor(){
            return segmentFactor;
        }

        public void setSegmentFactor(double value){
            this.segmentFactor = value;
        }

        /**
         * The size of the ngrams used by the language model (default: 4).  Irrelevant if no language model is created with SetLanguageModel.
         */
        private int ngramSize = 4;

        public int getNgramSize(){
            return this.ngramSize;
        }

        public void setNgramSize(int value){
            this.ngramSize = value;
        }

        /**
         * This writes the model out to file.
         * @param writer
         * @param table
         * @throws IOException
         */
        private void WriteToWriter(DataOutputStream writer, SparseDoubleVector<Pair<String, String>> table) throws IOException {
            if (table == null) {
                writer.write(-1);
            }
            else
            {
                writer.write(table.size());
                //for (KeyValuePair<Pair<String, String>, Double> entry : table)
                for(Pair<String, String> key : table.keySet())
                {
                    Double value = table.get(key);    
                    writer.writeChars(key.getFirst());
                    writer.writeChars(key.getSecond());
                    writer.writeDouble(value);
                }
            }
        }

        private SparseDoubleVector<Pair<String, String>> ReadTableFromReader(DataInputStream reader) throws IOException {
            int count = reader.readInt();
            if (count == -1) return null;
            SparseDoubleVector<Pair<String, String>> table = new SparseDoubleVector<>(count);
            for (int i = 0; i < count; i++)
            {
                // FIXME: this is readChar, probably should be readString??
                Pair<String, String> keyPair = new Pair<>(reader.readChar() + "", reader.readChar() + "");
                table.put(keyPair, reader.readDouble());
            }

            return table;
        }

        /**
         * This writes the production probabilities out to file in human-readable format.
         * @param fname the name of the output file
         * @param threshold only write probs above this threshold
         * @throws IOException
         */
        public void WriteProbs(String fname, double threshold) throws IOException {
            ArrayList<String> outlines = new ArrayList<>();

            List<Production> keys = new ArrayList<>(probs.keySet());
            Collections.sort(keys, new Comparator<Production>() {
                @Override
                public int compare(Production o1, Production o2) {
                    return o1.getFirst().compareTo(o2.getFirst());
                }
            });
            for(Production t : keys){
                if(probs.get(t) > threshold) {
                    String tstr = t.getFirst() + "\t" + t.getSecond();
                    outlines.add(tstr + "\t" + probs.get(t));
                }
            }
            LineIO.write(fname, outlines);
        }

        /**
         * This just calls WriteProbs(fname, threshold) with threshold of 0.
         * @param fname the name of the output file.
         */
        public void WriteProbs(String fname) throws IOException {
            WriteProbs(fname, 0.0);
        }

        /**
         * This is basically the reverse of the WriteProbs function.
         */
        public void ReadProbs(String fname) throws FileNotFoundException {
            List<String> lines = LineIO.read(fname);

            probs = new SparseDoubleVector<>();

            for(String line : lines){
                String[] sline = line.trim().split("\t");
                probs.put(new Production(sline[0], sline[1]), Double.parseDouble(sline[2]));
            }
        }


        /**
         * This reads a model from file.
         */
        public SPModel(String fname) throws IOException {
            if(probs != null){
                probs.clear();
            }
            ReadProbs(fname);
        }

        /**
         * Creates a new model for generating transliterations (creating new words in the target language from a word in the source language).
         * Remember to Train() the model before calling Generate().
         * @param examples The training examples to learn from.
         */
        public SPModel(Collection<Example> examples)
        {
            trainingExamples = new ArrayList<>(examples.size());
            for(Example example : examples) {
                // Default weight of examples is 1
                trainingExamples.add(example.Triple());
            }
        }

        /**
         *
         * Creates and sets a simple (ngram) language model to use when generating transliterations.
         * The transliterations will then have probability == P(T|S)*P(T), where P(T) is the language model.
         * In principle this allows you to leverage the presumably vast number of words available in the target language,
         * although practical results may vary.
         * @param targetLanguageExamples Example words from the target language you're transliterating into.
         */
        public void SetLanguageModel(List<String> targetLanguageExamples)
        {
            logger.info("Setting language model with " + targetLanguageExamples.size() + " words.");
            languageModel = Program.GetNgramCounts(targetLanguageExamples, maxSubstringLength2);
        }

        /**
         * Creates the language model directly. This was intended for use with SRILM.
         * @param lm
         */
        public void SetLanguageModel(HashMap<String,Double> lm){
            logger.info("Setting language model with hashmap directly.");
            languageModel = lm;
        }


        /**
         * This initializes with rom=false and testing = empty list.
         */
        public void Train(int emIterations){
            Train(emIterations, false, new ArrayList<Example>());
        }

        /**
         * Trains the model for the specified number of iterations.
         * @param emIterations The number of iterations to train for.
         * @param testing
         */
        public void Train(int emIterations, boolean rom, List<Example> testing)
        {
            List<Triple<String, String, Double>> trainingTriples = trainingExamples;

            pruned = null;
            multiprobs = null;

            // this is the initialization.
            if (probs == null)
            {

                // FIXME: out variables... is exampleCounts actually used anywhere???
                //List<List<Pair<Pair<String, String>, Double>>> exampleCounts = new ArrayList<>();
                probs = new SparseDoubleVector<>(Program.MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, null, Program.WeightingMode.None, WikiTransliteration.NormalizationMode.None, false));

                boolean getExampleCounts = false;
                // gets counts of productions, not normalized.
                probs = new SparseDoubleVector<>(Program.MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2,
                        trainingTriples, null, Program.WeightingMode.None, WikiTransliteration.NormalizationMode.None, getExampleCounts));

                // this just normalizes by the source string.
                probs = new SparseDoubleVector<>(Program.PSecondGivenFirst(probs));

                // FIXME: uniform origin initialization?
                probs = Program.SplitIntoOrigins(probs, this.numOrigins);

                if(rom) {
                    probs = Program.InitializeWithRomanization(probs, trainingTriples, testing);
                }

            }

            for (int i = 0; i < emIterations; i++)
            {
                logger.info("Training, iteration=" + (i+1));
                boolean getExampleCounts = true;
                // Difference is Weighting mode.
                probs = new SparseDoubleVector<>(Program.MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2,
                        trainingTriples, segmentFactor != 1 ? probs.multiply(segmentFactor) : probs, Program.WeightingMode.CountWeighted, WikiTransliteration.NormalizationMode.None, getExampleCounts));

                // this just normalizes by the source string.
                probs = new SparseDoubleVector<>(Program.PSecondGivenFirst(probs));
            }
        }

        /**
         * Calculates the probability P(T|S), that is, the probability that transliteratedWord is a transliteration of sourceWord.
         * @param sourceWord The word is the source language
         * @param transliteratedWord The purported transliteration of the source word, in the target language
         * @return P(T|S)
         */
        public double Probability(String sourceWord, String transliteratedWord)
        {
            if (multiprobs==null) {
                multiprobs = probs.multiply(segmentFactor);
            }

            HashMap<Production, Double> memoizationTable = new HashMap<>();
            int orig = -1;

            if(sourceWord.length() > Program.segSums.length){
                System.err.println("Sourceword is too long (length " + sourceWord.length() + "). Setting prob=0");
                return 0;
            }else if(transliteratedWord.length() > Program.segSums.length){
                System.err.println("TransliterateWord is too long (length " + transliteratedWord.length() + "). Setting prob=0");
                return 0;
            }

            double score = WikiTransliteration.GetSummedAlignmentProbability(sourceWord, transliteratedWord, maxSubstringLength1, maxSubstringLength2, multiprobs, memoizationTable, minProductionProbability, orig)
                        / Program.segSums[sourceWord.length() - 1][transliteratedWord.length() - 1];

            return score;
        }

        /**
         * Generates a TopList of the most likely transliterations of the given word.
         * The TopList is like a SortedList sorted most-probable to least-probable with probabilities (doubles) as keys,
         * except that the keys may not be unique (multiple transliterations can be equiprobable).
         * The most likely transliteration is at index 0.
         * The number of transliterations returned in this manner will not exceed the maxCandidates property.
         * You must train the model before generating transliterations.
         * @param sourceWord The word to transliterate.
         * @return A TopList containing the most likely transliterations of the word.
         */
        public TopList<Double,String> Generate(String sourceWord) throws Exception {
            if (probs == null) throw new NullPointerException("Must train at least one iteration before generating transliterations");

            if (pruned==null)
            {
                multiprobs = probs.multiply(segmentFactor);
                pruned = Program.PruneProbs(maxCandidates, multiprobs);
                probMap = WikiTransliteration.GetProbMap(pruned);
            }

            TopList<Double, String> result = WikiTransliteration.Predict2(maxCandidates, sourceWord, maxSubstringLength2, probMap, pruned, new HashMap<String, HashMap<String, Double>>(), maxCandidates);


            if (languageModel != null)
            {
                TopList<Double, String> fPredictions = new TopList<>(maxCandidates);
                for (Pair<Double, String> prediction : result) {
                    Double prob = Math.pow(WikiTransliteration.GetLanguageProbability(prediction.getSecond(), languageModel, ngramSize), 1);
                    double reranked = Math.log(prediction.getFirst()) + Math.log(prob) / prediction.getSecond().length();

                    fPredictions.add(Math.exp(reranked), prediction.getSecond());
                }
                result = fPredictions;
            }

            return result;
        }
    }
