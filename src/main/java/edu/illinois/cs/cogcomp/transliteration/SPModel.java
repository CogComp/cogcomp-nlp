using System;
using System.Collections.Generic;
using System.Text;
using Pasternack.Utility;
using Pasternack.Collections.Generic;
using Pasternack.Collections.Generic.Specialized;
using System.IO;

namespace SPTransliteration
{
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
    [Serializable]
    public class SPModel
    {
        const double minProductionProbability = 0.000000000000001;
        const int maxSubstringLength1 = 15;
        const int maxSubstringLength2 = 15;

        private Dictionary<string, double> languageModel=null;
        //private Dictionary<string, double> languageModelDual=null;
        private int ngramSize = 4;
        private List<Triple<string,string,double>> trainingExamples;
        //private List<Triple<string,string,double>> trainingExamplesDual;
        private SparseDoubleVector<Pair<string, string>> probs = null;
        private SparseDoubleVector<Pair<string, string>> multiprobs = null;
        private SparseDoubleVector<Pair<string, string>> pruned = null;
        //private SparseDoubleVector<Pair<string, string>> probsDual = null;
        //private SparseDoubleVector<Pair<string, string>> prunedDual = null;
        private Map<string, string> probMap = null;
        //private Map<string, string> probMapDual = null;

        private int maxCandidates=100;
        private double segmentFactor = 0.5;

        private void WriteToWriter(BinaryWriter writer, SparseDoubleVector<Pair<string, string>> table)
        {
            if (table == null)
                writer.Write((int)-1);
            else
            {
                writer.Write(table.Count);
                foreach (KeyValuePair<Pair<string, string>, double> entry in table)
                {
                    writer.Write(entry.Key.x);
                    writer.Write(entry.Key.y);
                    writer.Write(entry.Value);
                }
            }
        }

        private SparseDoubleVector<Pair<string, string>> ReadTableFromReader(BinaryReader reader)
        {
            int count = reader.ReadInt32();
            if (count == -1) return null;
            SparseDoubleVector<Pair<string, string>> table = new SparseDoubleVector<Pair<string, string>>(count);
            for (int i = 0; i < count; i++)
            {
                Pair<string, string> keyPair = new Pair<string, string>(reader.ReadString(), reader.ReadString());
                table.Add(keyPair, reader.ReadDouble());
            }

            return table;
        }

        public void WriteToStream(Stream stream)
        {
            BinaryWriter writer = new BinaryWriter(stream);

            if (languageModel == null) writer.Write((int)-1);
            else
            {
                writer.Write(languageModel.Count);
                foreach (KeyValuePair<string, double> pair in languageModel)
                {
                    writer.Write(pair.Key);
                    writer.Write(pair.Value);
                }
            }

            writer.Write(ngramSize);

            writer.Write(trainingExamples.Count);
            foreach (Triple<string, string, double> triple in trainingExamples)
            {
                writer.Write(triple.x); writer.Write(triple.y); writer.Write(triple.z);
            }

            WriteToWriter(writer, probs);
            //WriteToWriter(writer, pruned);

            /*
            if (probMap == null)
                writer.Write((int)-1);
            else
            {
                writer.Write(probMap.Count);
                foreach (KeyValuePair<string, string> pair in probMap)
                {
                    writer.Write(pair.Key); writer.Write(pair.Value);
                }
            }
             */

            writer.Flush();
        }

        /// <summary>
        /// Deserializes an SPModel from a stream.
        /// </summary>
        /// <param name="stream"></param>
        public SPModel(Stream stream)
        {        
            BinaryReader reader = new BinaryReader(stream);
            int lml = reader.ReadInt32();
            if (lml == -1) languageModel = null;
            else
            {
                languageModel = new Dictionary<string,double>(lml);
                for (int i = 0; i < lml; i++)
                    languageModel.Add(reader.ReadString(),reader.ReadDouble());
            }

            ngramSize = reader.ReadInt32();

            int tec = reader.ReadInt32();
            trainingExamples = new List<Triple<string,string,double>>(tec);
            for (int i = 0; i < tec; i++)
                trainingExamples.Add(new Triple<string,string,double>(reader.ReadString(),reader.ReadString(),reader.ReadDouble()));

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
                probMap = new Map<string,string>();
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
        public SPModel(ICollection<Example> examples)
        {
            trainingExamples = new List<Triple<string,string,double>>(examples.Count);
            foreach (Example example in examples)
                trainingExamples.Add(example.Triple);
        }

        /// <summary>
        /// Creates and sets a simple (ngram) language model to use when generating transliterations.
        /// The transliterations will then have probability == P(T|S)*P(T), where P(T) is the language model.
        /// In principle this allows you to leverage the presumably vast number of words available in the target language,
        /// although practical results may vary.
        /// </summary>
        /// <param name="targetLanguageExamples">Example words from the target language you're transliterating into.</param>
        public void SetLanguageModel(List<string> targetLanguageExamples)
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
        public void Train(IDictionary<string,string> trainingPairs)
        {
            List<KeyValuePair<string,string>> pairs = new List<KeyValuePair<string,string>>(trainingPairs.Count);
            foreach (KeyValuePair<string,string> pair in trainingPairs)
                pairs.Add(pair);

            Train(Program.ConvertExamples(pairs));
        }*/

        /// <summary>
        /// Trains the model for the specified number of iterations.
        /// </summary>
        /// <param name="emIterations">The number of iterations to train for.</param>
        public void Train(int emIterations)
        {
            List<Triple<string, string, double>> trainingTriples = trainingExamples;

            List<List<KeyValuePair<Pair<string, string>, double>>> exampleCounts;

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
        public double Probability(string sourceWord, string transliteratedWord)
        {
            if (multiprobs==null)            
                multiprobs = probs * segmentFactor;

            return WikiTransliteration.GetSummedAlignmentProbability(sourceWord, transliteratedWord, maxSubstringLength1, maxSubstringLength2, multiprobs, new Dictionary<Pair<string, string>, double>(), minProductionProbability)
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
        public TopList<double,string> Generate(string sourceWord)
        {
            if (probs == null) throw new InvalidOperationException("Must train at least one iteration before generating transliterations");
            if (pruned==null)
            {
                multiprobs = probs * segmentFactor;
                pruned = Program.PruneProbs(maxCandidates, multiprobs);
                probMap = WikiTransliteration.GetProbMap(pruned);     
            }                        
                        
             TopList<double, string> result = new TopList<double,string>(maxCandidates);
             result = WikiTransliteration.Predict2(maxCandidates, sourceWord, maxSubstringLength2, probMap, pruned, new Dictionary<string, Dictionary<string, double>>(), maxCandidates);
                
            if (languageModel != null)
            {
                TopList<double, string> fPredictions = new TopList<double, string>(maxCandidates);
                foreach (KeyValuePair<double, string> prediction in result)
                    //fPredictions.Add(prediction.Key * Math.Pow(WikiTransliteration.GetLanguageProbabilityViterbi(prediction.Value, languageModel, ngramSize),1d/(prediction.Value.Length)), prediction.Value);
                    fPredictions.Add(prediction.Key * Math.Pow(WikiTransliteration.GetLanguageProbability(prediction.Value, languageModel, ngramSize), 1), prediction.Value);
                result = fPredictions;
            }
            
            return result;
        }        
    }
}
