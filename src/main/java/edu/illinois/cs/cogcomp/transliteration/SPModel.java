package edu.illinois.cs.cogcomp.transliteration;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.Triple;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.utils.SparseDoubleVector;
import edu.illinois.cs.cogcomp.utils.TopList;

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
        double minProductionProbability = 0.000000000000001;
        int maxSubstringLength1 = 15;
        int maxSubstringLength2 = 15;

        private HashMap<String, Double> languageModel = null;
        //private Dictionary<String, double> languageModelDual=null;

        private List<Triple<String,String,Double>> trainingExamples;
        //private List<Triple<String,String,double>> trainingExamplesDual;

        /**
         * This is the production probabilities table.
         */
        private SparseDoubleVector<Pair<String, String>> probs = null;

        /**
         * This is the production probabilities table multiplied by the segmentfactor.
         */
        private SparseDoubleVector<Pair<String, String>> multiprobs = null;

        private HashMap<Pair<String, String>, Double> pruned = null; // used to be a SparseDoubleVector
        //private SparseDoubleVector<Pair<String, String>> probsDual = null;
        //private SparseDoubleVector<Pair<String, String>> prunedDual = null;
        private HashMap<String, String> probMap = null;
        //private Map<String, String> probMapDual = null;

        /**
         * The maximum number of candidate transliterations returned by Generate, as well as preserved in intermediate steps.
         * Larger values will possibly yield better transliterations, but will be more computationally expensive.
         * The default value is 100.
         * COMMENTED OUT BY SWM.
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

        public void WriteToStream(OutputStream stream) throws IOException {
            DataOutputStream writer = new DataOutputStream(stream);

            if (languageModel == null) writer.write(-1);
            else
            {
                writer.write(languageModel.size());
                for(String key : languageModel.keySet())
                {
                    // TODO what are the data types here?
                    writer.writeChars(key);
                    writer.writeDouble(languageModel.get(key));
                }
            }

            writer.write(ngramSize);

            writer.write(trainingExamples.size());
            for(Triple<String, String, Double> triple : trainingExamples)
            {
                writer.writeChars(triple.getFirst());
                writer.writeChars(triple.getSecond());
                writer.writeDouble(triple.getThird());
            }

            WriteToWriter(writer, probs);

            writer.flush();
        }

        /**
         * This writes the production probabilities out to file in human-readable format.
         * @param fname the name of the output file
         * @param threshold only write probs above this threshold
         * @throws IOException
         */
        public void WriteProbs(String fname, double threshold) throws IOException {
            ArrayList<String> outlines = new ArrayList<>();
            for(Pair<String, String> t : probs.keySet()){
                if(probs.get(t) > threshold) {
                    outlines.add(t.getFirst() + "\t" + t.getSecond() + "\t" + probs.get(t));
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
         * Deserializes an SPModel from a stream.
         * SWM: ignore this for now.
         */
        public SPModel(InputStream stream) throws IOException {
            DataInputStream reader = new DataInputStream(stream);
            int lml = reader.readInt();
            if (lml == -1) languageModel = null;
            else
            {
                languageModel = new HashMap<>(lml);
                for (int i = 0; i < lml; i++) {
                    // FIXME: is this correct??? Should be ReadString
                    languageModel.put(reader.readUTF(), reader.readDouble());
                }
            }

            ngramSize = reader.readInt();

            int tec = reader.readInt();
            trainingExamples = new ArrayList<>(tec);
            for (int i = 0; i < tec; i++)
                trainingExamples.add(new Triple<>(reader.readUTF(), reader.readUTF(), reader.readDouble()));

            probs = ReadTableFromReader(reader);

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
//        public void SetLanguageModel(List<String> targetLanguageExamples)
//        {
//            languageModel = Program.GetNgramCounts(targetLanguageExamples, maxSubstringLength2);
//        }

        /**
         * Trains the model for the specified number of iterations.
         * @param emIterations The number of iterations to train for.
         */
        public void Train(int emIterations)
        {
            List<Triple<String, String, Double>> trainingTriples = trainingExamples;

            pruned = null;
            multiprobs = null;

            if (probs == null)
            {
<<<<<<< HEAD
                // FIXME: out variables... is exampleCounts actually used anywhere???
                //List<List<Pair<Pair<String, String>, Double>>> exampleCounts = new ArrayList<>();
                probs = new SparseDoubleVector<>(Program.MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, null, Program.WeightingMode.None, WikiTransliteration.NormalizationMode.None, false));
=======
                // FIXME: is exampleCounts actually used anywhere???
                List<List<Pair<Pair<String, String>, Double>>> exampleCounts = new ArrayList<>();
                boolean getExampleCounts = false;
                probs = new SparseDoubleVector<>(Program.MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2,
                        trainingTriples, null, Program.WeightingMode.None, WikiTransliteration.NormalizationMode.None, getExampleCounts));
>>>>>>> dff36e31160cde38cde88529386e8d04be716bc2
                probs = new SparseDoubleVector<>(Program.PSecondGivenFirst(probs));
            }

            for (int i = 0; i < emIterations; i++)
            {

                boolean getExampleCounts = true;
                probs = new SparseDoubleVector<>(Program.MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2,
                        trainingTriples, segmentFactor != 1 ? probs.multiply(segmentFactor) : probs, Program.WeightingMode.CountWeighted, WikiTransliteration.NormalizationMode.None, getExampleCounts));
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

            HashMap<Pair<String, String>, Double> memoizationTable = new HashMap<>();
            return WikiTransliteration.GetSummedAlignmentProbability(sourceWord, transliteratedWord, maxSubstringLength1, maxSubstringLength2, multiprobs, memoizationTable, minProductionProbability)
                            / Program.segSums[sourceWord.length() - 1][transliteratedWord.length() - 1];
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
        public TopList<Double,String> Generate(String sourceWord)
        {
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
                    fPredictions.add(prediction.getFirst() * Math.pow(WikiTransliteration.GetLanguageProbability(prediction.getSecond(), languageModel, ngramSize), 1), prediction.getSecond());
                }
                result = fPredictions;
            }

            return result;
        }
    }
