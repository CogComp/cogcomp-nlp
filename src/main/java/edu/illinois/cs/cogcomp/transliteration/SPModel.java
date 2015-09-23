package edu.illinois.cs.cogcomp.transliteration;


import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.Triple;

import java.io.*;
import java.util.*;

/// <summary>
/// <para>Segmentation-Production Model for generating and discovery transliterations.
/// Generation is the process of creating the transliteration of a word in a target language given the word in the source language.
/// Discovery is the process of identifying a transliteration from a list of candidate words in the target language (this
/// is facilitated here by obtaining the probability P(T|S)).</para>
///
/// <para>
/// One useful idea (used in our paper) is that you can find Sqrt(P(T|S)*P(S|T)) rather than just P(T|S) alone, since
/// you don't necessarily know a priori in what direction the word was originally translated (e.g. from English to Russian or Russian to English?).
/// In our discovery experiments this geometric mean performed substantially better, although of course your results may vary.
/// </para>
/// </summary>
public class SPModel
    {
        double minProductionProbability = 0.000000000000001;
        int maxSubstringLength1 = 15;
        int maxSubstringLength2 = 15;

        private HashMap<String, Double> languageModel = null;
        //private Dictionary<String, double> languageModelDual=null;
        private int ngramSize = 4;
        private List<Triple<String,String,Double>> trainingExamples;
        //private List<Triple<String,String,double>> trainingExamplesDual;
        private SparseDoubleVector<Pair<String, String>> probs = null;
        private SparseDoubleVector<Pair<String, String>> multiprobs = null;
        private SparseDoubleVector<Pair<String, String>> pruned = null;
        //private SparseDoubleVector<Pair<String, String>> probsDual = null;
        //private SparseDoubleVector<Pair<String, String>> prunedDual = null;
        private HashMap<String, String> probMap = null;
        //private Map<String, String> probMapDual = null;

        private int maxCandidates=100;
        private double segmentFactor = 0.5;

        // binary writer is a c# class, sparsedoublevector is from pasternack.
        private void WriteToWriter(DataOutputStream writer, SparseDoubleVector<Pair<String, String>> table) throws IOException {
            if (table == null)
                writer.write(-1);
            else
            {
                writer.write(table.Count);
                for (KeyValuePair<Pair<String, String>, Double> entry : table)
                {
                    writer.write(entry.Key.x);
                    writer.write(entry.Key.y);
                    writer.write(entry.Value);
                }
            }
        }

        private SparseDoubleVector<Pair<String, String>> ReadTableFromReader(DataInputStream reader) throws IOException {
            int count = reader.readInt();
            if (count == -1) return null;
            SparseDoubleVector<Pair<String, String>> table = new SparseDoubleVector<Pair<String, String>>(count);
            for (int i = 0; i < count; i++)
            {
                // FIXME: this is readChar, probably should be readString??
                Pair<String, String> keyPair = new Pair<>(reader.readChar() + "", reader.readChar() + "");
                table.Add(keyPair, reader.readDouble());
            }

            return table;
        }

        public void WriteToStream(OutputStream stream) throws IOException {
            DataOutputStream writer = new DataOutputStream(stream);

            if (languageModel == null) writer.write(-1);
            else
            {
                writer.write(languageModel.size());
                for(Map.Entry<String, Double> pair : languageModel)
                {
                    // TODO what are the data types here?
                    writer.writeChars(pair.getKey());
                    writer.writeDouble(pair.getValue());
                }
            }

            writer.write(ngramSize);

            writer.write(trainingExamples.Count);
            for(Triple<String, String, Double> triple : trainingExamples)
            {
                writer.writeChars(triple.getFirst());
                writer.writeChars(triple.getSecond());
                writer.writeDouble(triple.getThird());
            }

            WriteToWriter(writer, probs);

            writer.flush();
        }

        /// <summary>
        /// Deserializes an SPModel from a stream.
        /// </summary>
        /// <param name="stream"></param>
        public SPModel(InputStream stream) throws IOException {
            DataInputStream reader = new DataInputStream(stream);
            int lml = reader.readInt();
            if (lml == -1) languageModel = null;
            else
            {
                languageModel = new HashMap<>(lml);
                for (int i = 0; i < lml; i++)
                    languageModel.put(reader.ReadString(), reader.ReadDouble());
            }

            ngramSize = reader.readInt();

            int tec = reader.readInt();
            trainingExamples = new ArrayList<>(tec);
            for (int i = 0; i < tec; i++)
                trainingExamples.add(new Triple<String, String, Double>(reader.ReadString(), reader.ReadString(), reader.readDouble()));

            probs = ReadTableFromReader(reader);

            /*
            pruned = ReadTableFromReader(reader);

            //DEBUG:
            pruned = null;

            int pmc = reader.ReadInt32();
            if (pmc == -1)
                probMap = null;
            else
            {
                probMap = new Map<String,String>();
                for (int i = 0; i < pmc; i++)
                    probMap.Add(reader.ReadString(),reader.ReadString());
            }            
             */
        }

        /// <summary>
        /// The maximimum number of candidate transliterations returned by Generate, as well as preserved in intermediate steps.
        /// Larger values will possibly yield better transliterations, but will be more computationally expensive.
        /// The default value is 100.
        /// </summary>
        public int MaxCandidates
        {
            get { return maxCandidates; }
            set { maxCandidates = value; }
        }

        /// <summary>
        /// <para>
        /// The (0,1] segment factor determines the preference for longer segments by effectively penalizing the total number of segments used.
        /// The probability of a transliteration is multiplied by SegmentFactor^[#segments].  Lower values prefer longer segments.  A value of 0.5, the default, is better for generation,
        /// since productions from longer segments are more likely to be exactly correct; conversely, a value of 1 is better for discrimination.
        /// </para>
        /// <para>Set SegmentFactor before training begins.  Setting it after this time effectively changes the model while keeping the same parameters, which isn't a good idea.</para>
        /// </summary>
        public double SegmentFactor
        {
            get { return segmentFactor; }
            set { segmentFactor = value; }
        }

        /// <summary>
        /// Creates a new model for generating transliterations (creating new words in the target language from a word in the source language).      
        /// Remember to Train() the model before calling Generate().
        /// </summary>
        /// <param name="examples">The training examples to learn from.</param>        
        public SPModel(Collection<Example> examples)
        {
            trainingExamples = new ArrayList<Triple<String,String,Double>>(examples.size());
            for(Example example : examples)
                trainingExamples.add(example.Triple);
        }

        /// <summary>
        /// Creates and sets a simple (ngram) language model to use when generating transliterations.
        /// The transliterations will then have probability == P(T|S)*P(T), where P(T) is the language model.
        /// In principle this allows you to leverage the presumably vast number of words available in the target language,
        /// although practical results may vary.
        /// </summary>
        /// <param name="targetLanguageExamples">Example words from the target language you're transliterating into.</param>
        public void SetLanguageModel(List<String> targetLanguageExamples)
        {
            languageModel = Program.GetNgramCounts(targetLanguageExamples, maxSubstringLength2); 
        }

        /// <summary>
        /// The size of the ngrams used by the language model (default: 4).  Irrelevant if no language model is created with SetLanguageModel.
        /// </summary>
        public int LanguageModelNgramSize
        {
            get { return ngramSize; }
            set { ngramSize = value; }
        }

        /*
        public void Train(IDictionary<String,String> trainingPairs)
        {
            List<KeyValuePair<String,String>> pairs = new List<KeyValuePair<String,String>>(trainingPairs.Count);
            foreach (KeyValuePair<String,String> pair in trainingPairs)
                pairs.Add(pair);

            Train(Program.ConvertExamples(pairs));
        }*/

        /// <summary>
        /// Trains the model for the specified number of iterations.
        /// </summary>
        /// <param name="emIterations">The number of iterations to train for.</param>
        public void Train(int emIterations)
        {
            List<Triple<String, String, Double>> trainingTriples = trainingExamples;

            List<List<KeyValuePair<Pair<String, String>, Double>>> exampleCounts;

            pruned = null;
            multiprobs = null;

            if (probs == null)
            {
                probs = Program.MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, null, WeightingMode.None, NormalizationMode.None, false, out exampleCounts);
                probs = Program.PSecondGivenFirst(probs);
            }

            for (int i = 0; i < emIterations; i++)            
            {
                probs = Program.MakeRawAlignmentTable(maxSubstringLength1, maxSubstringLength2, trainingTriples, segmentFactor != 1 ? probs * segmentFactor : probs, WeightingMode.CountWeighted, NormalizationMode.None, true, out exampleCounts);            
                probs = Program.PSecondGivenFirst(probs);
            }
        }

        /// <summary>
        /// Calculates the probability P(T|S), that is, the probability that transliteratedWord is a transliteration of sourceWord.
        /// </summary>
        /// <param name="sourceWord">The word is the source language</param>
        /// <param name="transliteratedWord">The purported transliteration of the source word, in the target language</param>
        /// <returns>P(T|S)</returns>
        public double Probability(String sourceWord, String transliteratedWord)
        {
            if (multiprobs==null) {
                multiprobs = probs * segmentFactor;
            }

            return WikiTransliteration.GetSummedAlignmentProbability(sourceWord, transliteratedWord, maxSubstringLength1, maxSubstringLength2, multiprobs, new Dictionary<Pair<String, String>, double>(), minProductionProbability)
                            / Program.segSums[sourceWord.Length - 1][transliteratedWord.Length - 1];
        }

        /// <summary>
        /// Generates a TopList of the most likely transliterations of the given word.
        /// The TopList is like a SortedList sorted most-probable to least-probable with probabilities (doubles) as keys,
        /// except that the keys may not be unique (multiple transliterations can be equiprobable).
        /// The most likely transliteration is at index 0.
        /// The number of transliterations returned in this manner will not exceed the maxCandidates property.
        /// You must train the model before generating transliterations.
        /// </summary>
        /// <param name="sourceWord">The word to transliterate.</param>
        /// <returns>A TopList containing the most likely transliterations of the word.</returns>
        public TopList<double,String> Generate(String sourceWord)
        {
            if (probs == null) throw new NullPointerException("Must train at least one iteration before generating transliterations");

            if (pruned==null)
            {
                multiprobs = probs * segmentFactor;
                pruned = Program.PruneProbs(maxCandidates, multiprobs);
                probMap = WikiTransliteration.GetProbMap(pruned);     
            }                        
                        
             TopList<double, String> result = new TopList<double,String>(maxCandidates);
             result = WikiTransliteration.Predict2(maxCandidates, sourceWord, maxSubstringLength2, probMap, pruned, new Dictionary<String, Dictionary<String, double>>(), maxCandidates);
                
            if (languageModel != null)
            {
                TopList<Double, String> fPredictions = new TopList<Double, String>(maxCandidates);
                for (KeyValuePair<Double, String> prediction : result)
                    //fPredictions.Add(prediction.Key * Math.Pow(WikiTransliteration.GetLanguageProbabilityViterbi(prediction.Value, languageModel, ngramSize),1d/(prediction.Value.Length)), prediction.Value);
                    fPredictions.Add(prediction.Key * Math.Pow(WikiTransliteration.GetLanguageProbability(prediction.Value, languageModel, ngramSize), 1), prediction.Value);
                result = fPredictions;
            }
            
            return result;
        }
    }
